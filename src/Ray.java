public class Ray {

    private Vec3 o;
    private Vec3 d;

    public Ray(){
        o = new Vec3();
        d = new Vec3();
    }

    public Ray(Vec3 orign, Vec3 direction){
        o = orign;
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
}
