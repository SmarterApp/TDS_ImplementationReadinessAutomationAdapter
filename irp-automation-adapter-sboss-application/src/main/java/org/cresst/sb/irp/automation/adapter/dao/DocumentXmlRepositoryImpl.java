package org.cresst.sb.irp.automation.adapter.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.cresst.sb.irp.automation.adaptar.util.XmlRepositoryMapper;
import org.cresst.sb.irp.automation.adapter.web.domain.XmlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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
	public void getXmlRepositoryData(Date date) {
		String SQL = "SELECT TOP 6 [FileID]   ,[Location],[TestName],[OppId],[_efk_Testee],[StatusDate],[DateRecorded],[isDemo],[Contents],[SenderBrokerGuid],[CallbackURL]"
				+ " FROM [OSS_TIS].[dbo].[XMLRepository]"
				//+ " WHERE TestName like '%COMBINED%' AND DateRecorded > :startTimeOfSimulation"
				+ " WHERE TestName like '%COMBINED%'  "
				+ " ORDER BY DateRecorded DESC";
		 
		
		Map namedParameters = new HashMap();   
		namedParameters.put("startTimeOfSimulation", date);
	    
		  
		List<XmlRepository> documents = namedParameterJdbcTemplate.query(SQL, namedParameters,	new XmlRepositoryMapper());

		for (XmlRepository rep : documents) {
			logger.info("File id: " + rep.getFileID());
		}

	}

}
