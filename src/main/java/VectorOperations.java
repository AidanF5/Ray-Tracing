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

    public static Vec3 normalise(Vec3 v){
        return scale(1/v.getMag(), v);
    }

    public static Vec3 reflect(Vec3 r, Vec3 n){
        return subtract(r, scale(2*dot(r, n), n));
    }

    public static Vec3 multiplyComponents(Vec3 u, Vec3 v){
        return new Vec3(u.x()*v.x(), u.y()*v.y(), u.z()*v.z());
    }

    public static void copy(Vec3 u, Vec3 v){
        v.update(u.x(), u.y(), u.z());
    }

    public static Vec3 refract(Vec3 v, Vec3 n, double refractiveRatio){
        double cosTheta = Math.min(VectorOperations.dot(VectorOperations.negate(v), n), 1.0);
        Vec3 orthogonal = VectorOperations.scale(refractiveRatio, VectorOperations.add(v, VectorOperations.scale(cosTheta, n)));
        Vec3 parallel = VectorOperations.scale(
                -Math.sqrt(Math.abs(1.0 - (orthogonal.getMag()*orthogonal.getMag()))),
                n
        );
        return VectorOperations.add(orthogonal, parallel);
    }
}
