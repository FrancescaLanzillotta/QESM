public class MuApp extends Application {
    private MuContainer container;

    public MuApp(int id, int invocationRate, int functionData, int stateData) {
        super("MU" + id , invocationRate, functionData, stateData);
    }
    @Override
    public void invokeFunction() {
        container.performFunction();
    }

    public MuContainer getContainer() {
        return container;
    }

    public void assignMuContainer(MuContainer muContainer){
        if (muContainer != null){
            container = muContainer;
        }
    }

    @Override
    public void resetAllocation() {
        container = null;
    }

    @Override
    public void changeOperationMode(Orchestrator o) {
        o.switchMuApp(this);
    }

    @Override
    public String toString() {
        return "MuApp{" +
                "id=" + getId() +
                ", container=" + container +
                '}';
    }
}
