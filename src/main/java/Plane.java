public class Plane extends Object{
    private float a;
    private float b;
    private float c;
    private float d;

    public Plane(float a, float b, float cn, float d, Vec3 c, Material m){
        this.a = a;
        this.b = b;
        this.c = cn;
        this.d = d;
        mat = m;
        colour = c;
    }

    public RayHit getIntersection(Ray r, Interval interval){
        Vec3 n = new Vec3(a, b, c);
        if(VectorOperations.dot(r.direction(), n) == 0){
            return new RayHit();
        }
        Vec3 dir = r.direction();
        Vec3 o = r.origin();
        float multiplier = (d - (a * o.x()) - (b * o.y()) - (c * o.z()))/((a * dir.x()) + (b * dir.y()) + (c * dir.z()));
        if(!interval.surrounds(multiplier)){
            return new RayHit();
        }
        Vec3 pointAt = r.pointAt(multiplier);
        Vec3 normal = new Vec3(a, b, c);
        Vec3 norm = VectorOperations.normalise(normal);
        RayHit hit = new RayHit(pointAt, normal, multiplier, colour, mat);
        hit.setFrontFace(norm, r);
        return hit;
    }



}
