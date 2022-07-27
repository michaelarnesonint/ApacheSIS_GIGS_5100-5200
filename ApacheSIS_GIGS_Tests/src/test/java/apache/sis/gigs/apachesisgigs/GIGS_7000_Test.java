package apache.sis.gigs.apachesisgigs;

import apache.sis.gigs.apachesisgigs.parser.GIGS7001Parser;
import apache.sis.gigs.apachesisgigs.parser.GIGSDeprecationInfo;
import java.io.File;
import java.util.List;
import java.util.ServiceLoader;
import org.apache.sis.util.Deprecable;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

public class GIGS_7000_Test {

    public final static String GIGS_TEST_DIRECTORY = "Path to GIGS test files";

    @Test
    public void test_GIGS_7001() throws Exception {
        CoordinateOperationAuthorityFactory opAuthorityfactory = ServiceLoader.load(CoordinateOperationAuthorityFactory.class).iterator().next();
        CRSAuthorityFactory crsAuthorityFactory = ServiceLoader.load(CRSAuthorityFactory.class).iterator().next();

        File file = new File(GIGS_TEST_DIRECTORY + "\\GIGS 7000 Deprecation test data\\ASCII\\GIGS_dep_7001.txt");
        GIGS7001Parser parser = new GIGS7001Parser();
        List<GIGSDeprecationInfo> infos = parser.parseFile(file);
        for (GIGSDeprecationInfo currentInfo : infos) {
            String epsgObjectCode = currentInfo.getEpsgObjectCode();
            if (!epsgObjectCode.startsWith("EPSG")) {
                epsgObjectCode = "EPSG:" + epsgObjectCode;
            }
            String epsgCodeForReplacement = currentInfo.getEpsgCodeForReplacement();
            if (epsgCodeForReplacement != null && !epsgCodeForReplacement.isEmpty() && !epsgCodeForReplacement.startsWith("EPSG")) {
                epsgCodeForReplacement = "EPSG:" + epsgCodeForReplacement;
            }
            IdentifiedObject epgsObject = null;
            IdentifiedObject replacementEpsgObject = null;
            GIGSDeprecationInfo.EntityType entityType = currentInfo.getEntityType();
            switch (entityType) {
                case COORDINATE_OPERATION:
                    epgsObject = opAuthorityfactory.createCoordinateOperation(epsgObjectCode);
                    if (epsgCodeForReplacement != null && !epsgCodeForReplacement.isEmpty()) {
                        replacementEpsgObject = opAuthorityfactory.createCoordinateOperation(epsgObjectCode);
                    }
                    break;
                case COORDINATE_REFERENCE_SYSTEM:
                    epgsObject = crsAuthorityFactory.createCoordinateReferenceSystem(epsgObjectCode);
                    if (epsgCodeForReplacement != null && !epsgCodeForReplacement.isEmpty()) {
                        replacementEpsgObject = crsAuthorityFactory.createCoordinateReferenceSystem(epsgObjectCode);
                    }
                    break;
                default:
                    fail("Unsupported entity type: " + entityType);
            }
            if (!isDeprecated(epgsObject)) {
                fail("EPSG Object Code " + epsgObjectCode + " is not listed as deprecated");
            }
            if (replacementEpsgObject != null && !isDeprecated(replacementEpsgObject)) {
                fail("EPSG Code for replacement " + epsgCodeForReplacement + " is not listed as deprecated");
                        
            }
        }
    }
    
    private boolean isDeprecated(IdentifiedObject currentEpgsObject) {
        if (!(currentEpgsObject instanceof Deprecable)) {
            return false;
        }
        return ((Deprecable) currentEpgsObject).isDeprecated();
    }
}
