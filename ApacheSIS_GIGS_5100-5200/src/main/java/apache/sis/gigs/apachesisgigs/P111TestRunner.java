package apache.sis.gigs.apachesisgigs;

import apache.sis.gigs.apachesisgigs.parser.P111Crs;
import apache.sis.gigs.apachesisgigs.parser.P111FileParser;
import apache.sis.gigs.apachesisgigs.parser.PointConversionParameters;
import apache.sis.gigs.apachesisgigs.utils.MathTransformUtils;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import javax.measure.Unit;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.FactoryException;

public class P111TestRunner {

    private CoordinateOperationAuthorityFactory opAuthorityfactory;
    private CoordinateOperationFactory opFactory;
    private CRSAuthorityFactory crsAuthorityFactory;
    private CRSFactory crsFactory;

    public P111TestRunner() throws Exception {
        opAuthorityfactory = ServiceLoader.load(CoordinateOperationAuthorityFactory.class).iterator().next();
        opFactory = ServiceLoader.load(CoordinateOperationFactory.class).iterator().next();
        crsAuthorityFactory = ServiceLoader.load(CRSAuthorityFactory.class).iterator().next();
        crsFactory = ServiceLoader.load(CRSFactory.class).iterator().next();
    }

    public void runTest(File p111File) throws Exception {
        P111FileParser parser = new P111FileParser();
        parser.parseFile(p111File);
        List<PointConversionParameters> conversionParametersList = parser.getConversionParameters();
        for (PointConversionParameters currentConversionParameters : conversionParametersList) {
            try {
                P111Crs fromCrs = currentConversionParameters.getFromCrs();
                P111Crs toCrs = currentConversionParameters.getToCrs();
                CrsConversionTestParameters fromCrsParameters = createCrsParamters(fromCrs, parser);
                CrsConversionTestParameters toCrsParameters = createCrsParamters(toCrs, parser);
                performPointConversion(currentConversionParameters, fromCrsParameters, toCrsParameters);
            } catch (Exception ex) {
                System.out.println("Test Failed for " + currentConversionParameters.getTestName());
                throw ex;
            }
        }

    }

    private void performPointConversion(PointConversionParameters conversionParameters, CrsConversionTestParameters fromCrsParameters, CrsConversionTestParameters toCrsParameters) throws Exception {
        double[] fromPoint = conversionParameters.getFromPoint();

        System.out.println("Starting conversion test " + conversionParameters.getTestName());
        MathTransform transform = createTransformation(fromCrsParameters, toCrsParameters);

        if (conversionParameters.isExpectedConversionFailure()) {
            System.out.println("Start point: " + Arrays.toString(fromPoint));
            System.out.println("Expecting conversion to fail");
            try {
                double[] transformPoint = MathTransformUtils.transformPoint(transform, fromPoint);
                System.out.println("Test Failed:Conversion did not fail as expected, calculate point: " + Arrays.toString(transformPoint));
                throw new ExpectedConversionFailException();
            } catch (ExpectedConversionFailException ex) {
                throw ex;
            } catch (Exception ex) {
                System.out.println("Conversion failed as expected");
            }
            System.out.println("Conversion test " + conversionParameters.getTestName() + " passed");
            return;
        }

        double[] convertedPoint = MathTransformUtils.transformPoint(transform, fromPoint);
        double tolerance = toCrsParameters.getTolerance();
        double[] expectedPoint = conversionParameters.getToPoint();

        System.out.println("Start point: " + Arrays.toString(fromPoint));
        System.out.println("Expected point: " + Arrays.toString(expectedPoint));

        System.out.println("Converted point : " + Arrays.toString(convertedPoint));
        for (int i = 0; i < convertedPoint.length; i++) {
            double diff = Math.abs(convertedPoint[i] - expectedPoint[i]);
            if (diff > tolerance) {
                System.out.println("Test Failed: Converted point not withing tolerance " + tolerance);
                throw new Exception("Converted point not within tolerance " + tolerance + " for test " + conversionParameters.getTestName()
                        + ". Expected " + Arrays.toString(expectedPoint) + " but calculated " + Arrays.toString(convertedPoint));
            }
        }

        double roundTripTolerance = fromCrsParameters.getRoundTripTolerance();
        double[] roundTripPoint = MathTransformUtils.transformPoint(transform.inverse(), convertedPoint);
        System.out.println("Round trip point : " + Arrays.toString(roundTripPoint));

        for (int i = 0; i < roundTripPoint.length; i++) {
            double diff = Math.abs(roundTripPoint[i] - fromPoint[i]);
            if (diff > roundTripTolerance) {
                System.out.println("Test Failed: Rount trip point not withing tolerance " + roundTripTolerance);
                throw new Exception("Round trip converted point not within tolerance " + roundTripTolerance + " for test " + conversionParameters.getTestName()
                        + ". Expected " + Arrays.toString(fromPoint) + " but calculated " + Arrays.toString(roundTripPoint));
            }
        }

        System.out.println("Conversion test " + conversionParameters.getTestName() + " passed");
    }

    private CrsConversionTestParameters createCrsParamters(P111Crs p111Crs, P111FileParser parser) throws Exception {
        String epsgCode = p111Crs.getCrsEpsgCode();
        String wkt = p111Crs.getCrsWkt();
        String wgs84TransformCode = p111Crs.getWgs84TransformCode();

        CoordinateReferenceSystem crs;
        if (epsgCode != null && !epsgCode.isEmpty()) {
            crs = crsAuthorityFactory.createCoordinateReferenceSystem("EPSG:" + epsgCode);
        } else {
            crs = crsFactory.createFromWKT(wkt);
        }
        double tolerance;
        double roundTripTolerance;
        if (isGeographicCrs(crs)) {
            tolerance = parser.getGeographicTolerance();
            roundTripTolerance = parser.getRoundTripGeographicTolerance();
        } else {
            tolerance = parser.getCartesianTolerance();
            roundTripTolerance = parser.getRoundTripCartesianTolerance();
        }

        return new CrsConversionTestParameters(crs, wgs84TransformCode, tolerance, roundTripTolerance);
    }

    private boolean isGeographicCrs(CoordinateReferenceSystem crs) {
        Unit unit = crs.getCoordinateSystem().getAxis(0).getUnit();
        Unit systemUnit = unit.getSystemUnit();
        return "rad".equalsIgnoreCase(systemUnit.getSymbol());
    }

    private MathTransform createTransformation(CrsConversionTestParameters fromCrsParameters, CrsConversionTestParameters toCrsParameters) throws Exception {
        CoordinateReferenceSystem fromCrs = fromCrsParameters.getCrs();
        String fromWgs84TransformCode = fromCrsParameters.getWgs84TransformationCode();
        CoordinateReferenceSystem toCrs = toCrsParameters.getCrs();
        String toWgs84TransformCode = toCrsParameters.getWgs84TransformationCode();

        if (fromWgs84TransformCode == null && toWgs84TransformCode == null) {
            CoordinateOperation operation = findOperation(fromCrs, toCrs);
            return operation.getMathTransform();
        }
        if (fromWgs84TransformCode != null && toWgs84TransformCode == null) {
            CoordinateOperation operation = opAuthorityfactory.createCoordinateOperation(fromWgs84TransformCode);
            MathTransform transform = operation.getMathTransform();
            return transform;
        }
        if (fromWgs84TransformCode == null && toWgs84TransformCode != null) {
            CoordinateOperation operation = opAuthorityfactory.createCoordinateOperation(toWgs84TransformCode);
            MathTransform transform = operation.getMathTransform().inverse();
            return transform;
        }
        //this case will never be reached 5100 and 5200 files dont specify a source and target transformation
        throw new UnsupportedOperationException("Concatentating wgs 84 transformations is not supported at this time");
    }

    private CoordinateOperation findOperation(final CoordinateReferenceSystem sourceCRS,
            final CoordinateReferenceSystem targetCRS) throws FactoryException {

        return opFactory.createOperation(sourceCRS, targetCRS);
    }

}
