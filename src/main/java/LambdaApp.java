import java.util.HashMap;

public class LambdaApp extends Application {
    private HashMap<LambdaContainer, Double> containersWeights;

    public LambdaApp(int id, int invocationRate, int functionData, int stateData) {
        super("LAMBDA" + id , invocationRate, functionData, stateData);
        containersWeights = new HashMap<>();
    }
    @Override
    public void invokeFunction() {
        // TODO: Pick a container based on the weights and do something with it (a counter maybe?)
    }

    public HashMap<LambdaContainer, Double> getContainersWeights() {
        return containersWeights;
    }

    public void assignLambdaContainer(LambdaContainer lc, double weight){
        if (containersWeights.containsKey(lc)){
            containersWeights.put(lc, weight + containersWeights.get(lc));
        } else {
            containersWeights.put(lc, weight);
        }
    }

    @Override
    public void resetAllocation() {
        containersWeights.clear();
    }

    @Override
    public void changeOperationMode(Orchestrator o) {
        o.switchLambdaApp(this);
    }

    @Override
    public String toString() {
        return "LambdaApp{" +
                "id=" + getId() +
                ", invocationRate=" + getInvocationRate() +
                ", functionData=" + getFunctionData() +
                ", stateData=" + getStateData() +
                ", containerWeights=" + containersWeights +
                '}';
    }

}
