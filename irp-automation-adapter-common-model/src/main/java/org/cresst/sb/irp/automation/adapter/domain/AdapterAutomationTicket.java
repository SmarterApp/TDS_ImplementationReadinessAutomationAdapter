package org.cresst.sb.irp.automation.adapter.domain;

import java.util.Date;
import java.util.UUID;

public class AdapterAutomationTicket {

    public AdapterAutomationTicket() {}

    // From Adapter
    private UUID adapterAutomationToken;
    private AdapterAutomationStatusReport adapterAutomationStatusReport;
    private Date startTimeOfSimulation;
    
    public UUID getAdapterAutomationToken() {
        return adapterAutomationToken;
    }

    public void setAdapterAutomationToken(UUID adapterAutomationToken) {
        this.adapterAutomationToken = adapterAutomationToken;
    }

    public AdapterAutomationStatusReport getAdapterAutomationStatusReport() {
        return adapterAutomationStatusReport;
    }

    public void setAdapterAutomationStatusReport(AdapterAutomationStatusReport adapterAutomationStatusReport) {
        this.adapterAutomationStatusReport = adapterAutomationStatusReport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterAutomationTicket that = (AdapterAutomationTicket) o;
        return adapterAutomationToken == that.adapterAutomationToken;
    }

    @Override
    public int hashCode() {
        return adapterAutomationToken.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AdapterAutomationTicket{");
        sb.append("adapterAutomationToken=").append(adapterAutomationToken);
        sb.append('}');
        return sb.toString();
    }

	public Date getStartTimeOfSimulation() {
		return startTimeOfSimulation;
	}

	public void setStartTimeOfSimulation(Date startTimeOfSimulation) {
		this.startTimeOfSimulation = startTimeOfSimulation;
	}
}
