package org.cresst.sb.irp.automation.adapter.student;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"test", "testType", "segmentName", "componentTestName", "cat", "studentSSID", "alternateSSID", "studentName"})
public class TestStudentMapping {
    private String test;
    private String testType;
    private String segmentName;
    private String componentTestName;
    private String studentSSID;
    private String alternateSSID;
    private String studentName;
    private String cat;
    private boolean isCat;

    @Override
    public String toString() {
        return "TestStudentMapping [test=" + test + ", testType=" + testType + ", segmentName=" + segmentName
                + ", componentTestName=" + componentTestName + ", studentSSID=" + studentSSID + ", alternateSSID="
                + alternateSSID + ", studentName=" + studentName + ", isCat=" + isCat + "]";
    }

    public boolean isCat() {
        return isCat;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.isCat = cat.toLowerCase().equals("true");
        this.cat = cat;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public String getComponentTestName() {
        return componentTestName;
    }

    public void setComponentTestName(String componentTestName) {
        this.componentTestName = componentTestName;
    }

    public String getStudentSSID() {
        return studentSSID;
    }

    public void setStudentSSID(String studentSSID) {
        this.studentSSID = studentSSID;
    }

    public String getAlternateSSID() {
        return alternateSSID;
    }

    public void setAlternateSSID(String alternateSSID) {
        this.alternateSSID = alternateSSID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
