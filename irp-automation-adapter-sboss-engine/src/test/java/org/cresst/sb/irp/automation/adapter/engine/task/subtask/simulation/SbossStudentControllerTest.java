package org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.AutomationPhase;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.statusreporting.SbossAutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.student.AutomationStudent;
import org.cresst.sb.irp.automation.adapter.student.StudentTestTypeEnum;
import org.cresst.sb.irp.automation.adapter.student.data.TestSelection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SbossStudentControllerTest {

    @Mock
    private AutomationStudent student1;

    @Mock
    private AutomationStudent student2;

    @Test
    public void whenAllStudentLoginsFail_AutomationErrorMarkedAndExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);

        final String sessionId = "Test Session ID";
        final List<AutomationStudent> students = Lists.newArrayList(student1, student2);

        when(student1.login(sessionId)).thenReturn(false);
        when(student2.login(sessionId)).thenReturn(false);

        SbossStudentController sut = new SbossStudentController(students);
        sut.setStatusReporter(reporter);

        try {
            // Run
            sut.initialize(sessionId, StudentTestTypeEnum.FIXED);
            fail("Expected exception");
        } catch (Exception exception) {
            // Assert
            assertTrue(ticket.getAdapterAutomationStatusReport().isError());
            verify(student1).login(sessionId);
            verify(student2).login(sessionId);
        }
    }

    @Test
    public void whenAtLeastOneStudentLoginSucceeds_OpenTest() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);

        final String sessionId = "Test Session ID";
        final List<AutomationStudent> students = Lists.newArrayList(student1, student2);
        TestSelection testSelection = new TestSelection();
        testSelection.setTestID("Test ID");
        testSelection.setTestKey("Test Key");

        when(student1.login(sessionId)).thenReturn(true);
        when(student2.login(sessionId)).thenReturn(false);

        when(student1.getTests()).thenReturn(Lists.newArrayList(testSelection));
        when(student1.openTestSelection(testSelection)).thenReturn(true);

        SbossStudentController sut = new SbossStudentController(students);
        sut.setStatusReporter(reporter);

        // Run
        sut.initialize(sessionId, StudentTestTypeEnum.FIXED);

        // Assert
        verify(student1).openTestSelection(testSelection);
    }

    @Test
    public void whenNoTestsAreOpen_AutomationErrorMarkedAndExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);

        final String sessionId = "Test Session ID";
        final List<AutomationStudent> students = Lists.newArrayList(student1, student2);
        TestSelection testSelection = new TestSelection();
        testSelection.setTestID("Test ID");
        testSelection.setTestKey("Test Key");

        when(student1.login(sessionId)).thenReturn(true);
        when(student2.login(sessionId)).thenReturn(false);

        when(student1.getTests()).thenReturn(Lists.newArrayList(testSelection));
        when(student1.openTestSelection(testSelection)).thenReturn(false);

        SbossStudentController sut = new SbossStudentController(students);
        sut.setStatusReporter(reporter);

        try {
            // Run
            sut.initialize(sessionId, StudentTestTypeEnum.FIXED);
            fail("Expected exception");
        } catch (Exception exception) {
            // Assert
            assertTrue(ticket.getAdapterAutomationStatusReport().isError());
            verify(student1).login(sessionId);
            verify(student2).login(sessionId);
            verify(student1).openTestSelection(testSelection);
        }
    }
}