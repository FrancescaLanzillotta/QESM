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

    private ComputeNode getComputeNode(String id){
        return computeNodes.get(Integer.parseInt(id.split("-")[0].substring(2)));
    }

    private ComputeNode getCheapestComputeNode(MuApp muApp){
        ComputeNode cheapest = null;
        int minHops = Integer.MAX_VALUE;
        for (ComputeNode cn : computeNodes){
            if (cn.getRemainderMuContainers() > 0 && cn.getHopsToClient() < minHops){
                cheapest = cn;
                minHops = cn.getHopsToClient() * muApp.getFunctionData();
            }
        }
        return cheapest;
    }

    private void addMuApp(MuApp muApp, ComputeNode computeNode){
        muApps.add(muApp);
        muApp.assignContainer(computeNode.addMuContainer(muApp.getId()));

    }

    private void removeMuApp(MuApp muApp){
        muApps.remove(muApp);
        ComputeNode cn = getComputeNode(muApp.getContainer().getId());
        cn.removeMuContainer(muApp.getContainer());
    }

    private void addLambdaApp(LambdaApp lambdaApp, ComputeNode computeNode){
        lambdaApps.add(lambdaApp);
        double residualInvocation = lambdaApp.getInvocationRate();

        for (LambdaContainer lambdaContainer : computeNode.getLambdaContainers()){
            if (lambdaContainer.getResidualCapacity() > 0 && residualInvocation > 0){
                double resources = Math.min(lambdaContainer.getResidualCapacity(), residualInvocation);
                lambdaApp.assignLambdaContainer(lambdaContainer, resources / lambdaApp.getInvocationRate());
                lambdaContainer.useResources(resources);
                residualInvocation -= resources;
            }
        }

        while (residualInvocation > 0){
            LambdaContainer lc = computeNode.addLambdaContainer();
            if (lc == null){
                break;
            } else {
                double resource = Math.min(lc.getResidualCapacity(), residualInvocation);
                lambdaApp.assignLambdaContainer(lc, resource / lambdaApp.getInvocationRate());
                lc.useResources(resource);
                residualInvocation -= resource;
            }
        }
    }

    private void removeLambdaApp(LambdaApp lambdaApp){
        lambdaApps.remove(lambdaApp);
        for (Map.Entry<LambdaContainer, Double> entry : lambdaApp.getContainerWeights().entrySet()){
            entry.getKey().freeResources(entry.getValue() * lambdaApp.getInvocationRate());
        }
        lambdaApp.getContainerWeights().clear();

        for (ComputeNode computeNode : computeNodes){
            computeNode.removeInactive();
        }
    }

    private void resetAllocation(){
        for (MuApp muApp : muApps){
            if (muApp.getContainer() != null){
                ComputeNode cn = getComputeNode(muApp.getContainer().getId());
                cn.removeMuContainer(muApp.getContainer());
                muApp.resetAllocation();
            }
        }

        for (LambdaApp lambdaApp : lambdaApps){
            if (!lambdaApp.getContainerWeights().isEmpty()){
                for (Map.Entry<LambdaContainer, Double> entry : lambdaApp.getContainerWeights().entrySet()){
                    entry.getKey().freeResources(entry.getValue() * lambdaApp.getInvocationRate());
                }
                lambdaApp.getContainerWeights().clear();
            }

            for (ComputeNode computeNode : computeNodes){
                computeNode.removeInactive();
            }
        }
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
    private void optimalMuAppsAllocation(){

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
                muApp.assignContainer(getComputeNode(muContainer.getId()).addMuContainer(muApp.getId()));
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
    private void optimalLambdaAppsAllocation(){
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
            double c2 = computeNode == computeNodes.getFirst() ? 0 : 10;
            // For each container, add two nodes: a dummy node and the container node.
            // The dummy node has one incoming edge connecting it to the container node with cost zero and capacity
            // equal to the service rate of the compute node hosting the container, and one outcoming edge connecting
            // it to destination node, also with cost zero and capacity equal to the service rate of the compute node
            // hosting the container
            while (computeNode.getRemainderLambdaContainers() > 0) {
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
                    lambdaContainer.useResources(entry.getValue());
                    inactiveContainers.remove(lambdaContainer);
                }
            }
        }

        for (ComputeNode cn : computeNodes){
            cn.removeInactive();
        }
    }

    public void optimalAppAllocation(boolean display){
        resetAllocation();

        optimalMuAppsAllocation();

        if (display) {
            System.out.println("----- Mu apps assigned -----");
            for (MuApp muApp : muApps){
                System.out.println(muApp.getId() + " -> " + muApp.getContainer().getId());
            }
        }

        optimalLambdaAppsAllocation();
        if (display) {
            System.out.println("----- Lambda apps assigned -----");
            for (LambdaApp lambdaApp : lambdaApps){
                System.out.println(lambdaApp.getId() + " -> " + lambdaApp.getContainerWeights());
            }
        }
    }

    public void greedyAppAllocation(boolean display){
        resetAllocation();
        for (MuApp muApp : muApps){
            addMuApp(muApp, getCheapestComputeNode(muApp));
        }

        if (display) {
            System.out.println("----- Mu apps assigned -----");
            for (MuApp muApp : muApps){
                System.out.println(muApp.getId() + " -> " + muApp.getContainer().getId());
            }
        }
        for (LambdaApp lambdaApp : lambdaApps){
            int residualInvocation = lambdaApp.getInvocationRate();
            while (residualInvocation > 0){
                int minCost = Integer.MAX_VALUE;
                ComputeNode cheapest = null;
                for (ComputeNode cn : computeNodes){
                    if (cn.getTotalResidualCapacity() > 0 || cn.getRemainderLambdaContainers() > 0){
                        int c1 = cn.getHopsToClient();
                        int c2 = cn == computeNodes.getFirst() ? 0 : 10;
                        if (c1 * lambdaApp.getFunctionData() + c2 * lambdaApp.getStateData() < minCost){
                            minCost = c1 * lambdaApp.getFunctionData() + c2 * lambdaApp.getStateData();
                            cheapest = cn;
                        }
                    }
                }
                LambdaContainer lambdaContainer = null;
                if (cheapest.getTotalResidualCapacity() > 0){
                    for (LambdaContainer lc : cheapest.getLambdaContainers()){
                        if (lc.getResidualCapacity() > 0){
                            lambdaContainer = lc;
                        }
                    }
                } else if (cheapest.getRemainderLambdaContainers() > 0) {
                    lambdaContainer = cheapest.addLambdaContainer();
                }
                if (lambdaContainer != null){
                    lambdaApp.assignLambdaContainer(lambdaContainer, (double) 1 / lambdaApp.getInvocationRate());
                    lambdaContainer.useResources(1);
                    residualInvocation -= 1;
                }
            }
        }
//        for (LambdaApp lambdaApp : lambdaApps){
//            int functionData = lambdaApp.getFunctionData();
//            int stateData = lambdaApp.getStateData();
//            int residualInvocation = lambdaApp.getInvocationRate();
//            while (residualInvocation > 0){
//                ComputeNode cheapest = getCheapestComputeNode(lambdaApp);
//                if (cheapest == null){
//                    System.out.println(computeNodes);
//                    System.out.println(lambdaApp);
//                }
//                double tmp = cheapest.getTotalResidualCapacity();
//                addLambdaApp(lambdaApp, cheapest);
//                residualInvocation -= (int) (tmp - cheapest.getTotalResidualCapacity());
//            }
//        }
        if (display) {
            System.out.println("----- Lambda apps assigned -----");
            for (LambdaApp lambdaApp : lambdaApps){
                System.out.println(lambdaApp.getId() + " -> " + lambdaApp.getContainerWeights());
            }
        }
    }

    public void randomAppAllocation(boolean display){
        resetAllocation();
        LinkedList<String> muContainers = new LinkedList<>();
        for (ComputeNode cn : computeNodes){
            for (int i = 0; i < cn.getRemainderMuContainers(); i++){
                muContainers.add(cn.getId());
            }
        }
        for(MuApp muApp : muApps){
            while (muApp.getContainer() == null){
                ComputeNode cn = getComputeNode(muContainers.remove((int) (Math.random() * muContainers.size())));
                addMuApp(muApp, cn);
            }
        }

        if (display) {
            System.out.println("----- Mu apps assigned -----");
            for (MuApp muApp : muApps){
                System.out.println(muApp.getId() + " -> " + muApp.getContainer().getId());
            }
        }

        ArrayList<LambdaContainer> lambdaContainers = new ArrayList<>();
        for (ComputeNode cn : computeNodes){
            while (cn.getRemainderLambdaContainers() > 0){
                lambdaContainers.add(cn.addLambdaContainer());
            }
        }
        for (LambdaApp lambdaApp : lambdaApps){
            int residualInvocation = lambdaApp.getInvocationRate();
            while (residualInvocation > 0){
                int randomIndex = (int) (Math.random() * lambdaContainers.size());
                LambdaContainer lambdaContainer = lambdaContainers.get(randomIndex);
                lambdaApp.assignLambdaContainer(lambdaContainer, (double) 1 / lambdaApp.getInvocationRate());
                lambdaContainer.useResources(1);
                residualInvocation -= 1;
                if (lambdaContainer.getResidualCapacity() == 0){
                    lambdaContainers.remove(lambdaContainer);
                }
            }
        }

        if (display) {
            System.out.println("----- Lambda apps assigned -----");
            for (LambdaApp lambdaApp : lambdaApps){
                System.out.println(lambdaApp.getId() + " -> " + lambdaApp.getContainerWeights());
            }
        }
    }

    public void switchMuApp(MuApp muApp, int stateData){
        removeMuApp(muApp);
        LambdaApp lambdaApp = new LambdaApp(lambdaApps.size(), muApp.getInvocationRate(), muApp.getFunctionData(), stateData);
        addLambdaApp(lambdaApp, computeNodes.getFirst());
    }

    public void switchLambdaApp(LambdaApp lambdaApp){
        removeLambdaApp(lambdaApp);
        MuApp muApp = new MuApp(muApps.size(), lambdaApp.getInvocationRate(), lambdaApp.getFunctionData());
        addMuApp(muApp, getCheapestComputeNode(muApp));
    }

    public double getAverageCostMuApp(){
        double average = 0;
        for(MuApp muApp : muApps){
            ComputeNode cn = getComputeNode(muApp.getContainer().getId());
            average += muApp.getFunctionData() * cn.getHopsToClient() * muApp.getInvocationRate();
        }
        average /= muApps.size();
        return average;
    }

    public double getCloudMuAppsRatio(){
        double total = 0;
        double cloud = 0;
        for (MuApp muApp : muApps){
            if (getComputeNode(muApp.getContainer().getId()) == computeNodes.getFirst()){
                cloud += muApp.getInvocationRate();
            }
            total += muApp.getInvocationRate();
        }
        return cloud / total;
    }

    public double getAverageCostLambdaApp(){
        double average = 0;
        for (LambdaApp lambdaApp : lambdaApps){
            for (Map.Entry<LambdaContainer, Double> entry : lambdaApp.getContainerWeights().entrySet()){
                ComputeNode cn = getComputeNode(entry.getKey().getId());
                double c1 = cn.getHopsToClient();
                int c2 = cn == computeNodes.getFirst() ? 0 : 10;
                average += (c1 * lambdaApp.getFunctionData() + c2 * lambdaApp.getStateData()) * entry.getValue() * lambdaApp.getInvocationRate();
            }
        }
        average /= lambdaApps.size();
        return average;
    }

    public double getCloudLambdaAppsRatio(){
        double total = 0;
        double cloud = 0;
        for (LambdaApp lambdaApp : lambdaApps){
            for (Map.Entry<LambdaContainer, Double> entry : lambdaApp.getContainerWeights().entrySet()){
                ComputeNode cn = getComputeNode(entry.getKey().getId());
                if (cn == computeNodes.getFirst()){
                    cloud += lambdaApp.getInvocationRate() * entry.getValue();
                }
                total += lambdaApp.getInvocationRate() * entry.getValue();
            }
        }
        return cloud / total;
    }


}
