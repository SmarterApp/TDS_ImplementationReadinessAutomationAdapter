package org.cresst.sb.irp.automation.adapter.art;

import org.cresst.sb.irp.automation.adapter.rollback.Rollbacker;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArtStudentUploader extends ArtUploader implements Rollbacker {
    private final static Logger logger = LoggerFactory.getLogger(ArtStudentUploader.class);

    // Must contain the template since it's shared between instances
    private static final List<String> studentTemplateLines = new ArrayList<>();

    final private String stateAbbreviation;
    final private String responsibleDistrictId;
    final private String responsibleInstitutionId;

    private boolean rollbackState = false;

    public ArtStudentUploader(Resource studentTemplate,
                              AutomationRestTemplate automationRestTemplate, URL artUrl,
                              String stateAbbreviation, String responsibleDistrictId, String responsibleInstitutionId) throws IOException {
        super(automationRestTemplate, artUrl);

        if (studentTemplateLines.isEmpty()) {
            studentTemplateLines.addAll(Files.readAllLines(Paths.get(studentTemplate.getURI()), StandardCharsets.UTF_8));
        }

        this.stateAbbreviation = stateAbbreviation;
        this.responsibleDistrictId = responsibleDistrictId;
        this.responsibleInstitutionId = responsibleInstitutionId;
    }

    @Override
    public void rollback() {
        rollbackState = true;
        ArtUploaderResult result = uploadData();
        rollbackState = false;

        logger.info("ART Student roll back was{}successful. Message: {}", result.isSuccessful() ? "" : " un", result.getMessage());
    }

    @Override
    String generateData() {
        return generateDataForStudents(rollbackState);
    }

    @Override
    String getFormatType() {
        return "STUDENT";
    }

    @Override
    String getFilename() {
        return "IRPStudents.csv";
    }

    public Set<ArtStudent> getArtStudents() {
        Set<ArtStudent> artStudents = new HashSet<>();
        for (String studentLine : studentTemplateLines) {
            String[] studentFields = studentLine.split(",");
            artStudents.add(new ArtStudent(studentFields[4], studentFields[7]));
        }
        return artStudents;
    }

    private String generateDataForStudents(boolean deleteGroup) {
        StringBuilder builder = new StringBuilder(studentTemplateLines.get(0));
        builder.append("\n");
        for (int i = 1; i < studentTemplateLines.size(); i++) {
            builder.append(studentTemplateLines.get(i)
                    .replace("{SA}", stateAbbreviation)
                    .replace("{RDI}", responsibleDistrictId)
                    .replace("{RII}", responsibleInstitutionId)
                    .replace("{DELETE}", deleteGroup ? "DELETE" : ""))
                    .append("\n");
        }

        return builder.toString();
    }
}
