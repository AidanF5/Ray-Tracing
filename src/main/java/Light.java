public class Light extends Material{

    public Light(){

    }


    @Override
    public boolean scatter(Ray r, RayHit hitty, Vec3 col, Ray scattered){
        return false;
    }

    @Override
    public boolean emitted(){
        return true;
    }
}
