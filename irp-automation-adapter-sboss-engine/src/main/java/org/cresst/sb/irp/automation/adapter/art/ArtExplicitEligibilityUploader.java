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
import java.util.List;

public class ArtExplicitEligibilityUploader extends ArtUploader implements Rollbacker {
    private final static Logger logger = LoggerFactory.getLogger(ArtExplicitEligibilityUploader.class);

    // Must contain the template since it's shared between instances
    private static final List<String> explicitEligibilityLines = new ArrayList<>();

    final private String stateAbbreviation;
    final private String responsibleDistrictId;

    private boolean rollbackState = false;

    public ArtExplicitEligibilityUploader(Resource eligibilityTemplate,
                                          AutomationRestTemplate automationRestTemplate,
                                          URL artUrl,
                                          String stateAbbreviation,
                                          String responsibleDistrictId) throws IOException {
        super(automationRestTemplate, artUrl);

        if (explicitEligibilityLines.isEmpty()) {
            explicitEligibilityLines.addAll(Files.readAllLines(Paths.get(eligibilityTemplate.getURI()), StandardCharsets.UTF_8));
        }

        this.stateAbbreviation = stateAbbreviation;
        this.responsibleDistrictId = responsibleDistrictId;
    }

    @Override
    public void rollback() {
        rollbackState = true;
        ArtUploaderResult result = uploadData();
        rollbackState = false;

        logger.info("ART Explicit Eligibility roll back was {}successful. Message: {}", result.isSuccessful() ? "" : " un", result.getMessage());
    }

    @Override
    String generateData() {
        return generateDataForExplicitEligibility(rollbackState);
    }

    @Override
    String getFormatType() {
        return "EXPLICITELIGIBILITY";
    }

    @Override
    String getFilename() {
        return "IRPExplicitEligibility_template.csv";
    }

    private String generateDataForExplicitEligibility(boolean deleteGroup) {
        StringBuilder builder = new StringBuilder(explicitEligibilityLines.get(0));
        builder.append("\n");
        for (int i = 1; i < explicitEligibilityLines.size(); i++) {
            builder.append(explicitEligibilityLines.get(i)
                    .replace("{SA}", stateAbbreviation)
                    .replace("{RDI}", responsibleDistrictId)
                    .replace("{DELETE}", deleteGroup ? "DELETE" : ""))
                    .append("\n");
        }

        return builder.toString();
    }
}
