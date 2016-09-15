package org.cresst.sb.irp.automation.adapter.web;

import org.springframework.hateoas.ResourceSupport;

public class TdsReportResource extends ResourceSupport {

    private String testName;
    private String studentIdentifier;

    public TdsReportResource(int id, String testName, String studentIdentifier) {
        this.testName = testName;
        this.studentIdentifier = studentIdentifier;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getStudentIdentifier() {
        return studentIdentifier;
    }

    public void setStudentIdentifier(String studentIdentifier) {
        this.studentIdentifier = studentIdentifier;
    }

}
