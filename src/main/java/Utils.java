import java.util.*;
public class Utils {

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

    static public double round(double number){
        return Math.round(number * 1000000d) / 1000000d;
    }
}
