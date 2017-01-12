package org.cresst.sb.irp.automation.adapter.configuration;

import java.net.URL;
import java.util.Objects;

public class AutomationProperties {
    private String tenantName;
    private String stateAbbreviation;
    private String district;
    private String institution;
    private URL oAuthUrl;
    private URL programManagementUrl;
    private String programManagementClientId;
    private String programManagementClientSecret;
    private String programManagementUserId;
    private String programManagementUserPassword;
    private URL testSpecBankUrl;
    private String testSpecBankUserId;
    private String testSpecBankPassword;
    private URL artUrl;
    private String artUserId;
    private String artPassword;
    private URL proctorUrl;
    private String proctorUserId;
    private String proctorPassword;
    private URL studentUrl;
    private String tisDbUrl;
    private String tisDbUsername;
    private String tisDbPassword;

    public AutomationProperties() {}

    /**
     * The values chosen to check for equality help identify duplicate requests.
     * @param o The other request to compare against
     * @return True if two requests are for the same vendor implementation; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutomationProperties that = (AutomationProperties) o;
        return Objects.equals(tenantName, that.tenantName) &&
                Objects.equals(stateAbbreviation, that.stateAbbreviation) &&
                Objects.equals(district, that.district) &&
                Objects.equals(institution, that.institution) &&
                Objects.equals(oAuthUrl, that.oAuthUrl) &&
                Objects.equals(programManagementUrl, that.programManagementUrl) &&
                Objects.equals(programManagementClientId, that.programManagementClientId) &&
                Objects.equals(programManagementClientSecret, that.programManagementClientSecret) &&
                Objects.equals(programManagementUserId, that.programManagementUserId) &&
                Objects.equals(programManagementUserPassword, that.programManagementUserPassword) &&
                Objects.equals(testSpecBankUrl, that.testSpecBankUrl) &&
                Objects.equals(artUrl, that.artUrl) &&
                Objects.equals(proctorUrl, that.proctorUrl);
    }

    /**
     * The hashcode is derived from values that identify duplicate requests. Duplicate requests should return the
     * same hashcode.
     * @return The hashcode of this request.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                tenantName,
                stateAbbreviation,
                district,
                institution,
                oAuthUrl,
                programManagementUrl,
                programManagementClientId,
                programManagementClientSecret,
                programManagementUserId,
                programManagementUserPassword,
                testSpecBankUrl,
                artUrl,
                proctorUrl);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AutomationProperties{");
        sb.append("tenantName='").append(tenantName).append('\'');
        sb.append(", oAuthUrl=").append(oAuthUrl);
        sb.append('}');
        return sb.toString();
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getStateAbbreviation() {
        return stateAbbreviation;
    }

    public void setStateAbbreviation(String stateAbbreviation) {
        this.stateAbbreviation = stateAbbreviation;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public URL getoAuthUrl() {
        return oAuthUrl;
    }

    public void setoAuthUrl(URL oAuthUrl) {
        this.oAuthUrl = oAuthUrl;
    }

    public URL getProgramManagementUrl() {
        return programManagementUrl;
    }

    public void setProgramManagementUrl(URL programManagementUrl) {
        this.programManagementUrl = programManagementUrl;
    }

    public String getProgramManagementClientId() {
        return programManagementClientId;
    }

    public void setProgramManagementClientId(String programManagementClientId) {
        this.programManagementClientId = programManagementClientId;
    }

    public String getProgramManagementClientSecret() {
        return programManagementClientSecret;
    }

    public void setProgramManagementClientSecret(String programManagementClientSecret) {
        this.programManagementClientSecret = programManagementClientSecret;
    }

    public String getProgramManagementUserId() {
        return programManagementUserId;
    }

    public void setProgramManagementUserId(String programManagementUserId) {
        this.programManagementUserId = programManagementUserId;
    }

    public String getProgramManagementUserPassword() {
        return programManagementUserPassword;
    }

    public void setProgramManagementUserPassword(String programManagementUserPassword) {
        this.programManagementUserPassword = programManagementUserPassword;
    }

    public URL getTestSpecBankUrl() {
        return testSpecBankUrl;
    }

    public void setTestSpecBankUrl(URL testSpecBankUrl) {
        this.testSpecBankUrl = testSpecBankUrl;
    }

    public String getTestSpecBankUserId() {
        return testSpecBankUserId;
    }

    public void setTestSpecBankUserId(String testSpecBankUserId) {
        this.testSpecBankUserId = testSpecBankUserId;
    }

    public String getTestSpecBankPassword() {
        return testSpecBankPassword;
    }

    public void setTestSpecBankPassword(String testSpecBankPassword) {
        this.testSpecBankPassword = testSpecBankPassword;
    }

    public URL getArtUrl() {
        return artUrl;
    }

    public void setArtUrl(URL artUrl) {
        this.artUrl = artUrl;
    }

    public String getArtUserId() {
        return artUserId;
    }

    public void setArtUserId(String artUserId) {
        this.artUserId = artUserId;
    }

    public String getArtPassword() {
        return artPassword;
    }

    public void setArtPassword(String artPassword) {
        this.artPassword = artPassword;
    }

    public URL getProctorUrl() {
        return proctorUrl;
    }

    public void setProctorUrl(URL proctorUrl) {
        this.proctorUrl = proctorUrl;
    }

    public String getProctorUserId() {
        return proctorUserId;
    }

    public void setProctorUserId(String proctorUserId) {
        this.proctorUserId = proctorUserId;
    }

    public String getProctorPassword() {
        return proctorPassword;
    }

    public void setProctorPassword(String proctorPassword) {
        this.proctorPassword = proctorPassword;
    }

    public URL getStudentUrl() {
        return this.studentUrl;
    }

    public void setStudentUrl(URL studentUrl) {
        this.studentUrl = studentUrl;
    }

	public String getTisDbUrl() {
		return tisDbUrl;
	}

	public void setTisDbUrl(String tisDbUrl) {
		this.tisDbUrl = tisDbUrl;
	}

	public String getTisDbUsername() {
		return tisDbUsername;
	}

	public void setTisDbUsername(String tisDbUsername) {
		this.tisDbUsername = tisDbUsername;
	}

	public String getTisDbPassword() {
		return tisDbPassword;
	}

	public void setTisDbPassword(String tisDbPassword) {
		this.tisDbPassword = tisDbPassword;
	}
}
