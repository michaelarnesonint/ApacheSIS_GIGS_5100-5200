package apache.sis.gigs.apachesisgigs.parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class P111Crs {

    private String crsEpsgCode;
    private String wgs84TransformCode;
    private String crsWkt;

    private P111Crs(Builder builder) {
        this.crsEpsgCode = builder.crsEpsgCode;
        this.wgs84TransformCode = builder.wgs84TransformCode;
        this.crsWkt = builder.crsWkt;
    }

    public String getCrsEpsgCode() {
        return crsEpsgCode;
    }

    public String getWgs84TransformCode() {
        return wgs84TransformCode;
    }

    public String getCrsWkt() {
        return crsWkt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final int PROJECTED_CRS_TYPE_CODE = 1;
        private static final int GEOGRAPHIC_2D_CRS_TYPE_CODE = 2;
        private static final int GEOGRAPHIC_3D_CRS_TYPE_CODE = 3;
        private static final int GEOCENTRIC_CRS_TYPE_CODE = 4;
        private static final int ENGINEERING_CRS_TYPE_CODE = 6;

        private String crsWkt;
        private String wgs84TransformCode;
        private String crsEpsgCode;

        private String crsName;
        private String baseGeographicCrsName;
        private String datumName;
        private String ellipsoid;
        private String primeMeridian;
        private Integer crsTypeCode = null;

        private String mapProjectionName;
        private String projectionMethod;
        private List<String> projectionParameters = new ArrayList<>();

        private String coordinateSystemLine1;
        private String[] coordinateSystemAxisDetails;
        private String[] coordinateSystemAxisUnits;

        //HC,1,3,0
        public Builder addCrsRefImplicitIdentification(String epsgCode, String crsName) {
            this.crsEpsgCode = epsgCode;
            this.crsName = crsName;
            return this;
        }

        //HC,1,4,0
        public Builder addCrsDetails(String epsgCode, String crsName, int crsTypeCode) {
            this.crsEpsgCode = epsgCode;
            this.crsName = crsName;
            this.crsTypeCode = crsTypeCode;
            return this;
        }

        //HC,1,4,3
        public Builder addBaseGeographicCrsDetails(String baseCrsName) {
            this.baseGeographicCrsName = baseCrsName;
            return this;
        }

        //HC,1,4,4
        public Builder addDatumName(String datumName) {
            this.datumName = datumName;
            return this;
        }

        //HC,1,4,5
        public Builder addPrimeMeridian(String primeMeridianName, String greenwichLongitude, String unitWkt) {
            this.primeMeridian = "PRIMEM[\"" + primeMeridianName + "\", " + greenwichLongitude + ", " + unitWkt + "]";
            return this;
        }

        //HC,1,4,6
        public Builder addEllipsoid(String ellipsoidName, String semiMajorAxis, String inverseFlattening, String unitWkt) {
            //ELLIPSOID["WGS 84", 6378137.0, 298.257223563, LENGTHUNIT["metre", 1]]],
            this.ellipsoid = "ELLIPSOID[\"" + ellipsoidName + "\", " + semiMajorAxis + ", " + inverseFlattening + ", " + unitWkt + "]";
            return this;
        }

        //HC,1,5,0
        public Builder addMapProjection(String mapProjectionName) {
            this.mapProjectionName = mapProjectionName;
            return this;
        }

        //HC,1,5,1
        public Builder addProjectionMethod(String projectionEpsgCode, String coordinateOperationMethodName) {
            this.projectionMethod = "METHOD[\"" + coordinateOperationMethodName + "\"";
            if (projectionEpsgCode != null && !projectionEpsgCode.isEmpty()) {
                projectionMethod += ", ID[\"EPSG\"," + projectionEpsgCode + "]";
            }
            this.projectionMethod += "]";
            return this;
        }

        //HC,1,5,2
        public Builder addProjectionParameters(String parameterEpsgCode, String parameterName, String parameterValue, String unitWkt) {
            String currentParameter = "PARAMETER[\"" + parameterName + "\", " + parameterValue + ", " + unitWkt;
            if (parameterEpsgCode != null && !parameterEpsgCode.isEmpty()) {
                currentParameter += ", ID[\"EPSG\", " + parameterEpsgCode + "]";
            }
            currentParameter += "]";
            projectionParameters.add(currentParameter);
            return this;
        }

        //HC,1,6,0
        public Builder addCoordinateSystemDetails(int coordinateSystemType, String coordinateSystemTypeName, int dimensions) {
            //  CS[Cartesian, 2],
            this.coordinateSystemLine1 = "CS[" + coordinateSystemTypeName + ", " + String.valueOf(dimensions) + "]";
            this.coordinateSystemAxisDetails = new String[dimensions];
            this.coordinateSystemAxisUnits = new String[dimensions];
            return this;
        }

        //HC,1,6,1 
        public Builder addCoordinateSystemAxisDetails(int coordinateOrder, String axisName, String axisOrientation, String axisAbbreviation, String unitWkt) {
            String axisWkt = "AXIS[\"" + axisName;
            if (axisAbbreviation != null && !axisAbbreviation.isEmpty()) {
                axisWkt += " (" + axisAbbreviation + ")";
            }
            axisWkt += "\", " + axisOrientation + ", ORDER[" + String.valueOf(coordinateOrder) + "]";
            this.coordinateSystemAxisDetails[coordinateOrder - 1] = axisWkt;
            this.coordinateSystemAxisUnits[coordinateOrder - 1] = unitWkt;
            return this;
        }

        public Builder setTransformation(String wgs84TransformCode) {
            this.wgs84TransformCode = wgs84TransformCode;
            return this;
        }

        public P111Crs build() {
            if (null == crsTypeCode) {
                crsWkt = buildGeographic2DCrsWkt();
            } else {
                switch (crsTypeCode) {
                    case PROJECTED_CRS_TYPE_CODE:
                        crsWkt = buildProjecedCrsWkt();
                        break;
                    case GEOGRAPHIC_2D_CRS_TYPE_CODE:
                        crsWkt = buildGeographic2DCrsWkt();
                        break;
                    case GEOGRAPHIC_3D_CRS_TYPE_CODE:
                        crsWkt = buildGeographic3DCrsWkt();
                        break;
                    case GEOCENTRIC_CRS_TYPE_CODE:
                        crsWkt = buildGeoCentricCrsWkt();
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported crs type " + crsTypeCode);
                }
            }
            return new P111Crs(this);
        }

        private String buildGeographic2DCrsWkt() {
            StringBuilder sb = new StringBuilder();
            if (crsName == null || crsName.isEmpty()) {
                throw new IllegalArgumentException("CRS Name not specified");
            }
            sb.append("GEODCRS[\"").append(crsName).append("\",\n");
            if (datumName == null || datumName.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append("DATUM[\"").append(datumName).append("\",\n");
            if (ellipsoid == null || ellipsoid.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append(ellipsoid).append("],\n");
            if (primeMeridian != null && !primeMeridian.isEmpty()) {
                sb.append(primeMeridian);
            }
            sb.append(",\n");
            if (coordinateSystemLine1 == null || coordinateSystemLine1.isEmpty()) {
                throw new IllegalArgumentException("Coordinate System not specified");
            }
            sb.append(coordinateSystemLine1);
            if (coordinateSystemAxisDetails.length > 0) {
                sb.append(",\n");
                Set<String> uniqueAxisUnits = new LinkedHashSet<>();
                for (String currentAxisUnit : coordinateSystemAxisUnits) {
                    uniqueAxisUnits.add(currentAxisUnit);
                }
                if (uniqueAxisUnits.size() == 1) {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append("],\n");
                    }
                    sb.append(uniqueAxisUnits.iterator().next());
                } else {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append(", ").append(coordinateSystemAxisUnits[i]).append("]");
                        if (i < coordinateSystemAxisDetails.length - 1) {
                            sb.append(",\n");
                        }
                    }
                }
            }
            if (crsEpsgCode != null && !crsEpsgCode.isEmpty()) {
                sb.append(",\n").append("ID[\"EPSG\", ").append(crsEpsgCode).append("]");
            }
            sb.append("]");
            return sb.toString();
        }

        private String buildGeographic3DCrsWkt() {
            StringBuilder sb = new StringBuilder();
            if (crsName == null || crsName.isEmpty()) {
                throw new IllegalArgumentException("CRS Name not specified");
            }
            sb.append("GEODCRS[\"").append(crsName).append("\",\n");
            if (datumName == null || datumName.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append("DATUM[\"").append(datumName).append("\",\n");
            if (ellipsoid == null || ellipsoid.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append(ellipsoid).append("],\n");
            if (primeMeridian != null && !primeMeridian.isEmpty()) {
                sb.append(primeMeridian);
            }
            sb.append(",\n");
            if (coordinateSystemLine1 == null || coordinateSystemLine1.isEmpty()) {
                throw new IllegalArgumentException("Coordinate System not specified");
            }
            sb.append(coordinateSystemLine1);
            if (coordinateSystemAxisDetails.length > 0) {
                sb.append(",\n");
                Set<String> uniqueAxisUnits = new LinkedHashSet<>();
                for (String currentAxisUnit : coordinateSystemAxisUnits) {
                    uniqueAxisUnits.add(currentAxisUnit);
                }
                if (uniqueAxisUnits.size() == 1) {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append("],\n");
                    }
                    sb.append(uniqueAxisUnits.iterator().next());
                } else {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append(", ").append(coordinateSystemAxisUnits[i]).append("]");
                        if (i < coordinateSystemAxisDetails.length - 1) {
                            sb.append(",\n");
                        }
                    }
                }
            }
            if (crsEpsgCode != null && !crsEpsgCode.isEmpty()) {
                sb.append(",\n").append("ID[\"EPSG\", ").append(crsEpsgCode).append("]");
            }
            sb.append("]");
            return sb.toString();
        }

        private String buildProjecedCrsWkt() {
            StringBuilder sb = new StringBuilder();
            if (crsName == null || crsName.isEmpty()) {
                throw new IllegalArgumentException("CRS Name not specified");
            }
            sb.append("PROJCRS[\"").append(crsName).append("\",\n");
            if (baseGeographicCrsName == null || baseGeographicCrsName.isEmpty()) {
                throw new IllegalArgumentException("Base Geographic CRS not specified");
            }
            sb.append("BASEGEODCRS[\"").append(baseGeographicCrsName).append("\",\n");
            if (datumName == null || datumName.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append("DATUM[\"").append(datumName).append("\",\n");
            if (ellipsoid == null || ellipsoid.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append(ellipsoid).append("],\n");
            if (primeMeridian != null && !primeMeridian.isEmpty()) {
                sb.append(primeMeridian);
            }
            sb.append("],\n");
            if (mapProjectionName == null || mapProjectionName.isEmpty()) {
                throw new IllegalArgumentException("Map Projection not specified");
            }
            sb.append("CONVERSION[\"").append(mapProjectionName).append("\",\n");
            if (projectionMethod == null || projectionMethod.isEmpty()) {
                throw new IllegalArgumentException("Projection method not specified");
            }
            sb.append(projectionMethod).append(",\n");
            for (int i = 0; i < projectionParameters.size(); i++) {
                sb.append(projectionParameters.get(i));
                if (i < projectionParameters.size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("],\n");
            if (coordinateSystemLine1 == null || coordinateSystemLine1.isEmpty()) {
                throw new IllegalArgumentException("Coordinate System not specified");
            }
            sb.append(coordinateSystemLine1);
            if (coordinateSystemAxisDetails.length > 0) {
                sb.append(",\n");
                Set<String> uniqueAxisUnits = new LinkedHashSet<>();
                for (String currentAxisUnit : coordinateSystemAxisUnits) {
                    uniqueAxisUnits.add(currentAxisUnit);
                }
                if (uniqueAxisUnits.size() == 1) {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append("],\n");
                    }
                    sb.append(uniqueAxisUnits.iterator().next());
                } else {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append(", ").append(coordinateSystemAxisUnits[i]).append("]");
                        if (i < coordinateSystemAxisDetails.length - 1) {
                            sb.append(",\n");
                        }
                    }
                }
            }
            if (crsEpsgCode != null && !crsEpsgCode.isEmpty()) {
                sb.append(",\n").append("ID[\"EPSG\", ").append(crsEpsgCode).append("]");
            }
            sb.append("]");

            return sb.toString();
        }

        private String buildGeoCentricCrsWkt() {
            StringBuilder sb = new StringBuilder();
            if (crsName == null || crsName.isEmpty()) {
                throw new IllegalArgumentException("CRS Name not specified");
            }
            sb.append("GEOCCS[\"").append(crsName).append("\",\n");
            if (datumName == null || datumName.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append("DATUM[\"").append(datumName).append("\",\n");
            if (ellipsoid == null || ellipsoid.isEmpty()) {
                throw new IllegalArgumentException("Geodetic Datum not specified");
            }
            sb.append(ellipsoid).append("],\n");
            if (primeMeridian != null && !primeMeridian.isEmpty()) {
                sb.append(primeMeridian);
            }
            sb.append(",\n");
            if (coordinateSystemLine1 == null || coordinateSystemLine1.isEmpty()) {
                throw new IllegalArgumentException("Coordinate System not specified");
            }
            sb.append(coordinateSystemLine1);
            if (coordinateSystemAxisDetails.length > 0) {
                sb.append(",\n");
                Set<String> uniqueAxisUnits = new LinkedHashSet<>();
                for (String currentAxisUnit : coordinateSystemAxisUnits) {
                    uniqueAxisUnits.add(currentAxisUnit);
                }
                if (uniqueAxisUnits.size() == 1) {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append("],\n");
                    }
                    sb.append(uniqueAxisUnits.iterator().next());
                } else {
                    for (int i = 0; i < coordinateSystemAxisDetails.length; i++) {
                        sb.append(coordinateSystemAxisDetails[i]).append(", ").append(coordinateSystemAxisUnits[i]).append("]");
                        if (i < coordinateSystemAxisDetails.length - 1) {
                            sb.append(",\n");
                        }
                    }
                }
            }
            if (crsEpsgCode != null && !crsEpsgCode.isEmpty()) {
                sb.append(",\n").append("ID[\"EPSG\", ").append(crsEpsgCode).append("]");
            }
            sb.append("]");
            return sb.toString();
        }
    }

}
