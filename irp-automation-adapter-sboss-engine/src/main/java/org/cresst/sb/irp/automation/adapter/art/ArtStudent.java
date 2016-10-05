package org.cresst.sb.irp.automation.adapter.art;

public class ArtStudent {
    private String firstName;
    private String ssid;
    public ArtStudent(String firstName, String ssid) {
        this.firstName = firstName;
        this.ssid = ssid;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getSsid() {
        return ssid;
    }
    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    @Override
    public String toString() {
        return "FirstName: " + getFirstName() + ", SSID: " + getSsid();
    }
}
