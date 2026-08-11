public class RayHit {
    private Vec3 point;
    private Vec3 normal;
    private double t;
    private Vec3 colour;
    private boolean valid;
    private boolean frontFace;
    private Material mat;

    public RayHit(){
        valid = false;
    }

    public RayHit(Vec3 p, Vec3 n, double t, Vec3 c, Material m){
        point = p;
        normal = n;
        this.t = t;
        colour = c;
        valid = true;
        mat = m;
    }

    public boolean isValid() {
        return valid;
    }

    public double getT() {
        return t;
    }

    public Vec3 getColour() {
        return colour;
    }

    public Vec3 getNormal() {
        return normal;
    }

    public Vec3 getPoint() {
        return point;
    }

    public boolean getFrontFace(){
        return frontFace;
    }

    public Material getMaterial(){
        return mat;
    }

    public void setFrontFace(Vec3 outwardNormal, Ray r){
        frontFace = VectorOperations.dot(r.direction(), outwardNormal) < 0;
        normal = frontFace ? outwardNormal : VectorOperations.negate(outwardNormal);
    }
}
