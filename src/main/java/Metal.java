public class Metal extends Material {

    private float fuzz;
    public Metal(float fuzz){
        this.fuzz = fuzz < 1 ? fuzz : 1;
        this.fuzz = Math.abs(this.fuzz);
    }

    public boolean scatter(Ray r, RayHit hitty, Vec3 colour, Ray scattered){
        Vec3 reflected = VectorOperations.normalise(VectorOperations.reflect(r.direction(), hitty.getNormal()));
        reflected = VectorOperations.add(reflected, VectorOperations.scale(fuzz, Vec3.randomUnit()));
        Ray temp = new Ray(hitty.getPoint(), reflected);
        scattered.copy(temp);
        VectorOperations.copy(hitty.getColour(), colour);
        return (VectorOperations.dot(scattered.direction(), hitty.getNormal()) > 0);
    }
}
