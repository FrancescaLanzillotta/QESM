import org.apache.commons.math3.distribution.*;

import java.net.URI;
import java.rmi.MarshalException;
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
public class Main {
    public static void main(String[] args) {

        int experiments = 50;
//                ------------------------ Scenario #1: Sensitivity to alpha and beta ------------------------

//      Experiment #1: average cost of lambda-apps vs beta for various values of alpha

//        double[] alphas = {0, 0.25, 0.5, 0.75};
//        double[] betas = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
//        int invocationRate = 1;
//        int functionData = 1;
//        int stateData = 0;
//        int mean = 50;
//
//        try {
//            FileWriter fileWriter = new FileWriter("Scenario #1 - Exp #1.txt");
//            fileWriter.write("------------- Scenario #1 - Exp #1 (average cost of lambda-apps) -------------" +
//                    "\nParameters:" +
//                    "\n\t Alpha: " + Arrays.toString(alphas) +
//                    "\n\t Beta: " + Arrays.toString(betas) +
//                    "\n\t E[|muApps|]: " + mean +
//                    "\n\t E[|lambdaApps|]: " + mean +
//                    "\n\t (invocationRate, functionData, stateData): (" + invocationRate + ", " + functionData + ", " + stateData + ")" +
//                    "\n\t # experiments: " + experiments +
//                    "\n\n----------------------------------------------------------------------\n");
//
//            PoissonDistribution poissonDistribution = new PoissonDistribution(mean);
//
//            for (double alpha : alphas){
//                double[] results = new double[betas.length];
//                fileWriter.append("\nalpha: " + alpha);
//                System.out.println("Alpha: " + alpha);
//
//                for (int exp = 0; exp < experiments; exp++){
//
//                    int nMuApps = poissonDistribution.sample();
//                    int nlambdaApps = poissonDistribution.sample();
//                    System.out.println("Exp-" + exp + "(" + nMuApps + " mu apps, " + nlambdaApps + " lambda apps)");
//
//                    for (int k = 0; k < betas.length; k++){
//                        ArrayList<ComputeNode> computeNodes = Utils.createNetworkTopology(nMuApps, nlambdaApps, alpha);
//
//                        HashSet<MuApp> muApps = new HashSet<>();
//                        for (int i = 0; i < nMuApps; i++) {
//                            MuApp muApp = new MuApp(i, invocationRate, functionData);
//                            muApps.add(muApp);
//                        }
//
//                        HashSet<LambdaApp> lambdaApps = new HashSet<>();
//                        for (int i = 0; i < nlambdaApps; i++) {
//                            LambdaApp lambdaApp = new LambdaApp(i, invocationRate, functionData, stateData);
//                            lambdaApps.add(lambdaApp);
//                        }
//
//                        Orchestrator orchestrator = new Orchestrator(computeNodes, muApps, lambdaApps, betas[k]);
//
//                        orchestrator.optimalAppAllocation(false);
//
//                        results[k] += orchestrator.getAverageCostLambdaApp();
//                    }
//                }
//
//                for (int i = 0; i < results.length; i++){
//                    results[i] = Utils.round(results[i] / experiments);
//                }
//
//                fileWriter.append("\n" + Arrays.toString(results));
//            }
//            fileWriter.close();
//        } catch (IOException e){
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }

//        Experiment #2: ratio of mu-apps assigned to the cloud vs alpha

//        double[] alphas = {0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875};
//        int[] muMeans = {25, 50, 75};
//
//        int invocationRate = 1;
//        int functionData = 1;
//        int stateData = 0;
//
//        try {
//            FileWriter fileWriter = new FileWriter("Scenario #1 - Exp #2.txt");
//            fileWriter.write("------------- Scenario #1 - Exp #2 (ratio of mu-apps assigned to the cloud) -------------" +
//                    "\nParameters:" +
//                    "\n\t E[|muApps|]: " + Arrays.toString(muMeans) +
//                    "\n\t Alpha: " + Arrays.toString(alphas) +
//                    "\n\t (invocationRate, functionData, stateData): (" + invocationRate + ", " + functionData + ", " + stateData + ")" +
//                    "\n\t # experiments: " + experiments +
//                    "\n\n----------------------------------------------------------------------\n");
//
//            for (int mean : muMeans) {
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
//                        ArrayList<ComputeNode> computeNodes = Utils.createNetworkTopology(nMu, 0, alphas[k]);
//
//                        HashSet<MuApp> muApps = new HashSet<>();
//
//                        for (int i = 0; i < nMu; i++) {
//                            MuApp muApp = new MuApp(i, invocationRate, functionData);
//                            muApps.add(muApp);
//                        }
//
//                        Orchestrator o = new Orchestrator(computeNodes, muApps, new HashSet<>(), 1);
//
//                        o.optimalAppAllocation(false);
//                        results[k] += o.getCloudMuAppsRatio();
//                    }
//                }
//                for (int i = 0; i < results.length; i++){
//                    results[i] = Utils.round(results[i] / experiments);
//                }
//                fileWriter.append("\n" + Arrays.toString(results));
//            }
//            fileWriter.close();
//        } catch (IOException e){
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }

//                ------------------------ Scenario #2: Increasing the number of applications ------------------------

        double alpha = 0.5;
        double beta = 1;
        int[] nApps = {20, 40, 60, 80, 100, 120, 140, 160, 180, 200};

        try {
            FileWriter fileWriter = new FileWriter("Scenario #2 - Exp #1-2-3-4.txt");
            fileWriter.write("------------- Scenario #2 - Exp #1-2-3-4 (increasing the number of applications) -------------" +
                    "\nParameters:" +
                    "\n\t Alpha: " + alpha +
                    "\n\t Beta: " + beta +
                    "\n\t E[|muApps|]: " + Arrays.toString(new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50}) +
                    "\n\t E[|lambdaApps|]: " + Arrays.toString(new int[]{15, 30, 45, 60, 75, 90, 105, 120, 135, 150}) +
                    "\n\t Heavyweight applications (invocationRate, functionData, stateData): (" + 1 + ", " + 100 + ", " + 10 + ")" +                    "\n\t Heavyweight applications (invocationRate, functionData, stateData): (" + 1 + ", " + 100 + ", " + 10 + ")" +
                    "\n\t Lightweight applications (invocationRate, functionData, stateData): (" + 10 + ", " + 1 + ", " + 0 + ")" +
                    "\n\t # experiments: " + experiments);

            double[] muOptResults = new double[nApps.length];
            double[] lambdaOptResults = new double[nApps.length];
            double[] muOptRatio = new double[nApps.length];
            double[] lambdaOptRatio = new double[nApps.length];
            double[] muGreedResults = new double[nApps.length];
            double[] lambdaGreedResults = new double[nApps.length];
            double[] muGreedRatio = new double[nApps.length];
            double[] lambdaGreedRatio = new double[nApps.length];
            double[] muRandomResults = new double[nApps.length];
            double[] lambdaRandomResult = new double[nApps.length];
            double[] muRandomRatio = new double[nApps.length];
            double[] lambdaRandomRatio = new double[nApps.length];

            for (int k = 0; k < nApps.length; k++){
                System.out.println("E[|muApps|]: " + nApps[k] * 0.25);
                System.out.println("E[|lambdaApps|]: " + nApps[k] * 0.75);

                PoissonDistribution muPoisson = new PoissonDistribution(nApps[k] * 0.25);
                PoissonDistribution lambdaPoisson = new PoissonDistribution(nApps[k] * 0.75);

                for (int exp = 0; exp < experiments; exp++){
                    int nMu = 0;
                    while (nMu == 0){
                        nMu = muPoisson.sample();
                    }

                    int nLambda = lambdaPoisson.sample();
                    System.out.println("Exp-" + exp + " (" + nMu + " mu apps)");

                    ArrayList<ComputeNode> computeNodes = Utils.createNetworkTopology(nMu, nLambda, alpha);
                    HashSet<MuApp> muApps = new HashSet<>();
                    HashSet<LambdaApp> lambdaApps = new HashSet<>();


                    while (muApps.size() < nMu || lambdaApps.size() < nLambda){
                        boolean light = Math.random() <= 0.5;
                        if (light){
                            if (muApps.size() < nMu){
                                muApps.add(new MuApp(muApps.size(), 10, 1));
                            } else {
                                lambdaApps.add(new LambdaApp(lambdaApps.size(), 10, 1, 0));
                            }
                        } else {
                            if (muApps.size() < nMu){
                                muApps.add(new MuApp(muApps.size(), 1, 100));
                            } else {
                                lambdaApps.add(new LambdaApp(lambdaApps.size(), 1, 100, 10));
                            }
                        }
                    }

                    Orchestrator orchestrator = new Orchestrator(computeNodes, muApps, lambdaApps, beta);

                    orchestrator.optimalAppAllocation(false);


                    muOptResults[k] += orchestrator.getAverageCostMuApp();
                    muOptRatio[k] += orchestrator.getCloudMuAppsRatio();
                    lambdaOptResults[k] += orchestrator.getAverageCostLambdaApp();
                    lambdaOptRatio[k] += orchestrator.getCloudLambdaAppsRatio();

                    orchestrator.greedyAppAllocation(false);
                    muGreedResults[k] += orchestrator.getAverageCostMuApp();
                    muGreedRatio[k] += orchestrator.getCloudMuAppsRatio();
                    lambdaGreedResults[k] += orchestrator.getAverageCostLambdaApp();
                    lambdaGreedRatio[k] += orchestrator.getCloudLambdaAppsRatio();


                    orchestrator.randomAppAllocation(false);
                    muRandomResults[k] += orchestrator.getAverageCostMuApp();
                    muRandomRatio[k] += orchestrator.getCloudMuAppsRatio();
                    lambdaRandomResult[k] += orchestrator.getAverageCostLambdaApp();
                    lambdaRandomRatio[k] += orchestrator.getCloudLambdaAppsRatio();
                }

                muOptResults[k] = Utils.round(muOptResults[k] / experiments);
                muOptRatio[k] = Utils.round(muOptRatio[k] / experiments);
                lambdaOptResults[k] = Utils.round(lambdaOptResults[k] / experiments);
                lambdaOptRatio[k] = Utils.round(lambdaOptRatio[k] / experiments);

                muGreedResults[k] = Utils.round((muGreedResults[k] / experiments));
                muGreedRatio[k] = Utils.round(muGreedRatio[k] / experiments);
                lambdaGreedResults[k] = Utils.round(lambdaGreedResults[k] / experiments);
                lambdaGreedRatio[k] = Utils.round(lambdaGreedRatio[k] / experiments);

                muRandomResults[k] = Utils.round(muRandomResults[k] / experiments);
                muRandomRatio[k] = Utils.round(muRandomRatio[k] / experiments);
                lambdaRandomResult[k] = Utils.round(lambdaRandomResult[k] / experiments);
                lambdaRandomRatio[k] = Utils.round(lambdaRandomRatio[k] / experiments);


            }
            fileWriter.write("\n\n------------- Mu-apps average cost -------------\n");
            fileWriter.write("Proposed: \n" + Arrays.toString(muOptResults));
            fileWriter.write("\nGreedy: \n" + Arrays.toString(muGreedResults));
            fileWriter.write("\nRandom: \n" + Arrays.toString(muRandomResults));
            fileWriter.write("\n\n------------- Mu-apps ratio in cloud -------------\n");
            fileWriter.write("Proposed: \n" + Arrays.toString(muOptRatio));
            fileWriter.write("\nGreedy: \n" + Arrays.toString(muGreedRatio));
            fileWriter.write("\nRandom: \n" + Arrays.toString(muRandomRatio));
            fileWriter.write("\n\n------------- Lambda-apps average cost -------------\n");
            fileWriter.write("Proposed: \n" + Arrays.toString(lambdaOptResults));
            fileWriter.write("\nGreedy: \n" + Arrays.toString(lambdaGreedResults));
            fileWriter.write("\nRandom: \n" + Arrays.toString(lambdaRandomResult));
            fileWriter.write("\n\n------------- Lambda-apps ratio in cloud -------------\n");
            fileWriter.write("Proposed: \n" + Arrays.toString(lambdaOptRatio));
            fileWriter.write("\nGreedy: \n" + Arrays.toString(lambdaGreedRatio));
            fileWriter.write("\nRandom: \n" + Arrays.toString(lambdaRandomRatio));
            fileWriter.close();

        } catch (IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

//                      ------------- Scenario #3: focusing on mu-apps - Exp #1-2 -------------

//        double alpha = 0.5;
//        double beta = 1;
//        int[] muMeans = {25, 50};
//
//        int invocationRate = 5;
//        int stateData = 0;
//        int[] functionDatas = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20};
//
//        try {
//            FileWriter fileWriter = new FileWriter("Scenario #3 - Exp #1-2.txt");
//            fileWriter.write("------------- Scenario #3 - Exp #1-2 (focusing on mu-apps) -------------" +
//                    "\nParameters:" +
//                    "\n\t Alpha: " + alpha +
//                    "\n\t Beta: " + beta +
//                    "\n\t E[|muApps|]: " + Arrays.toString(muMeans) +
//                    "\n\t E[|lambdaApps|]: " + 0 +
//                    "\n\t Class 1 applications (invocationRate, functionData, stateData): (" + invocationRate + ", " + 100 + ", " + stateData + ")" +                     "\n\t Class 2 applications (invocationRate, functionData, stateData): (" + invocationRate + ", " + Arrays.toString(functionDatas) + ", " + stateData + ")" +
//                    "\n\t # experiments: " + experiments);
//
//            for (int muMean : muMeans){
//                PoissonDistribution poissonDistribution = new PoissonDistribution(muMean);
//                double[] muOptResults = new double[functionDatas.length];
//                double[] muOptRatio = new double[functionDatas.length];
//                double[] muGreedResults = new double[functionDatas.length];
//                double[] muGreedRatio = new double[functionDatas.length];
//                double[] muRandomResults = new double[functionDatas.length];
//                double[] muRandomRatio = new double[functionDatas.length];
//
//                for (int k = 0; k < functionDatas.length; k++){
//                    for (int exp = 0; exp < experiments; exp++){
//                        int nMu = poissonDistribution.sample();
//
//                        ArrayList<ComputeNode> computeNodes = Utils.createNetworkTopology(nMu, 0, alpha);
//
//                        HashSet<MuApp> muApps = new HashSet<>();
//
//                        for (int i = 0; i < nMu; i++){
//                            int functionData = 100;
//                            boolean dynamic = Math.random() >= 0.5;
//                            if (dynamic){
//                                functionData = functionDatas[k];
//                            }
//                            muApps.add(new MuApp(i, invocationRate, functionData));
//                        }
//
//                        Orchestrator orchestrator = new Orchestrator(computeNodes, muApps, new HashSet<>(), beta);
//
//                        orchestrator.optimalAppAllocation(false);
//                        muOptResults[k] += orchestrator.getAverageCostMuApp();
//                        muOptRatio[k] += orchestrator.getCloudMuAppsRatio();
//
//                        orchestrator.greedyAppAllocation(false);
//                        muGreedResults[k] += orchestrator.getAverageCostMuApp();
//                        muGreedRatio[k] += orchestrator.getCloudMuAppsRatio();
//
//                        orchestrator.randomAppAllocation(false);
//                        muRandomResults[k] += orchestrator.getAverageCostMuApp();
//                        muRandomRatio[k] += orchestrator.getCloudMuAppsRatio();
//                    }
//
//                    muOptResults[k] = Utils.round(muOptResults[k] / experiments);
//                    muOptRatio[k] = Utils.round(muOptRatio[k] / experiments);
//                    muGreedResults[k] = Utils.round( muGreedResults[k] / experiments);
//                    muGreedRatio[k] = Utils.round(muGreedRatio[k] / experiments);
//                    muRandomResults[k] = Utils.round(muRandomResults[k] / experiments);
//                    muRandomRatio[k] = Utils.round(muRandomRatio[k] / experiments);
//
//                }
//                fileWriter.write("\n\n------------- Mu-apps average cost (E[|muApps|] = " + muMean + ")-------------\n");
//                fileWriter.write("Proposed: \n" + Arrays.toString(muOptResults));
//                fileWriter.write("\nGreedy: \n" + Arrays.toString(muGreedResults));
//                fileWriter.write("\nRandom: \n" + Arrays.toString(muRandomResults));
//                fileWriter.write("\n\n------------- Mu-apps ratio in cloud (E[|muApps|] = " + muMean + ")-------------\n");
//                fileWriter.write("Proposed: \n" + Arrays.toString(muOptRatio));
//                fileWriter.write("\nGreedy: \n" + Arrays.toString(muGreedRatio));
//                fileWriter.write("\nRandom: \n" + Arrays.toString(muRandomRatio));
//            }
//
//            fileWriter.close();
//        } catch (IOException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }

//        -------------------------- Scenario #4 - Exp #1 (focusing on lambda-apps) --------------------------

//        double alpha = 0.5;
//        double beta = 1;
//        int muMean = 37;
//        int lambdaMean = 113;
//
//        int invocationRate = 5;
//        int[] stateDatas = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
//        int[] functionDatas = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
//
//        try {
//            FileWriter fileWriter = new FileWriter("Scenario #4 - Exp #1.txt");
//            fileWriter.write("------------- Scenario #4 - Exp #1 (focusing on lambda-apps) -------------" +
//                    "\nParameters:" +
//                    "\n\t Alpha: " + alpha +
//                    "\n\t Beta: " + beta +
//                    "\n\t E[|muApps|]: " + muMean +
//                    "\n\t E[|lambdaApps|]: " + lambdaMean +
//                    "\n\t Class 1 applications (invocationRate, functionData, stateData): (" + invocationRate + ", " + 100 + ", " + 10 + ")" +
//                    "\n\t Class 2 applications (invocationRate, functionData, stateData): (" + invocationRate + ", " + Arrays.toString(functionDatas) + ", " + Arrays.toString(stateDatas) + ")" +
//                    "\n\t # experiments: " + experiments);
//
//
//            PoissonDistribution muDistribution = new PoissonDistribution(muMean);
//            PoissonDistribution lambdaDistribution = new PoissonDistribution(lambdaMean);
//            double[][] results = new double[stateDatas.length][functionDatas.length];
//
//            for (int k = 0; k < stateDatas.length; k++){
//                for (int l = 0; l < functionDatas.length; l++){
//                    for (int exp = 0; exp < experiments; exp++){
//                        int nMu = muDistribution.sample();
//                        int nLambda = lambdaDistribution.sample();
//
//                        ArrayList<ComputeNode> computeNodes = Utils.createNetworkTopology(nMu, 0, alpha);
//
//                        HashSet<MuApp> muApps = new HashSet<>();
//                        HashSet<LambdaApp> lambdaApps = new HashSet<>();
//
//
//                        for (int i = 0; i < nMu + nLambda; i++){
//                            int functionData = 100;
//                            int stateData = 10;
//                            boolean dynamic = Math.random() >= 0.5;
//                            if (dynamic){
//                                functionData = functionDatas[l];
//                                stateData = stateDatas[k];
//                            }
//                            if (muApps.size() < nMu){
//                                muApps.add(new MuApp(muApps.size(), invocationRate, functionData));
//                            } else if (lambdaApps.size() < nLambda) {
//                                lambdaApps.add(new LambdaApp(lambdaApps.size(), invocationRate, functionData, stateData));
//                            }
//                        }
//
//                        Orchestrator orchestrator = new Orchestrator(computeNodes, muApps, lambdaApps, beta);
//
//                        orchestrator.optimalAppAllocation(false);
//                        results[k][l] += orchestrator.getAverageCostLambdaApp();
//                    }
//
//                    results[k][l] = Utils.round(results[k][l] / experiments);
//                }
//            }
//            fileWriter.write("\n\n------------- Lambda-apps average cost -------------\n");
//            fileWriter.write("[");
//            for (int row = 0; row < stateDatas.length; row++){
//                fileWriter.write(Arrays.toString(results[row]) + ",\n");
//            }
//            fileWriter.write("]");
//
//            fileWriter.close();
//        } catch (IOException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }

    }
}