package apache.sis.gigs.apachesisgigs.parser;

public class GIGSDeprecationInfo {

    public enum EntityType {
        COORDINATE_OPERATION,
        COORDINATE_REFERENCE_SYSTEM;

        public static EntityType decodeFromString(String type) {
            if (type.equals("Coordinate_Operation")) {
                return COORDINATE_OPERATION;
            }
            if (type.equals("Coordinate Reference System")) {
                return COORDINATE_REFERENCE_SYSTEM;
            }
            throw new IllegalArgumentException("Can't decode entity type: " + type);
        }
    }

    private final String epsgObjectCode;
    private final EntityType entityType;
    private final String deprecatedEntityName;
    private final String epsgCodeForReplacement;
    private final String deprecationReason;
    private final String deprecationDate;

    public GIGSDeprecationInfo(Builder builder) {
        this.epsgObjectCode = builder.epsgObjectCode;
        this.entityType = builder.entityType;
        this.deprecatedEntityName = builder.deprecatedEntityName;
        this.epsgCodeForReplacement = builder.epsgCodeForReplacement;
        this.deprecationReason = builder.deprecationReason;
        this.deprecationDate = builder.deprecationDate;
    }

    public String getEpsgObjectCode() {
        return epsgObjectCode;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getDeprecatedEntityName() {
        return deprecatedEntityName;
    }

    public String getEpsgCodeForReplacement() {
        return epsgCodeForReplacement;
    }

    public String getDeprecationReason() {
        return deprecationReason;
    }

    public String getDeprecationDate() {
        return deprecationDate;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String epsgObjectCode;
        private EntityType entityType;
        private String deprecatedEntityName;
        private String epsgCodeForReplacement;
        private String deprecationReason;
        private String deprecationDate;

        public Builder setEpsgObjectCode(String epsgObjectCode) {
            this.epsgObjectCode = epsgObjectCode;
            return this;
        }

        public Builder setEntityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder setDeprecatedEntityName(String deprecatedEntityName) {
            this.deprecatedEntityName = deprecatedEntityName;
            return this;
        }

        public Builder setEpsgCodeForReplacement(String epsgCodeForReplacement) {
            this.epsgCodeForReplacement = epsgCodeForReplacement;
            return this;
        }

        public Builder setDeprecationReason(String deprecationReason) {
            this.deprecationReason = deprecationReason;
            return this;
        }

        public Builder setDeprecationDate(String deprecationDate) {
            this.deprecationDate = deprecationDate;
            return this;
        }

        public GIGSDeprecationInfo build() {
            if (this.epsgObjectCode == null) {
                throw new IllegalArgumentException("EPSG Object Code must be specified");
            }
            if (this.entityType == null) {
                throw new IllegalArgumentException("Entity Type must be specified");
            }
            return new GIGSDeprecationInfo(this);
        }

    }
}
