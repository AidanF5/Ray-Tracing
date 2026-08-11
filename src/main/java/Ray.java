public class Ray {

    private Vec3 o;
    private Vec3 d;

    public Ray(){
        o = new Vec3();
        d = new Vec3();
    }

    public Ray(Vec3 origin, Vec3 direction){
        o = origin;
        d = direction;
    }

    public Vec3 origin(){
        return o;
    }

    public Vec3 direction(){
        return d;
    }

    public Vec3 pointAt(double factor){
        return VectorOperations.add(o, VectorOperations.scale(factor, d));
    }

    public void copy(Ray r){
        o = r.origin();
        d = r.direction();
    }
}
