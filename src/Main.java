void main() {

    double aspectRatio = (double)16/9;
    int imageWidth = 400;

    //int imageWidth = 256;
    //int imageHeight = 256;

    int imageHeight = (int) Math.max((int) imageWidth/aspectRatio, 1);

    double focalLength = 1.0;
    double viewportHeight = 2.0;
    double viewportWidth = viewportHeight * (double)(imageWidth/imageHeight);

    Vec3 cameraOrigin = new Vec3(0, 0, 0);

    Vec3 viewportX = new Vec3(viewportWidth, 0, 0);
    Vec3 viewportY = new Vec3(0, -viewportHeight, 0);

    Vec3 pixelDeltaX = VectorOperations.scale((double) 1 /imageWidth, viewportX);
    Vec3 pixelDeltaY = VectorOperations.scale((double) 1/imageHeight, viewportY);

    Vec3 topLeftPixel =
            VectorOperations.subtract(VectorOperations.subtract(cameraOrigin, new Vec3(0, 0, focalLength)),
                VectorOperations.add(
                    VectorOperations.scale(0.5, viewportX),
                    VectorOperations.scale(0.5, viewportY)));

    Vec3 topLeftCentre = VectorOperations.add(topLeftPixel,
            VectorOperations.scale(0.5,
                    VectorOperations.add(pixelDeltaX, pixelDeltaY)));



    try (PrintWriter writer = new PrintWriter("image.ppm")) {
        writer.print("P3\n" + imageWidth + " " + imageHeight + "\n255\n");
        System.out.println("P3\n" + imageWidth + " " + imageHeight + "\n255\n");
        for (int j = 0; j < imageHeight; j++) {
            System.out.println("Scan Line Remaining: " + (imageHeight-j));
            for (int i = 0; i < imageWidth; i++) {

                Vec3 xOffset = VectorOperations.scale(i, pixelDeltaX);
                Vec3 yOffset = VectorOperations.scale(j, pixelDeltaY);
                Vec3 pixelCentre =VectorOperations.add(VectorOperations.add(topLeftCentre, xOffset), yOffset);
                Vec3 rayDirection = VectorOperations.subtract(pixelCentre, cameraOrigin);

                Ray r = new Ray(cameraOrigin, rayDirection);
                Vec3 pixelColour = colourRay(r);


                //double r = (double) i / (imageWidth - 1);
                //double g = (double) j / (imageHeight - 1);
                //double b = 0.0;

                //Vec3 pixelColour = new Vec3(r, g, b);

                // Output pixel color to file using the helper
                ColourUtils.writeColour(writer, pixelColour);
            }
        }
        System.out.println("Done!");
    }
    catch (IOException e) {
        e.printStackTrace();
    }
}

Vec3 colourRay(Ray r){
    Vec3 unit_direction = VectorOperations.scale((double)1/r.direction().magnitude, r.direction());
    double a = 0.5*(unit_direction.y()+1.0);
    return VectorOperations.add(VectorOperations.scale((1.0-a), new Vec3(1.0, 1.0, 1.0)), VectorOperations.scale(a, new Vec3(0.5, 0.7, 1.0)));
}
