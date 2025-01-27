public class MuApp extends Application {
    private MuContainer container;

    public MuApp(int id, int invocationRate, int functionData, int stateData) {
        super("MU" + id , invocationRate, functionData, stateData);
    }

    public void changeType(Orchestrator o, int stateData) {
        o.switchMuApp(this);
    }

    public void invokeFunction() {
        container.performFunction();
    }

    @Override
    public void resetAllocation() {
        container = null;
    }

    public void assignContainer(MuContainer muContainer){
        if (muContainer != null){
            container = muContainer;
        }
    }

    public MuContainer getContainer() {
        return container;
    }

    @Override
    public String toString() {
        return "MuApp{" +
                "id=" + getId() +
                ", container=" + container +
                '}';
    }
}
