import org.apache.commons.math3.distribution.*;
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
public class Main {
    public static void main(String[] args) {


//                ------------------------ Scenario #1: Sensitivity to alpha and beta ------------------------

        int experiments = 50;
        int invocationRate = 1;
        int functionData = 1;
        int stateData = 0;

//      Experiment #1: average cost of lambda-apps vs beta for various values of alpha

        double[] alphas = {0, 0.25, 0.5, 0.75};
        double[] betas = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        int mean = 50;

        try {
            FileWriter fileWriter = new FileWriter("Scenario #1 - Exp #1.txt");
            fileWriter.write("------------- Scenario #1 - Exp #1 (average cost of lambda-apps) -------------" +
                    "\nParameters:" +
                    "\n\t Alpha: " + Arrays.toString(alphas) +
                    "\n\t Beta: " + Arrays.toString(betas) +
                    "\n\t E[|muApps|]: " + mean +
                    "\n\t E[|lambdaApps|]: " + mean +
                    "\n\t (invocationRate, functionData, stateData): (" + invocationRate + ", " + functionData + ", " + stateData + ")" +
                    "\n\t # experiments: " + experiments +
                    "\n\n----------------------------------------------------------------------\n");

            PoissonDistribution poissonDistribution = new PoissonDistribution(mean);

            for (double alpha : alphas){
                double[] results = new double[betas.length];
                fileWriter.append("\nalpha: " + alpha);
                System.out.println("Alpha: " + alpha);

                for (int exp = 0; exp < experiments; exp++){

                    int nMuApps = poissonDistribution.sample();
                    int nlambdaApps = poissonDistribution.sample();
                    System.out.println("Exp-" + exp + "(" + nMuApps + " mu apps, " + nlambdaApps + " lambda apps)");

                    for (int k = 0; k < betas.length; k++){
                        ArrayList<ComputeNode> computeNodes = Utils.createNetworkTopology(nMuApps, nlambdaApps, alpha);

                        HashSet<MuApp> muApps = new HashSet<>();
                        for (int i = 0; i < nMuApps; i++) {
                            MuApp muApp = new MuApp(i, invocationRate, functionData);
                            muApps.add(muApp);
                        }

                        HashSet<LambdaApp> lambdaApps = new HashSet<>();
                        for (int i = 0; i < nlambdaApps; i++) {
                            LambdaApp lambdaApp = new LambdaApp(i, invocationRate, functionData, stateData);
                            lambdaApps.add(lambdaApp);
                        }

                        Orchestrator o = new Orchestrator(computeNodes, muApps, lambdaApps, betas[k]);

                        o.assignMuApps();
                        o.assignLambdaApps();

                        HashMap<LambdaApp, Double> costs = o.getLambdaCosts();
                        double avg = 0;
                        for (double cost : costs.values()){
                            avg += cost;
                        }
                        results[k] += avg / nlambdaApps;
                    }
                }

                for (int i = 0; i < results.length; i++){
                    results[i] /= experiments;
                }
                fileWriter.append("\n" + Arrays.toString(results));
            }
            fileWriter.close();
        } catch (IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

//        -------------------------------------------------------------------------------------------------------------

//        Experiment #2: ratio of mu-apps assigned to the cloud vs alpha

//        double[] alphas = {0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875};
//        int[] means = {25, 50, 75};
//
//        try {
//            FileWriter fileWriter = new FileWriter("Scenario #1 - Exp #2.txt");
//            fileWriter.write("------------- Scenario #1 - Exp #2 (ratio of mu-apps assigned to the cloud) -------------" +
//                    "\nParameters:" +
//                    "\n\t Mean: " + Arrays.toString(means) +
//                    "\n\t Alpha: " + Arrays.toString(alphas) +
//                    "\n\t (invocationRate, functionData, stateData): (" + invocationRate + ", " + functionData + ", " + stateData + ")" +
//                    "\n\t # experiments: " + experiments +
//                    "\n\n----------------------------------------------------------------------\n");
//
//            for (int mean : means) {
//                fileWriter.append("\nE[|muApps|] = " + mean);
//                PoissonDistribution poissonDistribution = new PoissonDistribution(mean);
//                double[] results = new double[alphas.length];
//
//                for (int exp = 0; exp < experiments; exp++){
//
//                    int nMu = poissonDistribution.sample();
//                    System.out.println("Exp-" + exp + "(" + nMu + " mu apps)");
//
//                    for (int k = 0; k < alphas.length; k++) {
//
//                        ArrayList<ComputeNode> computeNodes = Utils.createNetworkTopology(nMu, 0, alphas[k]);
//                        HashSet<MuApp> muApps = new HashSet<>();
//
//                        for (int i = 0; i < nMu; i++) {
//                            MuApp muApp = new MuApp(i, invocationRate, functionData);
//                            muApps.add(muApp);
//                        }
//
//                        Orchestrator o = new Orchestrator(computeNodes, muApps, new HashSet<>(), 1);
//
//                        o.assignMuApps();
//
//                        results[k] += (double) computeNodes.getFirst().getMuContainersSize() / nMu;
//                    }
//                }
//                for (int i = 0; i < results.length; i++){
//                    results[i] /= experiments;
//                }
//
//                fileWriter.append("\n" + Arrays.toString(results));
//            }
//            fileWriter.close();
//        } catch (IOException e){
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }

    }
}