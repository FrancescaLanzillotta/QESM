public class LambdaContainer extends Container{
    private int serviceRate;
    private double residualCapacity;

    public LambdaContainer(String id, int serviceRate) {
        super(id);
        this.serviceRate = serviceRate;
        residualCapacity = serviceRate;
    }

    public int getServiceRate() {
        return serviceRate;
    }

    public void useResources(double resources){
        if (resources > 0) {
            if (resources <= residualCapacity){
                residualCapacity -= resources;
            }
        }
    }

    public void freeResources(double resources){
        if (resources > 0){
            if (residualCapacity + resources <= serviceRate){
                residualCapacity += resources;
            } else {
                residualCapacity = serviceRate;
            }
        }
    }

    public double getResidualCapacity() {
        return residualCapacity;
    }


    @Override
    public String toString() {
        return "LambdaContainer{" +
                "id=" + getId() +
                ", serviceRate=" + serviceRate +
                ", availResources=" + residualCapacity +
                '}';
    }
}
