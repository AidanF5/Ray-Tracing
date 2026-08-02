import java.io.PrintWriter;

public class ColourUtils {

    public static void writeColour(PrintWriter out, Vec3 pixelColour){
        double r = pixelColour.x();
        double g = pixelColour.y();
        double b = pixelColour.z();

        int ir = (int) (255.999 * r);
        int ig = (int) (255.999 * g);
        int ib = (int) (255.999 * b);

        out.println(ir + " " + ig + " " + ib);
    }
}
