package org.cresst.sb.irp.automation.adapter.engine.task.subtask;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.AutomationPhase;
import org.cresst.sb.irp.automation.adapter.engine.task.AutomationPreloadResults;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation.ProctorController;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation.StudentController;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.statusreporting.SbossAutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.student.StudentTestTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SbossAutomationTestSimulatorTest {

//    @Mock
//    private StudentFactory studentFactory;

    @Mock
    private ProctorController proctorController;

    @Mock
    private StudentController studentController;

    // 1. Proctor initialization: login and test selection
    // 2. All Students initialization: login and open test for all students
    // 3. Proctor approval all
    // 4. All Students take test
    // Repeat step 2 for next test
    // 5. Students leave
    // 6. Proctor leaves

    @Test
    public void whenProctorInitializerFails_ExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        doThrow(new Exception("Test Proctor Initialization Error")).when(proctorController)
                .initialize(preloadResults.getIrpTestKeys());

        SbossAutomationTestSimulator sut = new SbossAutomationTestSimulator(proctorController, studentController);

        try {
            // Run
            sut.simulate(reporter, preloadResults);
            fail("Expected exception with 'Test Proctor Initialization Error' message");
        } catch (Exception exception) {
            // Assert
            assertThat(exception.getMessage(), is("Test Proctor Initialization Error"));
        }
    }

    @Test
    public void whenSimulationHasError_ProctorLeaves() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        doThrow(new Exception("Test Proctor Initialization Error")).when(proctorController)
                .initialize(preloadResults.getIrpTestKeys());

        SbossAutomationTestSimulator sut = new SbossAutomationTestSimulator(proctorController, studentController);

        try {
            // Run
            sut.simulate(reporter, preloadResults);
            fail("Expected exception to be thrown");
        } catch(Exception ex) {
            // Assert
            verify(proctorController).leave();
        }
    }

    @Test
    public void whenProctorInitilizationSuccessful_InitializeStudents() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        final String sessionId = "Test Session ID";
        when(proctorController.initialize(preloadResults.getIrpTestKeys())).thenReturn(sessionId);

        SbossAutomationTestSimulator sut = new SbossAutomationTestSimulator(proctorController, studentController);

        // Run
        sut.simulate(reporter, preloadResults);

        // Assert
        verify(studentController).initialize(sessionId, StudentTestTypeEnum.FIXED);
    }

    @Test
    public void whenStudentsInizializedFails_ExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        final String sessionId = "Test Session ID";
        when(proctorController.initialize(preloadResults.getIrpTestKeys())).thenReturn(sessionId);
        doThrow(new Exception("Test Student Initialization Error")).when(studentController)
                .initialize(sessionId, StudentTestTypeEnum.FIXED);

        SbossAutomationTestSimulator sut = new SbossAutomationTestSimulator(proctorController, studentController);

        try {
            // Run
            sut.simulate(reporter, preloadResults);
            fail("Expected exception with 'Test Student Initialization Error' message");
        } catch (Exception exception) {
            // Assert
            assertThat(exception.getMessage(), is("Test Student Initialization Error"));
        }
    }

    @Test
    public void whenStudentsInitialized_ProctorApprovesAllTests() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        final String sessionId = "Test Session ID";
        when(proctorController.initialize(preloadResults.getIrpTestKeys())).thenReturn(sessionId);

        SbossAutomationTestSimulator sut = new SbossAutomationTestSimulator(proctorController, studentController);

        // Run
        sut.simulate(reporter, preloadResults);

        // Assert
        verify(studentController).initialize(sessionId, StudentTestTypeEnum.FIXED);
        verify(studentController).initialize(sessionId, StudentTestTypeEnum.CAT);
        verify(proctorController, times(2)).approvalAll();
    }

    @Test
    public void whenProctorApprovesTests_StudentsTakeTests() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        final String sessionId = "Test Session ID";
        when(proctorController.initialize(preloadResults.getIrpTestKeys())).thenReturn(sessionId);

        SbossAutomationTestSimulator sut = new SbossAutomationTestSimulator(proctorController, studentController);

        // Run
        sut.simulate(reporter, preloadResults);

        // Assert
        verify(studentController).initialize(sessionId, StudentTestTypeEnum.FIXED);
        verify(studentController).initialize(sessionId, StudentTestTypeEnum.CAT);
        verify(proctorController, times(2)).approvalAll();
        verify(studentController, times(2)).takeTests();
    }

}