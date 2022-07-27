package apache.sis.gigs.apachesisgigs.parser;

import apache.sis.gigs.apachesisgigs.utils.SexagesimalConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.util.FactoryException;

public class P111FileParser {

    private static final String DEFAULT_DEGREE_UNIT_WKT = "ANGLEUNIT[\"degree\", 0.017453292519943295]";

    private double cartesianTolerance = 0.03;
    private double geographicTolerance = 0.0000003;
    private double roundTripCartesianTolerance = 0.006;
    private double roundTripGeographicTolerance = 0.00000006;

    private List<PointConversionParameters> conversionParameters = new ArrayList<>();
    private Map<String, String> unitTypeToUnitWkt = new LinkedHashMap<>();

    private DatumAuthorityFactory datumFactory;

    private Map<Integer, P111Crs.Builder> crsNumberToCrsBuilderMap = new LinkedHashMap<>();
    private Map<Integer, P111Crs> crsNumberToCrsMap;
    //handle transformation order
    private Map<Integer, String[]> crsNumberToCrsRef = new LinkedHashMap<>();
    private Map<Integer, String> crsNumberToTransform = new LinkedHashMap<>();

    public P111FileParser() throws FactoryException {
        this.datumFactory = ServiceLoader.load(DatumAuthorityFactory.class).iterator().next();
    }

    public void parseFile(File p111File) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(p111File)))) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] lineSegments = getLineSegments(line);
                if (lineSegments.length == 0) {
                    continue;
                }
                if (lineSegments.length < 5) {
                    continue;
                }
                String lineID = lineSegments[0] + "," + lineSegments[1] + "," + lineSegments[2] + "," + lineSegments[3];
                if (lineID.equals("HC,1,1,0")) {
                    parseUnitLine(lineSegments);
                } else if (lineID.equals("HC,1,3,0")) {
                    parseCrsRefIdLine(lineSegments);
                } else if (lineID.equals("HC,1,4,0")) {
                    parseCrsDetailsLine(lineSegments);
                } else if (lineID.equals("HC,1,4,3")) {
                    parseBaseGeographicCrsDetails(lineSegments);
                } else if (lineID.equals("HC,1,4,4")) {
                    parseDatumLine(lineSegments);
                } else if (lineID.equals("HC,1,4,5")) {
                    parsePrimeMeridianLine(lineSegments);
                } else if (lineID.equals("HC,1,4,6")) {
                    parseEllipsoidLine(lineSegments);
                } else if (lineID.equals("HC,1,5,0")) {
                    parseMapProjectionLine(lineSegments);
                } else if (lineID.equals("HC,1,5,1")) {
                    parseProjectionMethodLine(lineSegments);
                } else if (lineID.equals("HC,1,5,2")) {
                    parseProjectionParameterLine(lineSegments);
                } else if (lineID.equals("HC,1,6,0")) {
                    parseCoordinateSystemDetailsLine(lineSegments);
                } else if (lineID.equals("HC,1,6,1")) {
                    parseCoordinateSystemAxisDetails(lineSegments);
                } else if (lineID.equals("H1,0,2,0")) {
                    parseTolerances(lineSegments);
                } else if (lineID.equals("HC,1,7,0")) {
                    parseTransformation(lineSegments);
                } else if (lineID.equals("HC,1,8,1")) {
                    parseTransformationDetails(lineSegments);
                } else if (lineID.equals("HC,1,9,0")) {
                    parseConversionPointLine(lineSegments);
                }
            }
        }
    }

    private void buildCrsMap() {
        if (crsNumberToCrsMap != null) {
            return;
        }
        this.crsNumberToCrsMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, P111Crs.Builder> currentEntry : crsNumberToCrsBuilderMap.entrySet()) {
            P111Crs.Builder builder = currentEntry.getValue();
            P111Crs currentCrs = builder.build();
            this.crsNumberToCrsMap.put(currentEntry.getKey(), currentCrs);
        }
    }

    private void parseTolerances(String[] lineSegments) {
        String attributeName = lineSegments[4];
        if (attributeName.equals("Cartesian Tolerance")) {
            cartesianTolerance = parseToleranceValues(lineSegments);
        } else if (attributeName.equals("Geographic Tolerance")) {
            geographicTolerance = parseToleranceValues(lineSegments);
        } else if (attributeName.equals("Round Trip Cartesian Tolerance")) {
            roundTripCartesianTolerance = parseToleranceValues(lineSegments);
        } else if (attributeName.equals("Round Trip Geographic Tolerance")) {
            roundTripGeographicTolerance = parseToleranceValues(lineSegments);
        }
    }

    private double parseToleranceValues(String[] lineSegments) {
        String valueString = lineSegments[6];
        if (!valueString.contains(" ")) {
            return Double.parseDouble(valueString);
        }
        valueString = valueString.split(" ")[0].trim();
        return Double.parseDouble(valueString);
    }

    private void parseConversionPointLine(String[] lineSegments) {
        buildCrsMap();
        int testNumber = Integer.parseInt(lineSegments[5]);
        String testName = lineSegments[6];
        int fromCRSNumber = Integer.parseInt(lineSegments[7]);
        double fromX = Double.parseDouble(lineSegments[8]);
        double fromY = Double.parseDouble(lineSegments[9]);
        double fromZ = 0;
        if (!lineSegments[10].isEmpty()) {
            fromZ = Double.parseDouble(lineSegments[10]);
        }
        double[] fromPoint = new double[]{fromX, fromY, fromZ};
        boolean expectedToFail = false;
        int toCRSNumber = Integer.parseInt(lineSegments[11]);
        double[] toPoint;
        if (lineSegments[12].equals("NULL") || lineSegments[13].equals("NULL")) {
            toPoint = new double[]{Double.NaN, Double.NaN, Double.NaN};
            expectedToFail = true;
        } else {
            double toX = Double.parseDouble(lineSegments[12]);
            double toY = Double.parseDouble(lineSegments[13]);
            double toZ = 0;
            if (lineSegments.length > 14 && !lineSegments[14].isEmpty()) {
                toZ = Double.parseDouble(lineSegments[14]);
            }
            toPoint = new double[]{toX, toY, toZ};
        }
        P111Crs fromCrs = crsNumberToCrsMap.get(fromCRSNumber);
        P111Crs toCrs = crsNumberToCrsMap.get(toCRSNumber);
        conversionParameters.add(new PointConversionParameters(testNumber, testName, fromCrs, fromPoint, toCrs, toPoint, expectedToFail));
    }

    //HC,1,7,0
    private void parseTransformation(String[] lineSegments) {
        String wgs84TransformCode = lineSegments[6];
        if (wgs84TransformCode.isEmpty()) {
            return;
        }
        int crsNumber = Integer.valueOf(lineSegments[5]);
        this.crsNumberToTransform.put(crsNumber, wgs84TransformCode);
    }

    //HC,1,8,1
    private void parseTransformationDetails(String[] lineSegments) {
        int crsNumber = Integer.valueOf(lineSegments[5]);
        String wgs84TransformCode = crsNumberToTransform.get(crsNumber);
        if (wgs84TransformCode == null || wgs84TransformCode.isEmpty()) {
            return;
        }
        String fromCrsEpsgCode = lineSegments[7];
        String fromCrsEpsgName = lineSegments[8];

        //try to find matching source crs based on previous crs number ref entries
        Set<Map.Entry<Integer, String[]>> entrySet = crsNumberToCrsRef.entrySet();
        for (Map.Entry<Integer, String[]> currentEntry : entrySet) {
            Integer currentCrsNumber = currentEntry.getKey();
            String[] currentCrsRefLineSegments = currentEntry.getValue();
            String currentCrsEpsgCode = currentCrsRefLineSegments[6];
            String currentCrsEpsgName = currentCrsRefLineSegments[7];
            if (currentCrsEpsgCode.equals(fromCrsEpsgCode) && currentCrsEpsgName.equals(fromCrsEpsgName)) {
                P111Crs.Builder currentBuilder = getCrsBuilder(currentCrsNumber);
                currentBuilder.setTransformation(wgs84TransformCode);
                return;
            }
        }
    }

    //HC,1,3,0
    private void parseCrsRefIdLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String epsgCode = lineSegments[6];
        String epsgName = lineSegments[7];
        crsBuilder.addCrsRefImplicitIdentification(epsgCode, epsgName);

        int crsNumber = Integer.valueOf(lineSegments[5]);
        this.crsNumberToCrsRef.put(crsNumber, lineSegments);
    }

    //HC,1,4,0
    private void parseCrsDetailsLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String epsgCode = lineSegments[6];
        int crsType = Integer.valueOf(lineSegments[7]);
        String crsName = lineSegments[9];
        crsBuilder.addCrsDetails(epsgCode, crsName, crsType);
    }

    //HC 1,4,3
    private void parseBaseGeographicCrsDetails(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String baseCrsName = lineSegments[8];
        crsBuilder.addBaseGeographicCrsDetails(baseCrsName);
    }

    //HC,1,4,4
    private void parseDatumLine(String[] lineSegments) throws NoSuchAuthorityCodeException, FactoryException {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String datumCode = lineSegments[6];
        Datum datum = this.datumFactory.createDatum(datumCode);
        String datumName = datum.getName().getCode();
        crsBuilder.addDatumName(datumName);
    }

    //HC,1,4,5
    private void parsePrimeMeridianLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String primeMeridianName = lineSegments[7];
        String primeMeridianValue = lineSegments[8];
        String unitType = lineSegments[10];
        String unitWkt = unitTypeToUnitWkt.get(unitType);
        if (unitWkt == null) {
            throw new IllegalArgumentException("Can't find unit type " + unitType);
        }
        primeMeridianValue = convertUnitValueIfNeeded(unitType, primeMeridianValue);
        crsBuilder.addPrimeMeridian(primeMeridianName, primeMeridianValue, unitWkt);
    }

    //HC,1,4,6
    private void parseEllipsoidLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String ellipsoidName = lineSegments[7];
        String semiMajorAxis = lineSegments[8];
        String inverseFlattening = lineSegments[11];
        String unitType = lineSegments[10];
        String unitWkt = unitTypeToUnitWkt.get(unitType);
        if (unitWkt == null) {
            throw new IllegalArgumentException("Can't find unit type " + unitType);
        }
        crsBuilder.addEllipsoid(ellipsoidName, semiMajorAxis, inverseFlattening, unitWkt);
    }

    //HC,1,5,0
    private void parseMapProjectionLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String mapProjectionName = lineSegments[7];
        crsBuilder.addMapProjection(mapProjectionName);
    }

    //HC,1,5,1
    private void parseProjectionMethodLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String projectionEpsgCode = lineSegments[6];
        String coordinateOperationMethodName = lineSegments[7];
        crsBuilder.addProjectionMethod(projectionEpsgCode, coordinateOperationMethodName);
    }

    //HC,1,5,2
    private void parseProjectionParameterLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        String parameterName = lineSegments[4];
        String parameterEpsgCode = lineSegments[6];
        String parameterValue = lineSegments[7];
        String unitType = lineSegments[9];
        String unitWkt = unitTypeToUnitWkt.get(unitType);
        if (unitWkt == null) {
            throw new IllegalArgumentException("Can't find unit type " + unitType);
        }
        parameterValue = convertUnitValueIfNeeded(unitType, parameterValue);
        crsBuilder.addProjectionParameters(parameterEpsgCode, parameterName, parameterValue, unitWkt);
    }

    private String convertUnitValueIfNeeded(String unitType, String parametersValue) {
        if (!unitType.equalsIgnoreCase("sexagesimal DMS")) {
            return parametersValue;
        }
        SexagesimalConverter sexagesimalConverter = new SexagesimalConverter();
        
        double dms = Double.parseDouble(parametersValue);
        double decimalDegrees = sexagesimalConverter.convertSexagesimalToDecimalDegrees(dms);
        return String.valueOf(decimalDegrees);
    }

    //HC,1,6,0
    private void parseCoordinateSystemDetailsLine(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);

        int coordinateSystemType = Integer.valueOf(lineSegments[8]);
        String coordinateSystemTypeName = lineSegments[9];
        int dimensions = Integer.valueOf(lineSegments[10]);
        crsBuilder.addCoordinateSystemDetails(coordinateSystemType, coordinateSystemTypeName, dimensions);
    }

    //HC,1,6,1 
    private void parseCoordinateSystemAxisDetails(String[] lineSegments) {
        P111Crs.Builder crsBuilder = getCrsBuilder(lineSegments);
        int coordinateOrder = Integer.valueOf(lineSegments[6]);
        String axisName = lineSegments[8];
        String axisOrientation = lineSegments[9];
        String axisAbbreviation = lineSegments[10];
        String unitType = lineSegments[12];
        String unitWkt = unitTypeToUnitWkt.get(unitType);
        if (unitWkt == null) {
            throw new IllegalArgumentException("Can't find unit type " + unitType);
        }
        crsBuilder.addCoordinateSystemAxisDetails(coordinateOrder, axisName, axisOrientation, axisAbbreviation, unitWkt);
    }

    //HC,1,1,0
    private void parseUnitLine(String[] lineSegments) {
        if (isStandardDegreeUnit(lineSegments)) {
            //for max precision use pre defined degree unit
            unitTypeToUnitWkt.put("degree", DEFAULT_DEGREE_UNIT_WKT);
            return;
        }
        if (isSexagesimalDMS(lineSegments)) {
            //for max precision use pre defined degree unit
            String unitId = lineSegments[6];
            unitTypeToUnitWkt.put(unitId, DEFAULT_DEGREE_UNIT_WKT);
            return;
        }

        String unitValue = "1";
        String unitId = lineSegments[6];
        String unitType = lineSegments[7].toUpperCase();

        if (!lineSegments[10].isEmpty() || !lineSegments[11].isEmpty() || !lineSegments[12].isEmpty() || !lineSegments[13].isEmpty()) {
            double a = getABCDVariable(lineSegments[10]);
            double b = getABCDVariable(lineSegments[11]);
            double c = getABCDVariable(lineSegments[12]);
            double d = getABCDVariable(lineSegments[13]);
            if (a == 0 && b == 0 && c == 0 && d == 0) {
                unitValue = "1";
            } else {
                double y = (a + b) / (c + d);
                unitValue = String.valueOf(y);
            }
        }
        String unitWkt = unitType + "UNIT[\"" + unitId + "\", " + unitValue + "]";
        unitTypeToUnitWkt.put(unitId, unitWkt);
    }

    private boolean isSexagesimalDMS(String[] lineSegments) {
        return lineSegments[7].equals("angle") && lineSegments[6].equals("sexagesimal DMS");
    }

    private boolean isStandardDegreeUnit(String[] lineSegments) {
        if (!lineSegments[7].equals("angle") || !lineSegments[6].equals("degree")) {
            return false;
        }
        double a = getABCDVariable(lineSegments[10]);
        double b = getABCDVariable(lineSegments[11]);
        double c = getABCDVariable(lineSegments[12]);
        double d = getABCDVariable(lineSegments[13]);
        if (a != 0 || c != 180 || d != 0) {
            return false;
        }
        if (Math.abs(Math.PI - b) < 0.001) {
            return true;
        }
        return false;
    }

    private double getABCDVariable(String stringValue) {
        if (stringValue.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(stringValue);
    }

    private String[] getLineSegments(String line) {
        UnicodeUnescaper unicodeUnescapter = new UnicodeUnescaper();
        String[] split = line.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
            if (split[i].contains("\\u")) {
                split[i] = unicodeUnescapter.translate(split[i]);
            }
        }

        return split;
    }

    private P111Crs.Builder getCrsBuilder(String[] lineSegments) {
        int crsNumber = Integer.valueOf(lineSegments[5]);
        return getCrsBuilder(crsNumber);
    }

    private P111Crs.Builder getCrsBuilder(int crsNumber) {
        P111Crs.Builder builder = this.crsNumberToCrsBuilderMap.get(crsNumber);
        if (builder == null) {
            builder = P111Crs.builder();
            this.crsNumberToCrsBuilderMap.put(crsNumber, builder);
        }
        return builder;
    }

    public List<Integer> getCrsNumbers() {
        return new ArrayList<>(crsNumberToCrsMap.keySet());
    }

    public double getCartesianTolerance() {
        return cartesianTolerance;
    }

    public double getGeographicTolerance() {
        return geographicTolerance;
    }

    public double getRoundTripCartesianTolerance() {
        return roundTripCartesianTolerance;
    }

    public double getRoundTripGeographicTolerance() {
        return roundTripGeographicTolerance;
    }

    public List<PointConversionParameters> getConversionParameters() {
        return conversionParameters;
    }
}
