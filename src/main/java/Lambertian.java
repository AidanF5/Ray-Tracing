public class Lambertian extends Material{


    public Lambertian(){

    }

    @Override
    public boolean scatter(Ray r, RayHit hitty, Vec3 col, Ray scattered){
        Vec3 scatterDirection = VectorOperations.add(hitty.getNormal(), Vec3.randomUnit());
        if(scatterDirection.nearZero()){
            scatterDirection = hitty.getNormal();
        }
        Ray temp = new Ray(hitty.getPoint(), scatterDirection);
        scattered.copy(temp);
        VectorOperations.copy(hitty.getColour(), col);
        return true;
    }
}
