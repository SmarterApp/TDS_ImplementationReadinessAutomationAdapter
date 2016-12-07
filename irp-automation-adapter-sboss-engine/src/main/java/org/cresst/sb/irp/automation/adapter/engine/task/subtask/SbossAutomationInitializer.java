package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.configuration.AutomationProperties;
import org.cresst.sb.irp.automation.adapter.progman.ProgManTenantId;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SbossAutomationInitializer implements AutomationInitializer {
    private final static Logger logger = LoggerFactory.getLogger(SbossAutomationInitializer.class);

    private final AutomationProperties automationProperties;
    private final AutomationRestTemplate automationRestTemplate;

    public SbossAutomationInitializer(AutomationProperties automationProperties,
                                      AutomationRestTemplate automationRestTemplate) {
        this.automationProperties = automationProperties;
        this.automationRestTemplate = automationRestTemplate;
    }

    @Override
    public String initialize(AutomationStatusReporter initializationStatusReporter) throws Exception {
        try {
            logger.info("Getting Tenant ID");
            initializationStatusReporter.status("Fetching your Tenant ID from " + automationProperties.getProgramManagementUrl());

            final ProgManTenantId progManTenantId = new ProgManTenantId(automationRestTemplate,
                    automationProperties.getProgramManagementUrl(),
                    automationProperties.getStateAbbreviation());

            final String tenantId = progManTenantId.getTenantId();

            logger.info("Tenant ID {}", tenantId);
            initializationStatusReporter.status("Tenant ID received");

            return tenantId;
        } catch (Exception ex) {
            logger.info("Unable to get Tenant ID: " + ex.getMessage());
            initializationStatusReporter.status("Unable to get Tenant ID. Check OpenAM URL, PM URL, and PM credentials.");
            throw ex;
        }
    }
}
