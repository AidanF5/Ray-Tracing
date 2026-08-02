public abstract class Object {
    protected Vec3 colour;

    public abstract RayHit getIntersection(Ray r, Interval interval);
}
