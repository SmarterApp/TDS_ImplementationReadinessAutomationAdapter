package org.cresst.sb.irp.automation.adapter.dao;

import java.util.Date;
import java.util.List;

public interface DocumentXmlRepository {

	void getXmlRepositoryData(Date date);
	List<Integer> getXmlRepositoryDataIdentifiers(Date startTimeOfSimulation);
}
