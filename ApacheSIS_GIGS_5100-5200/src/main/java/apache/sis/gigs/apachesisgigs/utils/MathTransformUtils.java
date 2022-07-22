package apache.sis.gigs.apachesisgigs.utils;

import java.util.ServiceLoader;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.SingleOperation;

public class MathTransformUtils {

    public static double[] transformPoint(MathTransform transform, double[] xyzValues) throws Exception {
        if (transform.getSourceDimensions() == 3 && transform.getTargetDimensions() == 3) {
            return transform3DPoint(transform, xyzValues);
        } else {
            return transform2DPoint(transform, xyzValues);
        }
    }
    
    private static double[] transform3DPoint(MathTransform transform, double[] xyzValues) throws Exception {
        double[] convertedXyzValues = new double[3];
        transform.transform(xyzValues, 0, convertedXyzValues, 0, 1);
        return convertedXyzValues;
    }
    
    private static double[] transform2DPoint(MathTransform transform, double[] xyzValues) throws Exception {
        double[] xyPoint = new double[]{xyzValues[0], xyzValues[1]};
        double[] convertedXyValues = new double[2];
        transform.transform(xyPoint, 0, convertedXyValues, 0, 1);
        return new double[]{convertedXyValues[0], convertedXyValues[1], xyzValues[2]};
    }
    
    public static MathTransform get3DMathTransform(SingleOperation operation) throws Exception {
        ParameterValueGroup srcp = operation.getParameterValues();
        String oldName = operation.getMethod().getName().getCode();
        String newName = oldName.replace("geog2D", "geog3D");

        MathTransformFactory mtFactory = ServiceLoader.load(MathTransformFactory.class).iterator().next();
        ParameterValueGroup tgtp = mtFactory.getDefaultParameters(newName);
        Ellipsoid sourceEllipsoid = ((GeographicCRS) operation.getSourceCRS()).getDatum().getEllipsoid();
        Ellipsoid targetEllipsoid = ((GeographicCRS) operation.getTargetCRS()).getDatum().getEllipsoid();
        tgtp.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        tgtp.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        tgtp.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        tgtp.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        try {
            tgtp.parameter("X-axis translation").setValue(srcp.parameter("X-axis translation").doubleValue());
        } catch (ParameterNotFoundException ex) {
            //ignore
        }
        try {
            tgtp.parameter("Y-axis translation").setValue(srcp.parameter("Y-axis translation").doubleValue());
        } catch (ParameterNotFoundException ex) {
            //ignore
        }
        try {
            tgtp.parameter("Z-axis translation").setValue(srcp.parameter("Z-axis translation").doubleValue());
        } catch (ParameterNotFoundException ex) {
            //ignore
        }
        try {
            tgtp.parameter("Scale difference").setValue(srcp.parameter("Scale difference").doubleValue());
        } catch (ParameterNotFoundException ex) {

        }
        try {
            tgtp.parameter("X-axis rotation").setValue(srcp.parameter("X-axis rotation").doubleValue());
        } catch (ParameterNotFoundException ex) {

        }
        try {
            tgtp.parameter("Y-axis rotation").setValue(srcp.parameter("Y-axis rotation").doubleValue());
        } catch (ParameterNotFoundException ex) {

        }
        try {
            tgtp.parameter("Z-axis rotation").setValue(srcp.parameter("Z-axis rotation").doubleValue());
        } catch (ParameterNotFoundException ex) {

        }
        return mtFactory.createParameterizedTransform(tgtp);
    }
    
}
