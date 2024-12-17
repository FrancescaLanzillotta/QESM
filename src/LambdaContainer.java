public class LambdaContainer extends Container{
    private int serviceRate;

    public LambdaContainer(String id, int serviceRate) {
        super(id);
        this.serviceRate = serviceRate;
    }

    public int getServiceRate() {
        return serviceRate;
    }

    @Override
    public String toString() {
        return "LambdaContainer{" +
                "id=" + getId() +
                ", serviceRate=" + serviceRate +
                '}';
    }
}
