public class Disk extends Object{
    private Plane p;
    private Vec3 centre;
    float radius;

    public Disk(float nx, float ny, float nz, float d,
                float x, float y, float z,
                float r, Vec3 c, Material mat){
        p = new Plane(nx, ny, nz, d, c, mat);
        this.mat = mat;
        colour = c;
        centre = new Vec3(x, y, z);
        radius = r;
    }

    public RayHit getIntersection(Ray r, Interval interval){
        RayHit hitty = p.getIntersection(r, interval);
        if(!hitty.isValid()){
            return hitty;
        }
        if(VectorOperations.distance(centre, hitty.getPoint()) <= radius){ //replace with a distance operation
            //hit is in the disk
            return hitty;
        }
        return new RayHit();

    }
}
