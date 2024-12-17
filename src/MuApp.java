public class MuApp extends Application {
    MuContainer container;

    public MuApp(int id, int invocationRate, int functionData) {
        super("MU" + id , invocationRate, functionData);
    }

    @Override
    public void changeType(Orchestrator o) {
        // TODO: ask orchestrator to switch from mu to lambda
    }

    public void invokeFunction() {
        container.performFunction();
    }

    public void assignContainer(MuContainer muContainer){
        if (muContainer != null){
            container = muContainer;
        }
    }

    @Override
    public String toString() {
        return "MuApp{" +
                "id=" + getId() +
                ", container=" + container +
                '}';
    }
}
