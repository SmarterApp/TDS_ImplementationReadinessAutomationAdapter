package org.cresst.sb.irp.automation.adapter.dao;

import org.cresst.sb.irp.automation.adapter.dao.mapper.TDSReportMapper;
import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class DocumentXmlRepositoryImpl implements DocumentXmlRepository {
    private final static Logger logger = LoggerFactory.getLogger(DocumentXmlRepositoryImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final TDSReportMapper tdsReportMapper;

    public DocumentXmlRepositoryImpl(DataSource dataSourceSqlServer, TDSReportMapper tdsReportMapper) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSourceSqlServer);
        this.tdsReportMapper = tdsReportMapper;
    }

    @Override
    public TDSReport getTdsReport(int tdsReportId) {
        String SQL = "SELECT [Contents]"
                + " FROM [OSS_TIS].[dbo].[XMLRepository]"
                + " WHERE [FileId] = :tdsReportId";

        Map namedParameters = new HashMap();
        namedParameters.put("tdsReportId", tdsReportId);

        TDSReport tdsReport = namedParameterJdbcTemplate.queryForObject(SQL, namedParameters, tdsReportMapper);

        return tdsReport;
    }

    @Override
    public List<Integer> getXmlRepositoryDataIdentifiers(Date startTimeOfSimulation) {
        if (startTimeOfSimulation == null) {
            return new ArrayList<>();
        }

        String SQL = "SELECT [FileID],[Location],[TestName],[OppId],[_efk_Testee],[StatusDate],[DateRecorded],[isDemo],[Contents],[SenderBrokerGuid],[CallbackURL]"
                + " FROM [OSS_TIS].[dbo].[v_MostRecentXml]"
                + " WHERE DateRecorded > :startTimeOfSimulation"
                + " ORDER BY DateRecorded DESC";

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String strDate = dateFormat.format(startTimeOfSimulation);

        Map namedParameters = new HashMap();
        namedParameters.put("startTimeOfSimulation", strDate);

        List<Integer> idList = namedParameterJdbcTemplate.query(SQL, namedParameters, new RowMapper<Integer>() {
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("FileID");
            }
        });

        return idList;
    }

}