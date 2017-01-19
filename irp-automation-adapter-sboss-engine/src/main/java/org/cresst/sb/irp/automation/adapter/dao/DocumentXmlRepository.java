package org.cresst.sb.irp.automation.adapter.dao;

import org.cresst.sb.irp.automation.adapter.domain.TDSReport;

import java.util.Date;
import java.util.List;

public interface DocumentXmlRepository {

	/**
	 * Gets a TDSReport that maps to the given identifier
	 * @param tdsReportId The FileID that maps to the TDSReport
	 * @return A TDSReport or null if tdsReportId does not map to a TDSReport
	 */
	TDSReport getTdsReport(int tdsReportId);

    /**
     * Gets a list of identifiers that map to the latest XmlRepository records that were generated since
     * the beginning of the time specified by the input parameter
     * @param startTimeOfSimulation The start time of when to return the the list of generated XmlRepository records
     * @return The list of identifiers that map to the XmlRepository records
     */
	List<Integer> getXmlRepositoryDataIdentifiers(Date startTimeOfSimulation);
}
