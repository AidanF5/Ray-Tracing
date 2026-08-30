public class Sphere extends Object{

    private Vec3 centre;
    private double radius;
    private Material mat;

    public Sphere(float x, float y, float z, double r, Vec3 c, Material m){
        centre = new Vec3(x, y, z);
        radius = r;
        colour = c;
        mat = m;
    }

    @Override
    public RayHit getIntersection(Ray r, Interval interval) {
        Vec3 oc = VectorOperations.subtract(centre, r.origin());
        double a = r.direction().getMag()*r.direction().getMag();
        double h = VectorOperations.dot(r.direction(), oc);
        double c = (oc.getMag() * oc.getMag()) - (radius * radius);

        double discrim = h*h-a*c;
        if(discrim < 0){
            return new RayHit();
        }
        double sqrtDiscrim = Math.sqrt(discrim);

        float root = (float)((h - sqrtDiscrim) / a);
        if (!interval.surrounds(root)) {
            root = (float)((h + sqrtDiscrim) / a);
            if (!interval.surrounds(root))
                return new RayHit();
        }
        Vec3 point = r.pointAt(root);
        Vec3 outwardNormal = VectorOperations.scale((float)(1/radius),VectorOperations.subtract(point, centre));
        RayHit hit = new RayHit(point, outwardNormal, root, colour, mat);
        hit.setFrontFace(outwardNormal, r);
        return hit;
    }
}
