import java.util.HashMap;
import java.util.Map;

public class LambdaApp extends Application {
    private HashMap<LambdaContainer, Double> containerWeights;

    public LambdaApp(int id, int invocationRate, int functionData, int stateData) {
        super("LAMBDA" + id , invocationRate, functionData, stateData);
        containerWeights = new HashMap<>();
    }

    public void assignLambdaContainer(LambdaContainer lc, double weight){
        if (containerWeights.containsKey(lc)){
            containerWeights.put(lc, weight + containerWeights.get(lc));
        } else {
            containerWeights.put(lc, weight);
        }
    }

    public void invokeFunction() {
        // TODO: Pick a container based on the weights and do something with it (a counter maybe?)
    }

    @Override
    public void resetAllocation() {
        containerWeights.clear();
    }


    public HashMap<LambdaContainer, Double> getContainerWeights() {
        return containerWeights;
    }

    public void changeType(Orchestrator o) {
        o.switchLambdaApp(this);
    }

    @Override
    public String toString() {
        return "LambdaApp{" +
                "id=" + getId() +
                ", invocationRate=" + getInvocationRate() +
                ", functionData=" + getFunctionData() +
                ", stateData=" + getStateData() +
                ", containerWeights=" + containerWeights +
                '}';
    }

}
