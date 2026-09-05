import java.io.IOException;
import java.io.PrintWriter;

public class Camera {
    private float aspectRatio = 1.0f;
    private int imageWidth = 100;
    private int samplesPerPixel = 100;
    private int maxBounces = 30;
    //private int samplesPerPixel = 10;
    //private int maxBounces = 10;
    private float vfov = 20;
    private Vec3 lookFrom = new Vec3(13, 2, 3);
    private Vec3 lookAt = new Vec3(0, 0, 0);
    private Vec3 vup = new Vec3(0, 1, 0);
    private float defocusAngle = 0.6f;
    private float focusDist = 10.0f;

    private int imageHeight;
    private Vec3 cameraOrigin;
    private Vec3 topLeftCentre;
    private Vec3 pixelDeltaX;
    private Vec3 pixelDeltaY;
    private float pixelSamplesScales;
    private Vec3 u, v, w;
    private Vec3 defocusDiskX;
    private Vec3 defocusDiskY;

    public Camera(){

    }

    public Camera(float aspectRatio, int imageWidth){
        this.aspectRatio = aspectRatio;
        this.imageWidth = imageWidth;
    }

    public void render(ObjectList world){
        initialise();
        try (PrintWriter writer = new PrintWriter("image.ppm")) {
            writer.print("P3\n" + imageWidth + " " + imageHeight + "\n255\n");
            System.out.println("P3\n" + imageWidth + " " + imageHeight + "\n255\n");
            for (int j = 0; j < imageHeight; j++) {
                System.out.println("Scan Line Remaining: " + (imageHeight-j));
                for (int i = 0; i < imageWidth; i++) {

                    Vec3 col = new Vec3(0, 0, 0);
                    for (int k = 0; k < samplesPerPixel; k++) {
                        Ray r = getRay(i, j);
                        col = VectorOperations.add(col, colourRay(r, world, maxBounces));
                    }

                    // Output pixel color to file using the helper
                    ColourUtils.writeColour(writer, VectorOperations.scale(pixelSamplesScales, col));
                }
            }
            System.out.println("Done!");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initialise(){
        imageHeight = (int) Math.max((double)imageWidth/aspectRatio, 1);
        pixelSamplesScales = (float)(1.0 / samplesPerPixel);

        cameraOrigin = lookFrom;

        //double focalLength = VectorOperations.subtract(lookFrom, lookAt).getMag();
        float theta = (float)Math.toRadians(vfov);
        float h = (float)Math.tan(theta/2);
        //double viewportHeight = 2 * h * focalLength;
        float viewportHeight = 2 * h * focusDist;
        float viewportWidth = viewportHeight *((float)imageWidth/imageHeight);

        w = VectorOperations.normalise(VectorOperations.subtract(lookFrom, lookAt));
        u = VectorOperations.normalise(VectorOperations.cross(vup, w));
        v = VectorOperations.cross(w,  u);

        Vec3 viewportX = VectorOperations.scale(viewportWidth, u);
        Vec3 viewportY = VectorOperations.scale(viewportHeight, VectorOperations.negate(v));

        pixelDeltaX = VectorOperations.scale((float) 1 /imageWidth, viewportX);
        pixelDeltaY = VectorOperations.scale((float) 1/imageHeight, viewportY);

        Vec3 topLeftPixel =
                VectorOperations.subtract(VectorOperations.subtract(cameraOrigin, VectorOperations.scale(focusDist, w)),
                        VectorOperations.add(
                                VectorOperations.scale(0.5f, viewportX),
                                VectorOperations.scale(0.5f, viewportY)));

        topLeftCentre = VectorOperations.add(topLeftPixel,
                VectorOperations.scale(0.5f,
                        VectorOperations.add(pixelDeltaX, pixelDeltaY)));

        float defocusRadius = focusDist * (float) Math.tan(Math.toRadians(defocusAngle/2.0));
        defocusDiskX = VectorOperations.scale(defocusRadius, u);
        defocusDiskY = VectorOperations.scale(defocusRadius, v);


    }
    private Vec3 colourRay(Ray r, ObjectList world, int depth){
        if(depth <=0){
            return new Vec3(0, 0, 0);
        }
        RayHit hitty = world.getHit(r, new Interval(0.001, Double.POSITIVE_INFINITY));
        if(hitty.isValid()){
            Ray scattered = new Ray();
            Vec3 colour = new Vec3(0, 0, 0);
            if(hitty.getMaterial().scatter(r, hitty, colour, scattered)){
                return VectorOperations.multiplyComponents(colour, colourRay(scattered, world, depth-1));
            }
            //Vec3 direction = VectorOperations.add(hitty.getNormal(), Vec3.randomUnit());
            //return VectorOperations.scale(0.5, colourRay(new Ray(hitty.getPoint(), direction), world, depth-1));
            return new Vec3(0, 0, 0);
        }


        Vec3 unit_direction = VectorOperations.scale((float)1/r.direction().getMag(), r.direction());
        float a = (float)(0.5*(unit_direction.y()+1.0));
        return VectorOperations.add(VectorOperations.scale((1.0f-a), new Vec3(1.0f, 1.0f, 1.0f)), VectorOperations.scale(a, new Vec3(0.5f, 0.7f, 1.0f)));
    }

    private Ray getRay(int i, int j){
        Vec3 offset = sampleSquare();
        Vec3 xOffset = VectorOperations.scale(i+offset.x(), pixelDeltaX);
        Vec3 yOffset = VectorOperations.scale(j+ offset.y(), pixelDeltaY);
        Vec3 pixelCentre = VectorOperations.add(VectorOperations.add(topLeftCentre, xOffset), yOffset);
        Vec3 rayOrigin = (defocusAngle <= 0) ? cameraOrigin : defocusDiskSample();
        Vec3 rayDirection = VectorOperations.subtract(pixelCentre, rayOrigin);

        Ray r = new Ray(rayOrigin, rayDirection);
        return r;
    }
    private Vec3 sampleSquare(){
        return new Vec3((float)(Math.random() - 0.5),(float)(Math.random() - 0.5), 0);
    }

    private Vec3 defocusDiskSample(){
        Vec3 p = Vec3.randomUnitDisk();
        return VectorOperations.add(cameraOrigin, VectorOperations.add(VectorOperations.scale(p.x(), defocusDiskX), VectorOperations.scale(p.y(), defocusDiskY)));
    }
}
