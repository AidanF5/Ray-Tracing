public class Frustrum extends Object{

    //frustrum is built off a cone with a rang eof valid heights
    Vec3 point;
    Vec3 axis;
    float lowerBound; //0 for a cone
    float upperBound;
    boolean caps;
    float angle;

    public Frustrum(float x, float y, float z, float nx, float ny, float nz,
                    float lB, float uB, float angle, boolean caps, Vec3 col, Material mat){
        point = new Vec3(x, y, z);
        axis = VectorOperations.normalise(new Vec3(nx, ny, nz));
        lowerBound = lB;
        upperBound = uB;
        this.angle = angle;
        this.caps = caps;
        colour = col;
        this.mat = mat;
    }

    public RayHit getIntersection(Ray r, Interval interval){
        Vec3 delP = VectorOperations.subtract(r.origin(), point);
        float cosSqrdTheta = (float) Math.pow(Math.cos(angle), 2);
        float a = (float) (Math.pow(VectorOperations.dot(r.direction(), axis), 2) - cosSqrdTheta * VectorOperations.dot(r.direction(), r.direction()));
        float b = 2 * (VectorOperations.dot(r.direction(), axis) * (VectorOperations.dot(delP, axis)) - cosSqrdTheta * VectorOperations.dot(r.direction(), delP));
        float c = (float)(Math.pow(VectorOperations.dot(delP, axis), 2) - cosSqrdTheta * VectorOperations.dot(delP, delP));

        float discrim = (b * b) - (4 * a * c);
        if(discrim < 0){
            return new RayHit();
        }

        float bestT = Float.MAX_VALUE;
        Vec3 bestNorm = null;
        boolean hit = false;


        if(Math.abs(a) > 1e-6f) {
            float sqrtDiscrim = (float) Math.sqrt(discrim);
            float[] roots = {(-b - sqrtDiscrim) / (2*a), (-b + sqrtDiscrim) / (2*a)};
            for (int i = 0; i < 2; i++) {
                if (interval.surrounds(roots[i]) && roots[i] < bestT) {
                    Vec3 intersect = r.pointAt(roots[i]);
                    Vec3 intersectToPoint = VectorOperations.subtract(intersect, point);
                    float h = VectorOperations.dot(intersectToPoint, axis);
                    if (lowerBound <= h && h <= upperBound) {
                        bestT = roots[i];
                        hit = true;
                        bestNorm = VectorOperations.normalise(
                                VectorOperations.subtract(
                                        intersectToPoint, VectorOperations.scale((float) (h / cosSqrdTheta), axis)
                                ));
                    }
                }
            }
        }

        if(caps){
            float dirToAxis = VectorOperations.dot(r.direction(), axis);
            if(Math.abs(dirToAxis) > 1e-6f) {
                Vec3[] capNorms = {VectorOperations.scale(-1, axis), axis};
                float[] capHeights = {lowerBound, upperBound};
                for (int i = 0; i < 2; i++) {
                    if(capHeights[i] == 0){
                        continue;
                    }
                    Vec3 capBase = VectorOperations.add(point, VectorOperations.scale(capHeights[i], axis));
                    float multiplier = VectorOperations.dot(VectorOperations.subtract(capBase, r.origin()), axis) / VectorOperations.dot(r.direction(), axis);
                    if(interval.surrounds(multiplier) && multiplier < bestT){
                        Vec3 intersect = r.pointAt(multiplier);
                        if(Math.pow(VectorOperations.subtract(intersect, capBase).getMag(), 2) <= Math.pow(capHeights[i] * Math.tan(angle), 2)){
                            bestT = multiplier;
                            hit = true;
                            bestNorm = capNorms[i];
                        }
                    }
                }
            }
        }

        if(!hit){
            return new RayHit();
        }

        Vec3 hitPoint = r.pointAt(bestT);
        RayHit hitty = new RayHit(hitPoint, bestNorm, bestT, colour, mat);
        hitty.setFrontFace(bestNorm, r);

        return hitty;
    }

}
