public class Vec3 {
    public double[] vec = new double[3];
    private double magnitude;

    public Vec3(){
        vec[0] = 0;
        vec[1] = 0;
        vec[2] = 0;
        magnitude = 0;
    }

    public Vec3(double e0, double e1, double e2){
        vec[0] = e0;
        vec[1] = e1;
        vec[2] = e2;
        magnitude = calculate_magnitude();
    }

    public double x(){
        return vec[0];
    }

    public double y(){
        return vec[1];
    }

    public double z(){
        return vec[2];
    }

    public double getMag(){
        return magnitude;
    }

    private double calculate_magnitude(){
        return Math.sqrt(vec[0]* vec[0] + vec[1]* vec[1] + vec[2]* vec[2]);
    }

    public static Vec3 random(){
        return new Vec3(Math.random(), Math.random(), Math.random());
    }
    public static Vec3 random(double min, double max){
        return new Vec3((min + (max-min)*Math.random()), (min + (max-min)*Math.random()), (min + (max-min)*Math.random()));
    }
    public static Vec3 randomUnit(){
        while(true){
            Vec3 p = random(-1, 1);
            double lensq = p.getMag()*p.getMag();
            if (1e-160 < lensq && lensq <= 1.0) {
                return VectorOperations.scale((double)1/p.getMag(), p); // Normalize to surface
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
        double s = 1e-8;
        return (Math.abs(vec[0])<s) && (Math.abs(vec[1])<s) && (Math.abs(vec[2])<s);
    }
    public void update(double x, double y, double z){
        vec[0] = x;
        vec[1] = y;
        vec[2] = z;
    }
}
