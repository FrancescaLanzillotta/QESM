public class MuApp extends Application {
    private MuContainer container;

    public MuApp(int id, int invocationRate, int functionData) {
        super("MU" + id , invocationRate, functionData);
    }

    public void changeType(Orchestrator o, int stateData) {
        o.switchMuApp(this, stateData);
    }

    public void invokeFunction() {
        container.performFunction();
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
