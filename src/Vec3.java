public class Vec3 {
    public double[] vec = new double[3];
    public double magnitude;

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

}
