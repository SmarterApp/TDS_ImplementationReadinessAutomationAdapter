package org.cresst.sb.irp.automation.adapter.service;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * 
 * @author Paul Espinosa
 *
 * This class contains the methos used to process the automation process
 */
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
    String getTdsReport(int tdsReportId);

    /**
     * Gets all of the generated TDSReports starting from the given date and time or since the time of previous
     * Test Simulation.
     *
     * @param startTimeOfSimulation Optional. Date and time of when to return TDS Reports.
     * @return A collection of all the IDs generated for TDSReports since the given date and time if a time is specified
     *          or returns a the same collection of IDs that were generated since the previous Test Simulation.
     */
    Collection<Integer> getTdsReports(Date startTimeOfSimulation);
}
