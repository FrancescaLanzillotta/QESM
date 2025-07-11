import java.util.LinkedList;

public class ComputeNode{
    private String id;
    private LinkedList<MuContainer> muContainers;
    private LinkedList<LambdaContainer> lambdaContainers;
    private double alpha;
    private int hopsToClient; // 2 (far-edge) | 6 (near-edge) | 12 (cloud)
    private int maxContainers;
    private int serviceRate;
    private int maxMu;

    public ComputeNode(int id, int maxContainers, double alpha, int hopsToClient) {
        this.id = "CN" + id;
        this.maxContainers = maxContainers;
        this.alpha = alpha;
        maxMu = (int) (maxContainers * alpha);

        this.hopsToClient = hopsToClient;
        serviceRate = hopsToClient > 2 ? 20 : 10;
        this.lambdaContainers = new LinkedList<>();
        this.muContainers = new LinkedList<>();
    }

    public ComputeNode(int id, double alpha, int hopsToClient){
        this(id, hopsToClient > 2 ? 8 : 4, alpha, hopsToClient);
    }

    public int getHopsToClient() {
        return hopsToClient;
    }

    public String getId() {
        return id;
    }

    public int getMaxContainers() {
        return maxContainers;
    }

    public int getRemainderLambdaContainers(){
        return maxContainers - muContainers.size() - lambdaContainers.size();
    }

    // -------- Mu Containers --------

    public int getMaxMu() {
        return maxMu;
    }

    public int getMuContainersSize(){
        return muContainers.size();
    }

    public int getRemainderMuContainers(){
        return maxMu - muContainers.size();
    }

    public MuContainer addMuContainer(String muAppId){
        MuContainer mc = null;
        if (muContainers.size() < maxMu) {
            mc = new MuContainer(id + "-" + muAppId);
            muContainers.add(mc);
        }
        return mc;
    }

    public void removeMuContainer(MuContainer mc){ muContainers.remove(mc);}

    // -------- Lambda Containers --------

    public LambdaContainer addLambdaContainer(){
        LambdaContainer lc = null;
        if (lambdaContainers.size() < maxContainers - muContainers.size()){
            lc = new LambdaContainer(id + "-L" + lambdaContainers.size(), serviceRate);
            lambdaContainers.add(lc);
        }
        return lc;
    }

    public void removeLambdaContainer(LambdaContainer lc){
        lambdaContainers.remove(lc);
    }

    public void removeInactive(){
        lambdaContainers.removeIf(lc -> lc.getResidualCapacity() == lc.getServiceRate());
    }

    public LinkedList<LambdaContainer> getLambdaContainers() {
        return lambdaContainers;
    }

    public double getTotalResidualCapacity(){
        double totResidual = 0;
        for (LambdaContainer lambdaContainer : lambdaContainers){
            totResidual += lambdaContainer.getResidualCapacity();
        }
        return totResidual;
    }

    public double getAvailableServiceRate(){
        // Formula in the paper is: serviceRate * (maxContainers - muContainers.size())
        // but the definition states "aggregate of the service rates of all the lambda containers on compute node"
        return serviceRate * lambdaContainers.size();
    }

    public int getLambdaContainersSize() {return lambdaContainers.size();}

    // ----------------

    @Override
    public String toString() {
        return "ComputeNode{" +
                "id='" + id + '\'' +
                ", maxContainers=" + maxContainers +
                ", alpha=" + alpha +
                ", serviceRate=" + serviceRate +
                ", maxMu=" + maxMu +
                ",\n muContainers=" + muContainers +
                ",\n lambdaContainers=" + lambdaContainers +
                "\n}";
    }

}
