package org.cresst.sb.irp.automation.adapter.student;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

public class SbossStudentFactory implements StudentFactory {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudentFactory.class);

    private final AutomationRestTemplate studentRestTemplate;
    private final URL studentUrl;
    private final StudentResponseGenerator studentResponseGenerator;

    public SbossStudentFactory(AutomationRestTemplate studentRestTemplate,
                               URL studentUrl,
                               StudentResponseGenerator studentResponseGenerator) {

        this.studentRestTemplate = studentRestTemplate;
        this.studentUrl = studentUrl;
        this.studentResponseGenerator = studentResponseGenerator;
    }

    @Override
    public List<AutomationStudent> createStudents() {
        AutomationStudent student = new SbossStudent(
                studentRestTemplate,
                studentUrl,
                studentResponseGenerator,
                "AIRP39990001",
                "IRPStudentA");

        return Lists.newArrayList(student);
    }
}
