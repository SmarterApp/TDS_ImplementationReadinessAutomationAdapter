package org.cresst.sb.irp.automation.adapter.domain;

import java.net.URI;
import java.util.List;

public class AdapterAutomationTicket {

    public AdapterAutomationTicket() {}

    // From IRP
    private List<URI> irpResources;

    // From Adapter
    private int adapterAutomationToken;
    private AdapterAutomationStatusReport adapterAutomationStatusReport;

    public List<URI> getIrpResources() {
        return irpResources;
    }

    public void setIrpResources(List<URI> irpResources) {
        this.irpResources = irpResources;
    }

    public int getAdapterAutomationToken() {
        return adapterAutomationToken;
    }

    public void setAdapterAutomationToken(int adapterAutomationToken) {
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
        return adapterAutomationToken;
    }
}
