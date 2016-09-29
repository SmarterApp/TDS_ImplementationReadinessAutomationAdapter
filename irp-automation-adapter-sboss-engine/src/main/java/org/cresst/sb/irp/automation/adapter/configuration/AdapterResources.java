package org.cresst.sb.irp.automation.adapter.configuration;

import org.springframework.core.io.Resource;

public class AdapterResources {
    private Resource registrationTestPackageDirectory;
    private Resource studentTemplatePath;
    private Resource studentAccommodationsTemplatePath;
    private Resource studentGroupTemplatePath;

    public Resource getRegistrationTestPackageDirectory() {
        return registrationTestPackageDirectory;
    }

    public void setRegistrationTestPackageDirectory(Resource registrationTestPackageDirectory) {
        this.registrationTestPackageDirectory = registrationTestPackageDirectory;
    }

    public Resource getStudentTemplatePath() {
        return studentTemplatePath;
    }

    public void setStudentTemplatePath(Resource studentTemplatePath) {
        this.studentTemplatePath = studentTemplatePath;
    }

    public Resource getStudentAccommodationsTemplatePath() {
        return studentAccommodationsTemplatePath;
    }

    public void setStudentAccommodationsTemplatePath(Resource studentAccommodationsTemplatePath) {
        this.studentAccommodationsTemplatePath = studentAccommodationsTemplatePath;
    }

    public Resource getStudentGroupTemplatePath() {
        return studentGroupTemplatePath;
    }

    public void setStudentGroupTemplatePath(Resource studentGroupTemplatePath) {
        this.studentGroupTemplatePath = studentGroupTemplatePath;
    }
}
