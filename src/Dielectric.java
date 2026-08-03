public class Dielectric extends Material{

    private double refractiveIndex;

    public Dielectric(double index){
        refractiveIndex = index;
    }

    public boolean scatter(Ray r, RayHit hitty, Vec3 colour, Ray scattered){
        VectorOperations.copy(hitty.getColour(), colour);
        double ri = hitty.getFrontFace() ? (1.0/refractiveIndex) : refractiveIndex;
        Vec3 unitDirection = VectorOperations.normalise(r.direction());

        double cosTheta = Math.min(VectorOperations.dot(VectorOperations.negate(unitDirection), hitty.getNormal()), 1.0);
        double sinTheta = Math.sqrt(1.0-(cosTheta*cosTheta));

        boolean cannot_refract = ri * sinTheta > 1.0;
        Vec3 direction;

        if(cannot_refract || (reflectance(cosTheta, ri) > Math.random())){
            direction = VectorOperations.reflect(unitDirection, hitty.getNormal());
        }
        else{
            direction = VectorOperations.refract(unitDirection, hitty.getNormal(), ri);
        }

        Ray tempy = new Ray(hitty.getPoint(), direction);
        scattered.copy(tempy);
        return true;
    }

    private static double reflectance(double cosTheta, double ri){
        double r0 = (1- ri)/(1+ri);
        r0 *= r0;
        return r0 + (1-r0)* Math.pow((1-cosTheta), 5);
    }
}
