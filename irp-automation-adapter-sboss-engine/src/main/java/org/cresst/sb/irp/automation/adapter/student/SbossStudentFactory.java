package org.cresst.sb.irp.automation.adapter.student;

import com.google.common.collect.Lists;
import org.cresst.sb.irp.automation.adapter.web.AutomationRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryOperations;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SbossStudentFactory implements StudentFactory {
    private final static Logger logger = LoggerFactory.getLogger(SbossStudentFactory.class);

    private final AutomationRestTemplate studentRestTemplate;
    private final URL studentUrl;
    private final StudentResponseGenerator studentResponseGenerator;
    private final RetryOperations retryTemplate;

    public SbossStudentFactory(AutomationRestTemplate studentRestTemplate,
                               URL studentUrl,
                               StudentResponseGenerator studentResponseGenerator,
                               RetryOperations retryTemplate) {

        this.studentRestTemplate = studentRestTemplate;
        this.studentUrl = studentUrl;
        this.studentResponseGenerator = studentResponseGenerator;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public List<AutomationStudent> createStudents() {
        Map<StudentTestTypeEnum, String> testNames = new HashMap<>();
        testNames.put(StudentTestTypeEnum.FIXED, "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016");
        testNames.put(StudentTestTypeEnum.CAT, "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016");

        SbossStudent student = new SbossStudent(
                studentRestTemplate,
                retryTemplate,
                studentUrl,
                testNames,
                studentResponseGenerator,
                "AIRP39990001",
                "IRPStudentA");

        return Lists.<AutomationStudent>newArrayList(student);
    }
}
