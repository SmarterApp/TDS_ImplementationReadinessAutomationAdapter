package org.cresst.sb.irp.automation.adapter.tdsreport.extractor;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Extracts TDS Reports from the Test Delivery System.
 */
public interface TdsReportExtractor {
    /**
     * Gets an individual TDSReport.
     * @param tdsReportId The identifier of the TDSReport to obtain
     * @return The TDSReport as a string
     */
    String getTdsReport(int tdsReportId);

    /**
     * Gets all of the generated TDSReports starting from the given date and time or since the time of previous
     * Test Simulation.
     *
     * @param startTimeOfSimulation Date and time of when to return TDS Reports.
     * @param studentTestCount A mapping of student ID to number of tests student successfully completed
     * @return A collection of all the IDs generated for TDSReports since the given date and time if a time is specified
     *          or returns a the same collection of IDs that were generated since the previous Test Simulation.
     */
    Collection<Integer> getTdsReports(final Date startTimeOfSimulation, Map<Integer, Integer> studentTestCount);
}
