package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.art.*;
import org.cresst.sb.irp.automation.adapter.configuration.AdapterResources;
import org.cresst.sb.irp.automation.adapter.configuration.AutomationProperties;
import org.cresst.sb.irp.automation.adapter.engine.task.AutomationPreloadResults;
import org.cresst.sb.irp.automation.adapter.rollback.Rollbacker;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.tsb.TestSpecBankData;
import org.cresst.sb.irp.automation.adapter.tsb.TestSpecBankSideLoader;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class SbossAutomationPreloader implements AutomationPreloader {
    private final static Logger logger = LoggerFactory.getLogger(SbossAutomationPreloader.class);

    private final AutomationProperties automationProperties;
    private final AdapterResources adapterResources;
    private final AutomationRestTemplate automationRestTemplate;

    // TODO: DI these
    private TestSpecBankSideLoader testSpecBankSideLoader;
    private ArtAssessmentSelector artAssessmentSelector;
    private ArtStudentUploader artStudentUploader;
    private ArtStudentAccommodationsUploader artStudentAccommodationsUploader;
    private ArtStudentGroupUploader artStudentGroupUploader;

    private Stack<Rollbacker> rollbackers = new Stack<>();

    public SbossAutomationPreloader(AutomationProperties automationProperties,
                                    AdapterResources adapterResources,
                                    AutomationRestTemplate automationRestTemplate) {

        this.automationProperties = automationProperties;
        this.adapterResources = adapterResources;
        this.automationRestTemplate = automationRestTemplate;
    }

    @Override
    public AutomationPreloadResults preload(AutomationStatusReporter preloadingStatusReporter,
                                            String tenantId) throws Exception {

        Set<String> irpTestKeys = new HashSet<String>();

        try {
            logger.info("Side-loading Registration Test Packages");

            testSpecBankSideLoader = new TestSpecBankSideLoader(
                    adapterResources.getRegistrationTestPackageDirectory(),
                    automationRestTemplate,
                    automationProperties.getTestSpecBankUrl(),
                    tenantId);

            rollbackers.push(testSpecBankSideLoader);

            preloadingStatusReporter.status("Side-loading IRP's ART Registration Test Packages into your TSB");

            List<TestSpecBankData> testSpecBankData = testSpecBankSideLoader.sideLoadRegistrationTestPackages();

            for (TestSpecBankData data : testSpecBankData) {
                irpTestKeys.add(data.getName());
            }

            preloadingStatusReporter.status(String.format("Side-loading complete. Side-loaded %d Registration Test Packages into TSB.",
                    testSpecBankData.size()));

            logger.info("Selecting Registration Test Packages in vendor's ART application");

            artAssessmentSelector = new ArtAssessmentSelector(automationRestTemplate,
                    automationProperties.getArtUrl(),
                    automationProperties.getStateAbbreviation());

            rollbackers.push(artAssessmentSelector);

            preloadingStatusReporter.status("Removing any existing IRP Test Packages from ART");
            int numberRemoved = artAssessmentSelector.deleteExistingAssessments(testSpecBankData);
            preloadingStatusReporter.status("Removed " + numberRemoved + " existing IRP Test Packages from ART");

            preloadingStatusReporter.status("Registering IRP Test Packages in ART");
            int numOfSelectedAssessments = artAssessmentSelector.selectAssessments(testSpecBankData);

            logger.info("Verifying all Registration Test Packages are selected");

            if (!artAssessmentSelector.verifySelectedAssessments(tenantId, testSpecBankData)) {
                throw new Exception("IRP found an error with the Test Package data loaded into your system.");
            }

            preloadingStatusReporter.status("Registered " + numOfSelectedAssessments + " IRP Assessments in ART");

            artStudentUploader = new ArtStudentUploader(
                    adapterResources.getStudentTemplatePath(),
                    automationRestTemplate,
                    automationProperties.getArtUrl(),
                    automationProperties.getStateAbbreviation(),
                    automationProperties.getDistrict(),
                    automationProperties.getInstitution());

            rollbackers.push(artStudentUploader);

            preloadingStatusReporter.status("Loading IRP Students into ART");

            final ArtUploaderResult artStudentUploaderResult = artStudentUploader.uploadData();
            if (!artStudentUploaderResult.isSuccessful()) {
                preloadingStatusReporter.status("Failed to load IRP Students into ART: " + artStudentUploaderResult.getMessage());
                throw new Exception("Unable to upload Student data because " + artStudentUploaderResult.getMessage());
            }

            preloadingStatusReporter.status(String.format("Successfully loaded %d IRP Students into ART. SSIDs start with 'IRP.'",
                    artStudentUploaderResult.getNumberOfRecordsUploaded()));

            artStudentAccommodationsUploader = new ArtStudentAccommodationsUploader(
                    adapterResources.getStudentAccommodationsTemplatePath(),
                    automationRestTemplate,
                    automationProperties.getArtUrl(),
                    automationProperties.getStateAbbreviation());

            preloadingStatusReporter.status("Loading IRP Student Accommodations into ART");

            final ArtUploaderResult artStudentAccommodationsUploaderResult = artStudentAccommodationsUploader.uploadData();
            if (!artStudentAccommodationsUploaderResult.isSuccessful()) {
                preloadingStatusReporter.status("Failed to load IRP Student Accommodations into ART: "
                        + artStudentAccommodationsUploaderResult.getMessage());
                throw new Exception("Unable to upload Student Accommodations data because "
                        + artStudentAccommodationsUploaderResult.getMessage());
            }

            preloadingStatusReporter.status(String.format("Successfully loaded %d IRP Student Accommodations into ART.",
                    artStudentAccommodationsUploaderResult.getNumberOfRecordsUploaded()));

            artStudentGroupUploader = new ArtStudentGroupUploader(
                    adapterResources.getStudentGroupTemplatePath(),
                    automationRestTemplate,
                    automationProperties.getArtUrl(),
                    automationProperties.getStateAbbreviation(),
                    automationProperties.getDistrict(),
                    automationProperties.getInstitution(),
                    automationProperties.getProctorUserId());

            rollbackers.push(artStudentGroupUploader);

            preloadingStatusReporter.status("Loading IRP Student Group into ART");

            final ArtUploaderResult artStudentGroupUploaderResult = artStudentGroupUploader.uploadData();
            if (!artStudentGroupUploaderResult.isSuccessful()) {
                preloadingStatusReporter.status("Failed to load IRP Student Group into ART: " + artStudentGroupUploaderResult.getMessage());
                throw new Exception("Unable to upload IRP Student Group data because " + artStudentGroupUploaderResult.getMessage());
            }

            preloadingStatusReporter.status(String.format("Successfully added %d IRP Students to the IRPStudentGroup in ART.",
                    artStudentGroupUploaderResult.getNumberOfRecordsUploaded()));

            Thread.sleep(30 * 1000);

        } catch (Exception ex) {
            logger.error("Preloading error occurred.", ex);

            preloadingStatusReporter.status("Preloading error occurred.");
            preloadingStatusReporter.markAutomationError();

            throw ex;
        } finally {
            preloadingStatusReporter.status("Done preloading");
        }

        return new AutomationPreloadResults(irpTestKeys);
    }

    @Override
    public void rollback(AutomationStatusReporter cleanupStatusReporter) {
        while (!rollbackers.isEmpty()) {
            Rollbacker rollbacker = rollbackers.pop();
            try {
                rollbacker.rollback();
            } catch (Exception ex) {
                // Rollback as much as possible
                logger.error("A rollback failed for " + rollbacker, ex);
                cleanupStatusReporter.status("Rollback failed for " + rollbacker);
            }
        }
    }
}