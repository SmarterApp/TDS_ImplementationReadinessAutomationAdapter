package org.cresst.sb.irp.automation.adapter.engine.task.subtask.simulation;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.AutomationPhase;
import org.cresst.sb.irp.automation.adapter.statusreporting.AutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.statusreporting.SbossAutomationStatusReporter;
import org.cresst.sb.irp.automation.adapter.student.AutomationStudent;
import org.cresst.sb.irp.automation.adapter.student.StudentTestTypeEnum;
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
    public void whenAllStudentLoginsFail_ExceptionThrown() throws Exception {

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
        final StudentTestTypeEnum studentTestTypeEnum = StudentTestTypeEnum.FIXED;

        when(student1.login(sessionId)).thenReturn(true);
        when(student2.login(sessionId)).thenReturn(false);

        when(student1.openTest(studentTestTypeEnum)).thenReturn(true);

        SbossStudentController sut = new SbossStudentController(students);
        sut.setStatusReporter(reporter);

        // Run
        sut.initialize(sessionId, studentTestTypeEnum);

        // Assert
        verify(student1).openTest(studentTestTypeEnum);
    }

    @Test
    public void whenNoTestsAreOpen_ExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);

        final String sessionId = "Test Session ID";
        final List<AutomationStudent> students = Lists.newArrayList(student1, student2);
        final StudentTestTypeEnum studentTestTypeEnum = StudentTestTypeEnum.FIXED;

        when(student1.login(sessionId)).thenReturn(true);
        when(student2.login(sessionId)).thenReturn(false);

        SbossStudentController sut = new SbossStudentController(students);
        sut.setStatusReporter(reporter);

        try {
            // Run
            sut.initialize(sessionId, studentTestTypeEnum);
            fail("Expected exception");
        } catch (Exception exception) {
            // Assert
            verify(student1).login(sessionId);
            verify(student2).login(sessionId);
            verify(student1).openTest(studentTestTypeEnum);
        }
    }

    @Test
    public void whenAllStudentsFailDuringTakingTest_ExceptionThrown() throws Exception {

        // Setup
        AdapterAutomationTicket ticket = new AdapterAutomationTicket();
        ticket.setAdapterAutomationStatusReport(new AdapterAutomationStatusReport());
        AutomationStatusReporter reporter = new SbossAutomationStatusReporter(AutomationPhase.SIMULATION, ticket);

        final List<AutomationStudent> students = Lists.newArrayList(student1, student2);

        when(student1.takeTest()).thenReturn(false);
        when(student2.takeTest()).thenReturn(false);

        SbossStudentController sut = new SbossStudentController(students);
        sut.setStatusReporter(reporter);

        try {
            // Run
            sut.takeTests();
            fail("Expected exception");
        } catch (Exception exception) {
            // Assert
            verify(student1).takeTest();
            verify(student2).takeTest();
        }
    }
}