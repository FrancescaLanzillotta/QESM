public abstract class Application {
    private int invocationRate;
    private int functionData;
    private int stateData;
    private String id;
    public abstract void invokeFunction();
    public abstract void resetAllocation();
    public abstract void changeOperationMode(Orchestrator o);

    public Application(String id, int invocationRate, int functionData, int stateData) {
        this.id = id;
        this.invocationRate = invocationRate;
        this.functionData = functionData;
        this.stateData = stateData;
    }

    public int getInvocationRate() {
        return invocationRate;
    }

    public int getFunctionData() {
        return functionData;
    }

    public int getStateData() {
        return stateData;
    }

    public String getId() {
        return id;
    }

}
