package apache.sis.gigs.apachesisgigs.parser;

import apache.sis.gigs.apachesisgigs.parser.GIGSDeprecationInfo.EntityType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GIGS7001Parser {

    public List<GIGSDeprecationInfo> parseFile(File gigs7001File) throws Exception {
        List<GIGSDeprecationInfo> infos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(gigs7001File)))) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] currentLineSegments = getLineSegments(line);
                if (currentLineSegments.length != 6) {
                    throw new IllegalArgumentException("Unexpected number of segments for line: " + line);
                }
                String epsgObjectCode = currentLineSegments[0];
                EntityType entityType = EntityType.decodeFromString(currentLineSegments[1]);
                String epsgCodeForReplacement = null;
                if (!currentLineSegments[3].isEmpty()) {
                    epsgCodeForReplacement = currentLineSegments[3];
                }
                GIGSDeprecationInfo.Builder builder = GIGSDeprecationInfo.builder();
                builder.setEpsgObjectCode(epsgObjectCode)
                        .setEntityType(entityType)
                        .setDeprecatedEntityName(currentLineSegments[2])
                        .setDeprecationReason(currentLineSegments[4])
                        .setDeprecationDate(currentLineSegments[5]);
                if (epsgCodeForReplacement != null) {
                    builder.setEpsgCodeForReplacement(epsgCodeForReplacement);
                }
                GIGSDeprecationInfo currentInfo = builder.build();
                infos.add(currentInfo);
            }
        }

        return infos;
    }

    private String[] getLineSegments(String line) {
        String[] split = line.split("\t");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }

}
