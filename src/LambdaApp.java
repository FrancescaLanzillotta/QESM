import java.util.HashMap;
import java.util.Map;

public class LambdaApp extends Application {
    private int stateData;
    private HashMap<LambdaContainer, Double> containerWeights;

    public LambdaApp(int id, int invocationRate, int functionData, int stateData) {
        super("LAMBDA" + id , invocationRate, functionData);
        this.stateData = stateData;
        containerWeights = new HashMap<>();
    }

    public void assignLambdaContainer(LambdaContainer lc, double weight){
        containerWeights.put(lc, weight);
    }

    public void invokeFunction() {
        // TODO: Pick a container based on the weights and do something with it (a counter maybe?)
    }

    @Override
    public void changeType(Orchestrator o) {
        // TODO: Ask orchestrator to switch from lambda to mu
    }

    public int getStateData() {
        return stateData;
    }

    public HashMap<LambdaContainer, Double> getContainerWeights() {
        return containerWeights;
    }

    @Override
    public String toString() {
        return "LambdaApp{" +
                "id=" + getId() +
                ", invocationRate=" + getInvocationRate() +
                ", functionData=" + getFunctionData() +
                ", stateData=" + stateData +
                ", containerWeights=" + containerWeights +
                '}';
    }

}
