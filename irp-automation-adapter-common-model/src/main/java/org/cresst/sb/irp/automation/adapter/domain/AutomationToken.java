package org.cresst.sb.irp.automation.adapter.domain;

public class AutomationToken {
    private int token;

    public AutomationToken(AutomationRequest automationRequest) {
        this.token = automationRequest.hashCode();
    }

//    @JsonCreator
//    public AutomationToken(@JsonProperty("token") int token) {
//        this.token = token;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutomationToken that = (AutomationToken) o;
        return token == that.token;
    }

    @Override
    public int hashCode() {
        return token;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AutomationToken{");
        sb.append("token=").append(token);
        sb.append('}');
        return sb.toString();
    }

    public int getToken() {
        return token;
    }
}
