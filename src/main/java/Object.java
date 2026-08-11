public abstract class Object {
    protected Vec3 colour;
    protected Material mat;

    public abstract RayHit getIntersection(Ray r, Interval interval);

    public Material getMaterial(){
        return mat;
    }
}
