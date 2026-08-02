void main() {

    int imageWidth = 256;
    int imageHeight = 256;

    try (PrintWriter writer = new PrintWriter("image.ppm")) {
        writer.print("P3\n" + imageWidth + " " + imageHeight + "\n255\n");

        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                double r = (double) i / (imageWidth - 1);
                double g = (double) j / (imageHeight - 1);
                double b = 0.0;

                int ir = (int) (255.999 * r);
                int ig = (int) (255.999 * g);
                int ib = (int) (255.999 * b);


                writer.println(ir + " " + ig + " " + ib);
            }
        }
    }
    catch (IOException e) {
        e.printStackTrace();
    }
}
