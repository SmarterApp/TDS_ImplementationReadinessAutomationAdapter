package org.cresst.sb.irp.automation.adapter.dao;

import java.util.Date;
import java.util.List;

/**
 * 
 * This class is the repository for the operations related to the table "XMLRepository" and the view "v_MostRecentXml"
 *
 */
public interface DocumentXmlRepository {

	/**
	 * Gets a TDSReport that maps to the given identifier
	 * @param tdsReportId The FileID that maps to the TDSReport
	 * @return A TDSReport or null if tdsReportId does not map to a TDSReport
	 */
	String getTdsReport(int tdsReportId);

    /**
     * Gets a list of identifiers that map to the latest XmlRepository records that were generated since
     * the beginning of the time specified by the input parameter
     * @param startTimeOfSimulation The start time of when to return the the list of generated XmlRepository records
     * @return The list of identifiers that map to the XmlRepository records
     */
	List<Integer> getXmlRepositoryDataIdentifiers(Date startTimeOfSimulation);
}
