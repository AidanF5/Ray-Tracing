public class Interval {
    private double min;
    private double max;

    public Interval(){
        min = Double.NEGATIVE_INFINITY;
        max = Double.POSITIVE_INFINITY;
    }
    public Interval(double min, double max){
        this.min = min;
        this.max = max;
    }
    public double size() {
        return max - min;
    }

    public boolean contains(double x) {
        return min <= x && x <= max;
    }

    public boolean surrounds(double x) {
        return min < x && x < max;
    }
}
