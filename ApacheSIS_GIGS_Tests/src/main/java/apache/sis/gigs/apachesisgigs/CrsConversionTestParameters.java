package apache.sis.gigs.apachesisgigs;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class CrsConversionTestParameters {

    private final CoordinateReferenceSystem crs;
    private final String wgs84TransformationCode;
    private final double tolerance;
    private final double roundTripTolerance;
    
    public CrsConversionTestParameters(CoordinateReferenceSystem crs, String wgs84TransformationCode, double tolerance, double roundTripTolerance) {
        this.crs = crs;
        this.wgs84TransformationCode = wgs84TransformationCode;
        this.tolerance = tolerance;
        this.roundTripTolerance = roundTripTolerance;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public String getWgs84TransformationCode() {
        return wgs84TransformationCode;
    }

    public double getTolerance() {
        return tolerance;
    }

    public double getRoundTripTolerance() {
        return roundTripTolerance;
    }
    
    
    
}
