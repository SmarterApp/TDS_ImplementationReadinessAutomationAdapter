package org.cresst.sb.irp.automation.adapter.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TdsTestOpportunityRepository implements TestOpportunityRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public TdsTestOpportunityRepository(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Map<Integer, Integer> getStudentToTestCount(Date startTimeOfSimulation) {
        String sql = "SELECT `_efk_testee`, COUNT(`_efk_testee`) AS count" +
                " FROM session.testopportunity" +
                " WHERE `datestarted` >= :startTimeOfSimulation AND (`status` = 'reported' OR `status` = 'submitted')" +
                " GROUP BY `_efk_testee`" +
                " ORDER BY `_efk_testee`";

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String strDate = dateFormat.format(startTimeOfSimulation);

        Map namedParameters = new HashMap();
        namedParameters.put("startTimeOfSimulation", strDate);

        return namedParameterJdbcTemplate.query(sql, namedParameters, new ResultSetExtractor<Map<Integer, Integer>>() {
            @Override
            public Map<Integer, Integer> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                Map<Integer, Integer> records = new HashMap<>();

                while (resultSet.next()) {
                    records.put(resultSet.getInt("_efk_testee"), resultSet.getInt("count"));
                }

                return records;
            }
        });
    }
}
