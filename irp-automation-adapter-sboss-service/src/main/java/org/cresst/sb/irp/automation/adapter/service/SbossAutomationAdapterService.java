package org.cresst.sb.irp.automation.adapter.service;

import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.Context;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class SbossAutomationAdapterService implements AdapterAutomationService {

//    private final AsyncAutomationEngine automationEngine;

    private AdapterAutomationTicket ticket;
    private int ticker = 0;

//    public SbossAutomationAdapterService(AsyncAutomationEngine automationEngine) {
//        this.automationEngine = automationEngine;
//    }

    private static Map<Integer, TDSReport> reportsMap = new HashMap<>();

    private static int hashCode = 0;
    public static int tdsReportHashCode(String testName, String studentIdentifier) {
        if (hashCode == 3) hashCode = 0;
        return hashCode++;
    }

    static {
        for (int i = 0; i < 3; i++) {
            TDSReport.Test test = new TDSReport.Test();
            test.setName("TDSReportName" + i);

            TDSReport.Examinee examinee = new TDSReport.Examinee();
            TDSReport.Examinee.ExamineeAttribute attribute = new TDSReport.Examinee.ExamineeAttribute();
            attribute.setContext(Context.FINAL);
            attribute.setName("StudentIdentifier");
            attribute.setValue("TDSReportStudentIdentifier" + i);
            examinee.getExamineeAttributeOrExamineeRelationship().add(attribute);

            TDSReport tdsReport = new TDSReport();
            tdsReport.setTest(test);
            tdsReport.setExaminee(examinee);

            int id = tdsReportHashCode(test.getName(), attribute.getValue());
            reportsMap.put(id, tdsReport);
        }
    }

    /**
     * Begins automated generation of TDSReports.
     *
     * @return An AdapterAutomationTicket containing information about the automation request.
     */
    @Override
    public AdapterAutomationTicket automate() {

        if (ticket != null) { return ticket; }

        ticket = new AdapterAutomationTicket();
        AdapterAutomationStatusReport report = new AdapterAutomationStatusReport();
        ticket.setAdapterAutomationToken(12345);
        ticket.setAdapterAutomationStatusReport(report);

        return ticket;
    }

    /**
     * Gets the latest update to the AdapterAutomationTicket for the given Automation Token.
     *
     * @param adapterAutomationToken The token identifying the automation request.
     * @return The AdapterAutomationTicket with the latest automation status.
     */
    @Override
    public AdapterAutomationTicket getAdapterAutomationTicket(int adapterAutomationToken) {
        if (ticker == 3) {
            ticket.getAdapterAutomationStatusReport().setAutomationComplete(true);
        }
        ticket.getAdapterAutomationStatusReport().setLastUpdateTimestamp(ticker++);
        return ticket;
    }

    /**
     * Gets an individual TDSReport.
     *
     * @param tdsReportId The identifier of the TDSReport to obtain
     * @return The TDSReport
     */
    @Override
    public TDSReport getTdsReport(int tdsReportId) {
        return reportsMap.get(tdsReportId);
    }

    /**
     * Gets all of the generated TDSReports.
     *
     * @return A collection of all the generated TDSReports.
     */
    @Override
    public Collection<TDSReport> getTdsReports() {
        return reportsMap.values();
    }
}
