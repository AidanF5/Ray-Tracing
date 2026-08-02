public class VectorOperations {

    public static Vec3 negate(Vec3 v){
        return new Vec3(-v.x(), -v.y(), -v.z());
    }

    public static Vec3 add(Vec3 v1, Vec3 v2){
        return new Vec3(v1.x() + v2.x(), v1.y() + v2.y(), v1.z() + v2.z());
    }
    public static Vec3 subtract(Vec3 v1, Vec3 v2){
        return new Vec3(v1.x() - v2.x(), v1.y() - v2.y(), v1.z() - v2.z());
    }
    public static Vec3 scale(double factor, Vec3 v){
        return new Vec3(v.x() * factor, v.y() * factor, v.z() * factor);
    }

    public static double dot(Vec3 v1, Vec3 v2){
        return (v1.x() * v2.x()) + (v1.y() * v2.y()) + (v1.z() * v2.z());
    }

    public static Vec3 cross(Vec3 v1, Vec3 v2){
        return new Vec3(v1.y() * v2.z() - v1.z() * v2.y(),
                v1.z() * v2.x() - v1.x() * v2.z(),
                v1.x() * v2.y() - v1.y() * v2.x());
    }

    public Vec3 normalise(Vec3 v){
        return scale(1/v.getMag(), v);
    }

}
