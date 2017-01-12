package org.cresst.sb.irp.automation.adapter.service;
 
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationStatusReport;
import org.cresst.sb.irp.automation.adapter.domain.AdapterAutomationTicket;
import org.cresst.sb.irp.automation.adapter.domain.Context;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.cresst.sb.irp.automation.adapter.engine.AutomationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SbossAutomationAdapterService implements AdapterAutomationService {
	private final static Logger logger = LoggerFactory.getLogger(SbossAutomationAdapterService.class);
    private final AutomationEngine automationEngine;

    private AdapterAutomationTicket adapterAutomationTicket;

    private int listStatusSize;
    
    public SbossAutomationAdapterService(AutomationEngine automationEngine) {
        this.automationEngine = automationEngine;
    }

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

        if (adapterAutomationTicket == null || adapterAutomationTicket.getAdapterAutomationStatusReport().isAutomationComplete()) {
            adapterAutomationTicket = new AdapterAutomationTicket();
            adapterAutomationTicket.setAdapterAutomationToken(UUID.randomUUID());
            listStatusSize = 0;
            if (!automationEngine.automate(adapterAutomationTicket)) {
                adapterAutomationTicket = null;
            }
        }

        return adapterAutomationTicket;
    }

    /**
     * Gets the latest update to the AdapterAutomationTicket for the given Automation Token.
     *
     * @param adapterAutomationToken The token identifying the automation request.
     * @return The AdapterAutomationTicket with the latest automation status.
     */
    @Override
    public AdapterAutomationTicket getAdapterAutomationTicket(UUID adapterAutomationToken) {
    	if (adapterAutomationTicket != null &&
                adapterAutomationTicket.getAdapterAutomationToken().equals(adapterAutomationToken)) {
    		while (true) {
    			AdapterAutomationStatusReport report = adapterAutomationTicket.getAdapterAutomationStatusReport(); 
    			if (report.getPhaseStatuses().size() > listStatusSize) {
        			listStatusSize = adapterAutomationTicket.getAdapterAutomationStatusReport().getPhaseStatuses().size();
        			break;
            	}

    			try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
    		}

    		return adapterAutomationTicket;
    	} else {
    		return null;
    	}
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
