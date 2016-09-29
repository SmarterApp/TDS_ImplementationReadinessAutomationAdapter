package org.cresst.sb.irp.automation.adapter.service;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;

import java.util.Collection;
import java.util.UUID;

public interface AdapterAutomationService {

    /**
     * Begins automated generation of TDSReports.
     * @return An AdapterAutomationTicket containing information about the automation request.
     */
    AdapterAutomationTicket automate();

    /**
     * Gets the latest update to the AdapterAutomationTicket for the given Automation Token.
     * @param adapterAutomationToken The token identifying the automation request.
     * @return The AdapterAutomationTicket with the latest automation status.
     */
    AdapterAutomationTicket getAdapterAutomationTicket(UUID adapterAutomationToken);

    /**
     * Gets an individual TDSReport.
     * @param tdsReportId The identifier of the TDSReport to obtain
     * @return The TDSReport
     */
    TDSReport getTdsReport(int tdsReportId);

    /**
     * Gets all of the generated TDSReports.
     * @return A collection of all the generated TDSReports.
     */
    Collection<TDSReport> getTdsReports();
}
