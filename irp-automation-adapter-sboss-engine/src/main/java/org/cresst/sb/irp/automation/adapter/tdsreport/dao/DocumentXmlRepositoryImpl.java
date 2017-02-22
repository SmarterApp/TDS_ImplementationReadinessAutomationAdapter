package org.cresst.sb.irp.automation.adapter.tdsreport.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.*;

/**
 * 
 *
 */
public class DocumentXmlRepositoryImpl implements DocumentXmlRepository {
    private final static Logger logger = LoggerFactory.getLogger(DocumentXmlRepositoryImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DocumentXmlRepositoryImpl(DataSource dataSourceSqlServer) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSourceSqlServer);
    }

    @Override
    public String getTdsReport(int tdsReportId) {
        String SQL = "SELECT [Contents]"
                + " FROM [OSS_TIS].[dbo].[XMLRepository]"
                + " WHERE [FileId] = :tdsReportId";

        Map<String, Object > namedParameters = new HashMap<>();
        namedParameters.put("tdsReportId", tdsReportId);

        String tdsReport = namedParameterJdbcTemplate.queryForObject(SQL, namedParameters, new RowMapper<String>() {
        	@Override
        	public String mapRow(ResultSet rs, int arg1) throws SQLException {
        		SQLXML  sXml = rs.getSQLXML("Contents");
        		return sXml.getString();
        	}
		});

        return tdsReport;
    }

    @Override
    public List<Integer> getXmlRepositoryDataIdentifiers(Date startTimeOfSimulation) {
        if (startTimeOfSimulation == null) {
            return new ArrayList<>();
        }

        String SQL = "SELECT [FileID],[Location],[TestName],[OppId],[_efk_Testee],[StatusDate],[DateRecorded],[isDemo],[Contents],[SenderBrokerGuid],[CallbackURL]"
                + " FROM [OSS_TIS].[dbo].[v_MostRecentXml]"
                + " WHERE DateRecorded >= DATEADD(ms, :startTimeOfSimulation, GETDATE())"
                + " ORDER BY DateRecorded DESC";

        long diff = (new Date()).getTime() - startTimeOfSimulation.getTime();

        logger.info("Getting TDS Reports from database from {}", diff);

        Map namedParameters = new HashMap();
        namedParameters.put("startTimeOfSimulation", -diff);

        List<Integer> idList = namedParameterJdbcTemplate.query(SQL, namedParameters, new RowMapper<Integer>() {
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("FileID");
            }
        });

        return idList;
    }

}