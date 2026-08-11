import java.io.PrintWriter;

public class ColourUtils {

    private static double gamma = 2.2;

    public static void writeColour(PrintWriter out, Vec3 pixelColour){
        double r = pixelColour.x();
        double g = pixelColour.y();
        double b = pixelColour.z();

        r = linearToGamma(r);
        g = linearToGamma(g);
        b = linearToGamma(b);

        Interval i = new Interval(0, 0.999);
        int ir = (int) (256 * i.clamp(r));
        int ig = (int) (256 * i.clamp(g));
        int ib = (int) (256 * i.clamp(b));

        out.println(ir + " " + ig + " " + ib);
    }

    private static double linearToGamma(double linearComponent){
        if (linearComponent > 0){
            return Math.pow(linearComponent, (double)1/gamma);
        }
        return 0;
    }
}
