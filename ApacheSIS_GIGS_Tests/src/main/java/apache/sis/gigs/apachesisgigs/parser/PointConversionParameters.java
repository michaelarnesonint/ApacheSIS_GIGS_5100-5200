package apache.sis.gigs.apachesisgigs.parser;

public class PointConversionParameters {

    private int testNumber;
    private String testName;
    private P111Crs fromCrs;
    private double[] fromPoint;
    private P111Crs toCrs;
    private double[] toPoint;
    private boolean expectedConversionFailure = false;
    
    public PointConversionParameters(int testNumber, String testName, P111Crs fromCrs, double[] fromPoint, P111Crs toCrs, double[] toPoint, boolean expectedConversionFailure) {
        this.testNumber = testNumber;
        this.testName = testName;
        this.fromCrs = fromCrs;
        this.fromPoint = fromPoint;
        this.toCrs = toCrs;
        this.toPoint = toPoint;
        this.expectedConversionFailure = expectedConversionFailure;
    }

    public int getTestNumber() {
        return testNumber;
    }

    public String getTestName() {
        return testName;
    }

    public double[] getFromPoint() {
        return fromPoint;
    }

    public P111Crs getFromCrs() {
        return fromCrs;
    }

    public P111Crs getToCrs() {
        return toCrs;
    }

    public double[] getToPoint() {
        return toPoint;
    }

    public boolean isExpectedConversionFailure() {
        return expectedConversionFailure;
    }

}
