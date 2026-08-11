import java.util.ArrayList;

public class ObjectList {
    private ArrayList<Object> objects = new ArrayList<>();

    public ObjectList(){

    }

    public RayHit getHit(Ray r, Interval interval){
        RayHit closest = new RayHit();
        for (int i = 0; i < objects.size(); i++) {
            RayHit next = objects.get(i).getIntersection(r, interval);
            if(!closest.isValid()){
                closest = next;
            }
            else if(!next.isValid()){

            }
            else if(closest.getT()> next.getT()){
                closest = next;
            }
        }
        return closest;
    }

    public void add(Object o){
        objects.add(o);
    }
}
