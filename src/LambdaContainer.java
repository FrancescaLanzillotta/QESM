public class LambdaContainer extends Container{
    private int serviceRate;
    private double availResources;

    public LambdaContainer(String id, int serviceRate) {
        super(id);
        this.serviceRate = serviceRate;
        availResources = serviceRate;
    }

    public int getServiceRate() {
        return serviceRate;
    }

    public void useResources(double resources){
        if (resources > 0) {
            if (resources <= availResources){
                availResources -= resources;
            }
        }
    }

    public void freeResources(double resources){
        if (resources > 0){
            if (availResources + resources <= serviceRate){
                availResources += resources;
            } else {
                availResources = serviceRate;
            }
        }
    }

    public double getAvailResources() {
        return availResources;
    }


    @Override
    public String toString() {
        return "LambdaContainer{" +
                "id=" + getId() +
                ", serviceRate=" + serviceRate +
                ", availResources=" + availResources +
                '}';
    }
}
