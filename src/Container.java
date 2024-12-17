import javax.swing.plaf.multi.MultiSeparatorUI;

public abstract class Container {
    private String id;
    private int usageCounter;

    public Container(String id) {
        this.id = id;
        usageCounter = 0;
    }

    public void performFunction(){
        usageCounter++;
        System.out.println("Usages: " + usageCounter);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Container{" +
                "id='" + id + '\'' +
                '}';
    }
}
