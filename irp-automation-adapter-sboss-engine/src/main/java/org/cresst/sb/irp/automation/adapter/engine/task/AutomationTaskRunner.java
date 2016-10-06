package org.cresst.sb.irp.automation.adapter.engine.task;

import org.cresst.sb.irp.automation.adapter.accesstoken.AccessToken;
import org.cresst.sb.irp.automation.adapter.art.*;
import org.cresst.sb.irp.automation.adapter.domain.*;
import org.cresst.sb.irp.automation.adapter.proctor.SbossProctor;
import org.cresst.sb.irp.automation.adapter.proctor.Proctor;
import org.cresst.sb.irp.automation.adapter.progman.ProgManTenantId;
import org.cresst.sb.irp.automation.adapter.configuration.AdapterResources;
import org.cresst.sb.irp.automation.adapter.configuration.AutomationProperties;
import org.cresst.sb.irp.automation.adapter.rollback.Rollbacker;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.statusreporting.SbossAutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.student.SbossStudent;
import org.cresst.sb.irp.automation.adapter.student.Student;
import org.cresst.sb.irp.automation.adapter.student.StudentResponseService;
import org.cresst.sb.irp.automation.adapter.student.data.TestSelection;
import org.cresst.sb.irp.automation.adapter.tsb.TestSpecBankData;
import org.cresst.sb.irp.automation.adapter.tsb.TestSpecBankSideLoader;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.cresst.sb.irp.automation.adapter.web.SbossAutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Runs Automation Tasks against the Smarter Balanced Open Source Test Delivery System
 */
public class AutomationTaskRunner implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(AutomationTaskRunner.class);

    private final AdapterAutomationTicket adapterAutomationTicket;
    private final AutomationProperties automationProperties;
    private final AdapterResources adapterResources;

    private Runnable onCompletionCallback;

    public AutomationTaskRunner(AdapterAutomationTicket adapterAutomationTicket,
                                AutomationProperties automationProperties,
                                AdapterResources adapterResources) {
        this.adapterAutomationTicket = adapterAutomationTicket;
        this.automationProperties = automationProperties;
        this.adapterResources = adapterResources;
    }

    /**
     * Sets a external method to call after automation is completed. It will be run regardless of automation errors.
     * @param callback The method to call after automation is finished.
     */
    public void onCompletion(Runnable callback) {
        onCompletionCallback = callback;
    }

    @Override
    public void run() {
        AutomationRestTemplate accessTokenRestTemplate = new SbossAutomationRestTemplate();
        AutomationRestTemplate automationRestTemplate = new SbossAutomationRestTemplate();

        adapterAutomationTicket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());

        AutomationStatusReporter initializationStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.INITIALIZATION,
                adapterAutomationTicket);
        AutomationStatusReporter preloadingStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.PRELOADING,
                adapterAutomationTicket);
        AutomationStatusReporter simulationStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION,
                adapterAutomationTicket);
        AutomationStatusReporter analysisStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.ANALYSIS,
                adapterAutomationTicket);
        AutomationStatusReporter cleanupStatusReporter = new SbossAutomationStatusReporter(AutomationPhase.CLEANUP,
                adapterAutomationTicket);

        logger.info("Building Access Token");
        final AccessToken accessToken = AccessToken.buildAccessToken(accessTokenRestTemplate,
                automationProperties.getoAuthUrl(),
                automationProperties.getProgramManagementClientId(),
                automationProperties.getProgramManagementClientSecret(),
                automationProperties.getProgramManagementUserId(),
                automationProperties.getProgramManagementUserPassword());

        automationRestTemplate.addAccessToken(accessToken);

        try {
            final String tenantId = initialize(automationRestTemplate, initializationStatusReporter);

            AutomationPreloadResults automationPreloadResults = preload(automationRestTemplate, preloadingStatusReporter, tenantId);
            simulate(accessTokenRestTemplate, simulationStatusReporter, automationPreloadResults);
            //analyze();
            //cleanup();
        } catch (Exception ex) {
            logger.error("Ending automation task because of exception", ex);
        } finally {
            logger.info("Automation task for {} is complete.", adapterAutomationTicket);
            cleanupStatusReporter.markAutomationComplete();
        }
    }

    private String initialize(AutomationRestTemplate automationRestTemplate, AutomationStatusReporter initializationStatusReporter) {
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
            logger.info("Unable to get Tenant ID");
            initializationStatusReporter.status("Unable to get Tenant ID. Check OpenAM URL, PM URL, and PM credentials.");
            throw ex;
        }
    }

    private AutomationPreloadResults preload(AutomationRestTemplate automationRestTemplate, AutomationStatusReporter preloadingStatusReporter,
                                String tenantId) throws Exception {

        Set<String> irpTestKeys = new HashSet<>();
        List<ArtStudent> artStudents = new ArrayList<>();
        Stack<Rollbacker> rollbackers = new Stack<>();
        try {
            logger.info("Side-loading Registration Test Packages");

            final TestSpecBankSideLoader testSpecBankSideLoader = new TestSpecBankSideLoader(
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

            final ArtAssessmentSelector artAssessmentSelector = new ArtAssessmentSelector(automationRestTemplate,
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

            final ArtStudentUploader artStudentUploader = new ArtStudentUploader(
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

            final ArtStudentAccommodationsUploader artStudentAccommodationsUploader = new ArtStudentAccommodationsUploader(
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

            artStudents = artStudentUploader.getArtStudents();

            final ArtStudentGroupUploader artStudentGroupUploader = new ArtStudentGroupUploader(
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

        } catch (Exception ex) {
            logger.error("Automation error occurred. Rolling back data now.", ex);
            preloadingStatusReporter.status("An error occurred, IRP is rolling back any data it has loading into your implementation");

            while (!rollbackers.isEmpty()) {
                Rollbacker rollbacker = rollbackers.pop();
                rollbacker.rollback();
            }

            preloadingStatusReporter.status("Rollback is complete");

            preloadingStatusReporter.markAutomationError();

            throw ex;
        } finally {
            if (onCompletionCallback != null) {
                onCompletionCallback.run();
            }

            preloadingStatusReporter.status("Done");
        }

        return new AutomationPreloadResults(irpTestKeys, artStudents);
    }

    private void simulate(AutomationRestTemplate accessTokenRestTemplate, AutomationStatusReporter simulationStatusReporter,
                            AutomationPreloadResults automationPreloadResults) {

        Set<String> irpTestKeys = automationPreloadResults.getIrpTestKeys();
        List<ArtStudent> artStudents = automationPreloadResults.getArtStudents();

        final Proctor proctor = new SbossProctor(accessTokenRestTemplate,
                new SbossAutomationRestTemplate(),
                automationProperties.getoAuthUrl(),
                automationProperties.getProctorUrl(),
                automationProperties.getProgramManagementClientId(),
                automationProperties.getProgramManagementClientSecret(),
                automationProperties.getProctorUserId(),
                automationProperties.getProctorPassword());

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = null;
        StudentResponseService studentResponseService = null;
        try {
            is = new BufferedInputStream(new FileInputStream(classLoader.getResource("IRPv2_generated_item_responses.txt").getFile()));
        } catch (FileNotFoundException e) {
            simulationStatusReporter.status("Unable to load generated item responses: " + e.getMessage());
        }

        // TODO: use dependency injection
        try {
            studentResponseService = new StudentResponseService(is);
        } catch (IOException e) {
            logger.error("Unable to parse response file: " + e.getMessage());
        }
        final Student student = new SbossStudent(accessTokenRestTemplate, automationProperties.getStudentUrl(), studentResponseService);

        simulationStatusReporter.status(String.format("Logging in as Proctor (%s)", automationProperties.getProctorUserId()));
        if (proctor.login()) {
            logger.info("Proctor login successful");
            simulationStatusReporter.status("Proctor login successful. Initiating Test Session.");

            if (proctor.startTestSession(irpTestKeys )) {
                logger.info("Successfully started test session");
                logger.info("Available tests: " + Arrays.toString(irpTestKeys.toArray()));
                simulationStatusReporter.status("Test Session has been initiated by the Proctor");

                ArtStudent artStudent = artStudents.get(1);
                if(student.login(proctor.getSessionId(), artStudent.getSsid(), artStudent.getFirstName(), "")) {
                    logger.info("Student {} login successful", artStudent.getFirstName());

                    List<TestSelection> studentTests = student.getTests();
                    if (studentTests.size() > 0) {
                        if(student.startTestSession(studentTests.get(0))) {
                            logger.info("Student {} successfully started test {}", artStudent.getFirstName(), studentTests.get(0).getDisplayName());
                        } else {
                            logger.info("Student {} unable to start test {}", artStudent.getFirstName(), studentTests.get(0).getDisplayName());
                        }
                    }
                    //for (TestSelection testSelection : student.getTests()) {
                    //    logger.info("Found test: {} for student: {}", testSelection.getDisplayName(), artStudent.getFirstName());
                    //}
                } else {
                    logger.info("Student {} login unsuccessful", artStudent.getFirstName());
                    simulationStatusReporter.status(String.format("Student {} login unsuccessful", artStudent.getFirstName()));
                }

                /*
                // Login students that were put in ART
                for (ArtStudent artStudent : artStudents) {
                    if(student.login(proctor.getSessionId(), artStudent.getSsid(), artStudent.getFirstName(), "")) {
                        logger.info("Student {} login successful", artStudent.getFirstName());
                    } else {
                        logger.info("Student {} login unsuccessful", artStudent.getFirstName());
                        simulationStatusReporter.status(String.format("Student {} login unsuccessful", artStudent.getFirstName()));
                    }
                }*/

            } else {
                logger.info("Proctor was unable to start a Test Session");
                simulationStatusReporter.status("Proctor was unable to start a Test Session");
                simulationStatusReporter.markAutomationError();
            }
        } else {
            logger.info("Proctor login was unsuccessful");
            simulationStatusReporter.status("Proctor login was unsuccessful");
            simulationStatusReporter.markAutomationError();
        }
    }
}

