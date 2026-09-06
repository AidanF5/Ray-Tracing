public class Cylinder extends Object{
    private Vec3 axis;
    private Vec3 centre;
    private float radius;
    private float height;
    private boolean caps;

    public Cylinder(float x, float y, float z, float nx, float ny, float nz, float r, float h, boolean caps, Vec3 c, Material mat){
        centre = new Vec3(x, y, z);
        axis = VectorOperations.normalise(new Vec3(nx, ny, nz));
        radius = r;
        height = h;
        this.caps = caps;
        colour = c;
        this.mat = mat;
    }

    public RayHit getIntersection1(Ray r, Interval interval){
        Vec3 aVec = VectorOperations.subtract(r.direction(), VectorOperations.scale(VectorOperations.dot(r.direction(), axis), axis));
        Vec3 delP = VectorOperations.subtract(r.origin(), centre);
        Vec3 cVec = VectorOperations.subtract(delP, VectorOperations.scale(VectorOperations.dot(delP, axis), axis));
        float a = VectorOperations.dot(aVec, aVec);
        float b = 2 * VectorOperations.dot(aVec, cVec);
        float c = VectorOperations.dot(cVec, cVec) - (radius*radius);
        float discrim = (b * b) - (4 * a * c);
        if(discrim < 0){
            return new RayHit();
        }
        boolean firstValid = true;
        float root =(float) (-b - Math.sqrt(discrim))/(2*a);
        if(!interval.surrounds(root)){
            firstValid = false;
            root = (float)(-b + Math.sqrt(discrim))/(2*a);
            if(!interval.surrounds(root)){
                return new RayHit();
            }
        }
        do{
            Vec3 point = r.pointAt(root);
            float y = VectorOperations.dot(VectorOperations.subtract(point, centre), axis);
            if(y >= -height/2 && y<= height/2){
                //The intersection is valid;
            }
        }while(firstValid = false);
        return new RayHit();
    }

    public RayHit getIntersection(Ray r, Interval interval) {
        Vec3 aVec = VectorOperations.subtract(r.direction(), VectorOperations.scale(VectorOperations.dot(r.direction(), axis), axis));
        Vec3 delP = VectorOperations.subtract(r.origin(), centre);
        Vec3 cVec = VectorOperations.subtract(delP, VectorOperations.scale(VectorOperations.dot(delP, axis), axis));

        float a = VectorOperations.dot(aVec, aVec);
        float b = 2 * VectorOperations.dot(aVec, cVec);
        float c = VectorOperations.dot(cVec, cVec) - (radius * radius);
        float discrim = (b * b) - (4 * a * c);

        if (discrim < 0) {
            return new RayHit();
        }

        float closestT = Float.MAX_VALUE;
        Vec3 bestNormal = null;
        boolean hitFound = false;

        // Check it hits the cylinder within the height
        if (a > 1e-6f) {
            //not shooting straight down the middle of the cylinder
            float sqrtD = (float) Math.sqrt(discrim);
            float[] roots = { (-b - sqrtD) / (2 * a), (-b + sqrtD) / (2 * a) };

            for (int i = 0; i < 2; i++) {
                if (interval.surrounds(roots[i]) && roots[i] < closestT) {
                    Vec3 point = r.pointAt(roots[i]);
                    float y = VectorOperations.dot(VectorOperations.subtract(point, centre), axis);
                    if (y >= -height / 2.0f && y <= height / 2.0f) {
                        closestT = roots[i];
                        Vec3 projectedY = VectorOperations.scale(y, axis);
                        bestNormal = VectorOperations.normalise(VectorOperations.subtract(VectorOperations.subtract(point, centre), projectedY));
                        hitFound = true;
                    }
                }
            }
        }
        //Check if it hits a cap of the cylinder
        if(caps) {
            float denom = VectorOperations.dot(r.direction(), axis);
            if (Math.abs(denom) > 1e-6) {
                float[] capY = {height / 2.0f, -height / 2.0f};
                Vec3[] capNormals = {axis, VectorOperations.scale(-1, axis)};

                for (int i = 0; i < 2; i++) {
                    float tCap = (capY[i] - VectorOperations.dot(delP, axis)) / denom;

                    if (interval.surrounds(tCap) && tCap < closestT) {
                        Vec3 pCap = r.pointAt(tCap);
                        Vec3 capCenter = VectorOperations.add(centre, VectorOperations.scale(capY[i], axis));
                        Vec3 distVector = VectorOperations.subtract(pCap, capCenter);

                        if (VectorOperations.dot(distVector, distVector) <= (radius * radius)) {
                            closestT = tCap;
                            bestNormal = capNormals[i];
                            hitFound = true;
                        }
                    }
                }
            }
        }
        if (!hitFound) {
            return new RayHit();
        }

        Vec3 hitPoint = r.pointAt(closestT);
        RayHit hit = new RayHit(hitPoint, bestNormal, closestT, colour, mat);
        hit.setFrontFace(bestNormal, r);

        return hit;
    }
}
