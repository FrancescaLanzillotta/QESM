public abstract class Application {
    private int invocationRate;
    private int functionData;
    private String id;
    public abstract void invokeFunction();

    public Application(String id, int invocationRate, int functionData) {
        this.id = id;
        this.invocationRate = invocationRate;
        this.functionData = functionData;
    }

    public int getInvocationRate() {
        return invocationRate;
    }

    public int getFunctionData() {
        return functionData;
    }

    public String getId() {
        return id;
    }


}
