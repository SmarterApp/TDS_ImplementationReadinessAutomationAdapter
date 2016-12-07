package org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.AutomationPhase;
import org.cresst.sb.irp.automation.adapter.engine.task.AutomationPreloadResults;
import org.cresst.sb.irp.automation.adapter.proctor.AutomationProctor;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.statusreporting.SbossAutomationStatusReporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SbossProctorControllerTest {

    @Mock
    private AutomationProctor proctor;

    @Test
    public void whenProctorLoginUnsuccessful_AutomationErrorMarkedAndExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        when(proctor.login()).thenReturn(false);

        SbossProctorController sut = new SbossProctorController(proctor);
        sut.setStatusReporter(reporter);

        try {
            // Run
            sut.initialize(preloadResults.getIrpTestKeys());
            fail("Expected exception");
        } catch (Exception ex) {
            // Assert
            assertTrue(ticket.getAdapterAutomationStatusReport().isError());
        }
    }

    @Test
    public void whenProctorLogsInSuccessfully_StartTestSession() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        final String expectedSessionId = "Test Session ID";

        when(proctor.login()).thenReturn(true);
        when(proctor.startTestSession(preloadResults.getIrpTestKeys())).thenReturn(true);
        when(proctor.getSessionId()).thenReturn(expectedSessionId);

        SbossProctorController sut = new SbossProctorController(proctor);
        sut.setStatusReporter(reporter);

        // Run
        String actualSessionId = sut.initialize(preloadResults.getIrpTestKeys());

        // Assert
        verify(proctor).startTestSession(preloadResults.getIrpTestKeys());
        assertEquals(expectedSessionId, actualSessionId);
    }

    @Test
    public void whenProctorUnableToStartTestSession_AutomationErrorMarkedAndExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);
        AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        when(proctor.login()).thenReturn(true);
        when(proctor.startTestSession(preloadResults.getIrpTestKeys())).thenReturn(false);

        SbossProctorController sut = new SbossProctorController(proctor);
        sut.setStatusReporter(reporter);

        try {
            // Run
            sut.initialize(preloadResults.getIrpTestKeys());
            fail("Expected exception");
        } catch (Exception exception) {
            // Assert
            assertTrue(ticket.getAdapterAutomationStatusReport().isError());
        }
    }

    @Test
    public void whenProctorApprovesAllFails_AutomationErrorMarkedAndExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);

        when(proctor.approveAllTestOpportunities()).thenReturn(false);

        SbossProctorController sut = new SbossProctorController(proctor);
        sut.setStatusReporter(reporter);

        try {
            // Run
            sut.approvalAll();
            fail("Expected exception");
        } catch (Exception exception) {
            // Assert
            assertTrue(ticket.getAdapterAutomationStatusReport().isError());
        }
    }

    @Test
    public void whenProctorLeaves_SessionPausedAndProctorLoggedOut() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);

        SbossProctorController sut = new SbossProctorController(proctor);
        sut.setStatusReporter(reporter);

        // Run
        sut.leave();

        // Assert
        verify(proctor).pauseTestSession();
        verify(proctor).logout();
    }
}