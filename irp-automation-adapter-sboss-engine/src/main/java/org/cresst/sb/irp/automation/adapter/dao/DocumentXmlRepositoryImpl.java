package org.cresst.sb.irp.automation.adapter.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.cresst.sb.irp.automation.adaptar.tis.data.XmlRepository;
import org.cresst.sb.irp.automation.adaptar.util.XmlRepositoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentXmlRepositoryImpl implements DocumentXmlRepository {
	private final static Logger logger = LoggerFactory.getLogger(DocumentXmlRepositoryImpl.class);

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSourceSqlServer) {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSourceSqlServer);
	}

	@Override
	public void getXmlRepositoryData(Date startTimeOfSimulation) {
		String SQL = "SELECT  [FileID]   ,[Location],[TestName],[OppId],[_efk_Testee],[StatusDate],[DateRecorded],[isDemo],[Contents],[SenderBrokerGuid],[CallbackURL]"
				+ " FROM [OSS_TIS].[dbo].[XMLRepository]"
				+ " WHERE TestName like '%COMBINED%' AND DateRecorded > :startTimeOfSimulation"
				+ " ORDER BY DateRecorded DESC";		
				 
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");		 
        String strDate = dateFormat.format(startTimeOfSimulation);
        
		Map namedParameters = new HashMap();   
		namedParameters.put("startTimeOfSimulation", strDate);		
		
		logger.info("startTimeOfSimulation " + strDate);
		
		List<XmlRepository> documents = namedParameterJdbcTemplate.query(SQL, namedParameters,	new XmlRepositoryMapper());
		logger.info("Documents found  : " + documents.size());
		for (XmlRepository rep : documents) {
			logger.info("object : " + rep.toString());
		}
		
		
	}

}
