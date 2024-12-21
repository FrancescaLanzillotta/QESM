import java.util.*;
public class Utils {

    static public int getComputeNodeIndex(String id){
        return Integer.parseInt(id.split("-")[0].substring(2));
    }

    static public ArrayList<ComputeNode> createNetworkTopology(int nMuApps, int nLambdaApps, double alpha){
        ArrayList<ComputeNode> computeNodes = new ArrayList<>(14);

        int maxCon = (int) ((nMuApps + nLambdaApps) + ((nMuApps + nLambdaApps) * 0.5));
        ComputeNode cloud = new ComputeNode(0, maxCon, (nMuApps + (nMuApps * 0.5)) / maxCon, 12);
        computeNodes.add(cloud);

        for (int i = 1; i < 14; i++){
            if (i < 4){     // add three near-edge compute nodes
                computeNodes.add(new ComputeNode(i, alpha, 6));
            } else {        // add ten far-edge compute nodes
                computeNodes.add(new ComputeNode(i, alpha, 2));
            }
        }
        return computeNodes;
    }

}
