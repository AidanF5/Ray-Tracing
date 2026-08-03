import java.io.IOException;
import java.io.PrintWriter;

public class Camera {
    private double aspectRatio = 1.0;
    private int imageWidth = 100;
    private int samplesPerPixel = 100;
    private int maxBounces = 100;

    private int imageHeight;
    private Vec3 cameraOrigin;
    private Vec3 topLeftCentre;
    private Vec3 pixelDeltaX;
    private Vec3 pixelDeltaY;
    private double pixelSamplesScales;

    public Camera(){

    }

    public Camera(double aspectRatio, int imageWidth){
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
        pixelSamplesScales = 1.0 / samplesPerPixel;

        double focalLength = 1.0;
        double viewportHeight = 2.0;
        double viewportWidth = viewportHeight *((double)imageWidth/imageHeight);

        cameraOrigin = new Vec3(0, 0, 0);

        Vec3 viewportX = new Vec3(viewportWidth, 0, 0);
        Vec3 viewportY = new Vec3(0, -viewportHeight, 0);

        pixelDeltaX = VectorOperations.scale((double) 1 /imageWidth, viewportX);
        pixelDeltaY = VectorOperations.scale((double) 1/imageHeight, viewportY);

        Vec3 topLeftPixel =
                VectorOperations.subtract(VectorOperations.subtract(cameraOrigin, new Vec3(0, 0, focalLength)),
                        VectorOperations.add(
                                VectorOperations.scale(0.5, viewportX),
                                VectorOperations.scale(0.5, viewportY)));

        topLeftCentre = VectorOperations.add(topLeftPixel,
                VectorOperations.scale(0.5,
                        VectorOperations.add(pixelDeltaX, pixelDeltaY)));

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


        Vec3 unit_direction = VectorOperations.scale((double)1/r.direction().getMag(), r.direction());
        double a = 0.5*(unit_direction.y()+1.0);
        return VectorOperations.add(VectorOperations.scale((1.0-a), new Vec3(1.0, 1.0, 1.0)), VectorOperations.scale(a, new Vec3(0.5, 0.7, 1.0)));
    }

    private Ray getRay(int i, int j){
        Vec3 offset = sampleSquare();
        Vec3 xOffset = VectorOperations.scale(i+offset.x(), pixelDeltaX);
        Vec3 yOffset = VectorOperations.scale(j+ offset.y(), pixelDeltaY);
        Vec3 pixelCentre =VectorOperations.add(VectorOperations.add(topLeftCentre, xOffset), yOffset);
        Vec3 rayDirection = VectorOperations.subtract(pixelCentre, cameraOrigin);

        Ray r = new Ray(cameraOrigin, rayDirection);
        return r;
    }
    private Vec3 sampleSquare(){
        return new Vec3(Math.random() - 0.5, Math.random() - 0.5, 0);
    }
}
