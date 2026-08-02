import java.io.PrintWriter;

public class ColourUtils {

    public static void writeColour(PrintWriter out, Vec3 pixelColour){
        double r = pixelColour.x();
        double g = pixelColour.y();
        double b = pixelColour.z();

        Interval i = new Interval(0, 0.999);
        int ir = (int) (256 * i.clamp(r));
        int ig = (int) (256 * i.clamp(g));
        int ib = (int) (256 * i.clamp(b));

        out.println(ir + " " + ig + " " + ib);
    }
}
