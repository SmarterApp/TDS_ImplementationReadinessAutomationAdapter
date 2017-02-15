package org.cresst.sb.irp.automation.adapter.engine.task;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.AutomationInitializer;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.AutomationPreloader;
import org.cresst.sb.irp.automation.adapter.engine.task.subtask.AutomationTestSimulator;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SbossAutomationTaskRunnerTest {

    @Mock
    private AutomationInitializer initializer;

    @Mock
    private AutomationPreloader preloader;

    @Mock
    private AutomationTestSimulator simulator;
    
    @Test
    public void whenAdapterAutomationTicketNotSet_ExceptionThrown() throws Exception {
        SbossAutomationTaskRunner sut = new SbossAutomationTaskRunner(null, null, null);

        try {
            sut.run();
            fail("Expected RuntimeException with 'Adapter Automation Ticket was not set' message");
        } catch (RuntimeException exception) {
            assertThat(exception.getMessage(), is("Adapter Automation Ticket was not set"));
        }
    }

    @Test
    public void whenInitializerThrowsException_AutomationErrorMarked() throws Exception {

        // Setup
        when(initializer.initialize(any(AutomationStatusReporter.class))).thenThrow(new Exception("Test Error"));

        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        SbossAutomationTaskRunner sut = new SbossAutomationTaskRunner(initializer, preloader, simulator);
        sut.setAdapterAutomationTicket(ticket);

        // Run
        sut.run();

        // Assert
        assertTrue(ticket.getAdapterAutomationStatusReport().isError());
        assertTrue(ticket.getAdapterAutomationStatusReport().isAutomationComplete());
    }

    @Test
    public void whenPreloaderThrowsException_AutomationErrorMarkedAndRollback() throws Exception {

        // Setup
        final String tenantID = "Tenant ID";

        when(initializer.initialize(any(AutomationStatusReporter.class))).thenReturn(tenantID);
        when(preloader.preload(any(AutomationStatusReporter.class), eq(tenantID))).thenThrow(new Exception("Test Error"));

        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        SbossAutomationTaskRunner sut = new SbossAutomationTaskRunner(initializer, preloader, simulator);
        sut.setAdapterAutomationTicket(ticket);

        // Run
        sut.run();

        // Assert
        assertTrue(ticket.getAdapterAutomationStatusReport().isError());
        assertTrue(ticket.getAdapterAutomationStatusReport().isAutomationComplete());
        verify(preloader).rollback(any(AutomationStatusReporter.class));
    }

    @Test
    public void whenSimulatorThrowsException_AutomationErrorMarkedAndRollback() throws Exception {

        // Setup
        final String tenantID = "Tenant ID";
        final AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        when(initializer.initialize(any(AutomationStatusReporter.class))).thenReturn(tenantID);
        when(preloader.preload(any(AutomationStatusReporter.class), eq(tenantID))).thenReturn(preloadResults);
        doThrow(new Exception("Test Error")).when(simulator).simulate(any(AutomationStatusReporter.class), eq(preloadResults));

        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        SbossAutomationTaskRunner sut = new SbossAutomationTaskRunner(initializer, preloader, simulator);
        sut.setAdapterAutomationTicket(ticket);

        // Run
        sut.run();

        // Assert
        assertTrue(ticket.getAdapterAutomationStatusReport().isError());
        assertTrue(ticket.getAdapterAutomationStatusReport().isAutomationComplete());
        verify(preloader).rollback(any(AutomationStatusReporter.class));
    }

    @Test
    public void whenAutomationComplete_AutomationMarkedCompleteAndRollback() throws Exception {
        // Setup
        final String tenantID = "Tenant ID";
        final AutomationPreloadResults preloadResults = new AutomationPreloadResults(new HashSet<String>());

        when(initializer.initialize(any(AutomationStatusReporter.class))).thenReturn(tenantID);
        when(preloader.preload(any(AutomationStatusReporter.class), eq(tenantID))).thenReturn(preloadResults);

        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        SbossAutomationTaskRunner sut = new SbossAutomationTaskRunner(initializer, preloader, simulator);
        sut.setAdapterAutomationTicket(ticket);

        // Run
        sut.run();

        // Assert
        assertFalse(ticket.getAdapterAutomationStatusReport().isError());
        assertTrue(ticket.getAdapterAutomationStatusReport().isAutomationComplete());
        verify(simulator).simulate(any(AutomationStatusReporter.class), eq(preloadResults));
        verify(preloader).rollback(any(AutomationStatusReporter.class));
    }
}
