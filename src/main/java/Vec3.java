public class Vec3 {
    public float[] vec = new float[3];
    private float magnitude;

    public Vec3(){
        vec[0] = 0;
        vec[1] = 0;
        vec[2] = 0;
        magnitude = 0;
    }

    public Vec3(float e0, float e1, float e2){
        vec[0] = e0;
        vec[1] = e1;
        vec[2] = e2;
        magnitude = calculate_magnitude();
    }

    public float x(){
        return vec[0];
    }

    public float y(){
        return vec[1];
    }

    public float z(){
        return vec[2];
    }

    public float getMag(){
        return magnitude;
    }

    private float calculate_magnitude(){
        return (float)Math.sqrt(vec[0]* vec[0] + vec[1]* vec[1] + vec[2]* vec[2]);
    }

    public static Vec3 random(){
        return new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random());
    }
    public static Vec3 random(double min, double max){
        return new Vec3((float)(min + (max-min)*Math.random()), (float)(min + (max-min)*Math.random()), (float)(min + (max-min)*Math.random()));
    }
    public static Vec3 randomUnit(){
        while(true){
            Vec3 p = random(-1, 1);
            double lensq = p.getMag()*p.getMag();
            if (1e-160 < lensq && lensq <= 1.0) {
                return VectorOperations.scale((float)1/p.getMag(), p); // Normalize to surface
            }
        }
    }
    public static Vec3 randomHemisphere(Vec3 n){
        Vec3 unitSphere = randomUnit();
        if(VectorOperations.dot(unitSphere, n)>0.0){
            return unitSphere;
        }
        return VectorOperations.negate(unitSphere);
    }

    public boolean nearZero(){
        float s = (float)1e-8;
        return (Math.abs(vec[0])<s) && (Math.abs(vec[1])<s) && (Math.abs(vec[2])<s);
    }
    public void update(float x, float y, float z){
        vec[0] = x;
        vec[1] = y;
        vec[2] = z;
    }

    public static Vec3 randomUnitDisk(){
        while(true){
            Vec3 p = Vec3.random(-1, 1);
            p.vec[2] = 0;
            if(p.x()*p.x()+p.y()*p.y() < 1){
                return p;
            }
        }
    }
}
