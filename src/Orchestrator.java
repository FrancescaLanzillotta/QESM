import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import java.util.*;

public class Orchestrator {
    private HashSet<MuApp> muApps;
    private HashSet<LambdaApp> lambdaApps;
    private ArrayList<ComputeNode> computeNodes;
    private double beta;

    public Orchestrator(ArrayList<ComputeNode> computeNodes, HashSet<MuApp> muApps, HashSet<LambdaApp> lambdaApps, double beta){
        this.computeNodes = computeNodes;
        this.muApps = muApps;
        this.lambdaApps = lambdaApps;
        this.beta = beta;
    }

    /**
     * This method solves the mu-apps allocation sub-problem, which means that all the mu-apps
     * will be assigned to a container on one of the compute nodes.
     * This problem is solved using the Hungarian method, which assigns tasks to workers under a given cost for
     * each pair (task,worker) in order to minimize the total cost.
     * Each mu application is a task and each container in a given compute node that can be assigned
     * to mu-apps, according to maxContainers and alpha, is a worker.
     * The Hungarian algorithm is solved using the graph-based implementation contained in the JGraphT library.
     * Given a complete bipartite graph G=(muApps,muContainers; E) such that:
     *  <ul>
     *      <li> |muApps| = |muContainers| </li>
     *      <li> each edge connecting muApp to a container running on computeNode has non-negative cost computed as
     *      <pre> cost = computeNode.hopsToClient * muApp.functionData
     *      </pre></li>
     *  </ul>
     * the algorithm finds the perfect matching of minimal cost.
     * The time complexity is O(V^3), where V is the set of vertices in the graph.
     */
    public void assignMuApps(){

        SimpleWeightedGraph<Object, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // create a weighted graph, and add a vertex for each of the mu-apps and each of the
        // available containers running on the compute nodes in the network
        HashSet<MuApp> muAppHashSet = new HashSet<>();
        for (MuApp muApp : muApps){
            graph.addVertex(muApp);
            muAppHashSet.add(muApp);
        }

        HashSet<MuContainer> muContainerHashSet = new HashSet<>();
        for (ComputeNode cn : computeNodes){
            for (int i = 0; i < cn.getMaxMu(); i++){
                MuContainer mc = new MuContainer(cn.getId());
                graph.addVertex(mc);
                muContainerHashSet.add(mc);
                // add an edge between each mu-app and each container with suitable cost
                for (MuApp muApp : muApps){
                    DefaultWeightedEdge e = graph.addEdge(muApp, mc);
                    graph.setEdgeWeight(e, cn.getHopsToClient() * muApp.getFunctionData());
                }
            }
        }

        // if there are more available containers than apps, add dummy applications with no associated cost
        while (muAppHashSet.size() < muContainerHashSet.size()){
            MuApp dummy = new MuApp(muAppHashSet.size(), 0, 0);
            graph.addVertex(dummy);
            muAppHashSet.add(dummy);
            for (MuContainer mc : muContainerHashSet){
                DefaultWeightedEdge e = graph.addEdge(dummy, mc);
                graph.setEdgeWeight(e, 0);
            }
        }


        KuhnMunkresMinimalWeightBipartitePerfectMatching<Object, DefaultWeightedEdge> hungarian =
                new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, muAppHashSet, muContainerHashSet);

        MatchingAlgorithm.Matching<Object, DefaultWeightedEdge> matching = hungarian.getMatching();
        for (DefaultWeightedEdge e : matching.getEdges()){
            if (muApps.contains(graph.getEdgeSource(e))){ // weed out dummies
                MuApp muApp = (MuApp) graph.getEdgeSource(e);
                MuContainer muContainer = (MuContainer) graph.getEdgeTarget(e);
                muApp.assignContainer(computeNodes.get(Utils.getComputeNodeIndex(muContainer.getId())).addMuContainer(muApp.getId()));
            }
        }
    }

    /**
     * This method solves the lambda-apps allocation sub-problem i.e. for each application find the weights by which
     * the load-balancing of the app's requests will be performed.
     * The problem is modeled as an equivalent minimum cost flow problem and is solved using the implementation
     * contained in the JGraphT library, which uses the successive shortest path algorithm.
     *
     */
    public void assignLambdaApps(){
        SimpleDirectedWeightedGraph<Object, DefaultWeightedEdge> graph =
                new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // create and add to the directed weighted graph the source and destination nodes
        String source = "source";
        String dest = "destination";
        graph.addVertex(source);
        graph.addVertex(dest);

        // Map used to define the maximum capacities for each edge in the graph
        // The capacity is set so that every application does not request more than its request rate
        // and each container is not assigned more than the service rate of its hosting edge node
        Map<DefaultWeightedEdge, Integer> upperArcCapacities = new HashMap<>();

        // total amount of flow to input in the graph in order to solve the problem
        int totalFlow = 0;

        // For each application, add two nodes: a dummy node and the app node.
        // The dummy node has one incoming edge connecting it to the source node with cost zero and capacity equal to
        // the invocation rate of the app, and one outcoming edge connecting it to the app, also with cost zero and
        // capacity equal to the invocation rate of the app.
        for (LambdaApp lambdaApp : lambdaApps){
            totalFlow += lambdaApp.getInvocationRate();
            String dummyLambdaId = "'" + lambdaApp.getId();
            graph.addVertex(lambdaApp);
            graph.addVertex(dummyLambdaId);
            DefaultWeightedEdge e = graph.addEdge(source, dummyLambdaId);
            graph.setEdgeWeight(e, 0);
            upperArcCapacities.put(e, lambdaApp.getInvocationRate());
            e = graph.addEdge(dummyLambdaId, lambdaApp);
            graph.setEdgeWeight(e, 0);
            upperArcCapacities.put(e, lambdaApp.getInvocationRate());
        }

        HashSet<LambdaContainer> inactiveContainers = new HashSet<>();
        for (ComputeNode computeNode : computeNodes) {
            double c1 = computeNode.getHopsToClient();
            double c2 = Utils.getComputeNodeIndex(computeNode.getId()) == 0 ? 0 : 10;
            // For each container, add two nodes: a dummy node and the container node.
            // The dummy node has one incoming edge connecting it to the container node with cost zero and capacity
            // equal to the service rate of the compute node hosting the container, and one outcoming edge connecting
            // it to destination node, also with cost zero and capacity equal to the service rate of the compute node
            // hosting the container
            while (computeNode.getRemainderContainers() > 0) {
                LambdaContainer lc = computeNode.addLambdaContainer();
                inactiveContainers.add(lc);
                int maxCap = (int) Math.floor(lc.getServiceRate() * beta);
                LambdaContainer dummyLambdaCon = new LambdaContainer("'" + lc.getId(), maxCap);
                graph.addVertex(lc);
                graph.addVertex(dummyLambdaCon);
                DefaultWeightedEdge e = graph.addEdge(lc, dummyLambdaCon);
                graph.setEdgeWeight(e, 0);
                upperArcCapacities.put(e, maxCap);
                e = graph.addEdge(dummyLambdaCon, dest);
                graph.setEdgeWeight(e, 0);
                upperArcCapacities.put(e, maxCap);
                // Each container node has one outcoming edge connecting it to every application node previously added
                // to the graph, with associated cost depending on c1, c2 and the application's functionData and
                // stateData. The capacities of the edges is defined as the minimum value between the app's invocation
                // rate and the container's serviceRate multiplied by the over provisioning parameter beta.
                for (LambdaApp lambdaApp : lambdaApps){
                    e = graph.addEdge(lambdaApp, lc);
                    graph.setEdgeWeight(e, c1 * lambdaApp.getFunctionData() + c2 * lambdaApp.getStateData());
                    upperArcCapacities.put(e, Math.min(maxCap, lambdaApp.getInvocationRate()));
                }
            }
        }

        Map<Object, Integer> supplies = new HashMap<>();

        // The only nodes with nonzero supplies are source (+) and destination (-)
        supplies.put(source, totalFlow);
        supplies.put(dest, -totalFlow);

        MinimumCostFlowProblem.MinimumCostFlowProblemImpl<Object, DefaultWeightedEdge> minimumCostFlowProblem =
                new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(graph,
                        (vertex) -> supplies.getOrDefault(vertex, 0),
                        upperArcCapacities::get);

        MinimumCostFlowAlgorithm<Object, DefaultWeightedEdge> minimumCostFlowAlgorithm =
                new CapacityScalingMinimumCostFlow<>(1);

        MinimumCostFlowAlgorithm.MinimumCostFlow<DefaultWeightedEdge> res = minimumCostFlowAlgorithm.getMinimumCostFlow(minimumCostFlowProblem);
        Map<DefaultWeightedEdge, Double> flowMap = res.getFlowMap();

        for (Map.Entry<DefaultWeightedEdge, Double> entry : flowMap.entrySet()){
            if (graph.getEdgeSource(entry.getKey()) instanceof LambdaApp){ // consider only application nodes
                if (entry.getValue() != 0){
                    LambdaApp lambdaApp = (LambdaApp) graph.getEdgeSource(entry.getKey());
                    LambdaContainer lambdaContainer = (LambdaContainer) graph.getEdgeTarget(entry.getKey());
                    lambdaApp.assignLambdaContainer(lambdaContainer, entry.getValue() / lambdaApp.getInvocationRate());
                    inactiveContainers.remove(lambdaContainer);
                }
            }
        }

        for (ComputeNode cn : computeNodes){
            for (LambdaContainer lc : inactiveContainers){
                cn.removeLambdaContainer(lc);
            }
        }


    }

    public ComputeNode getComputeNode(String id){
        return computeNodes.get(Utils.getComputeNodeIndex(id));
    }

    public void addMuApp(MuApp muApp){
        muApps.add(muApp);
        ComputeNode cloud = computeNodes.getFirst();
        muApp.assignContainer(cloud.addMuContainer(muApp.getId()));
    }

    public HashMap<LambdaApp, Double> getLambdaCosts(){
        HashMap <LambdaApp, Double> costs = new HashMap<>();
        for (LambdaApp lambdaApp : lambdaApps){
            double cost = 0;
            for (Map.Entry<LambdaContainer, Double> entry : lambdaApp.getContainerWeights().entrySet()){
                int CNId = Utils.getComputeNodeIndex(entry.getKey().getId());
                double costToCN = computeNodes.get(CNId).getHopsToClient();
                int costToCloud = CNId == 0 ? 0 : 10;
                cost += (costToCN * lambdaApp.getFunctionData() + costToCloud * lambdaApp.getStateData()) * entry.getValue();
            }
            costs.put(lambdaApp, cost);
        }
        return costs;
    }

}
