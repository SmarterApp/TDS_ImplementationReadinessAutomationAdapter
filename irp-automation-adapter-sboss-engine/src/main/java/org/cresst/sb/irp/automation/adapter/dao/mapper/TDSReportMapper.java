package org.cresst.sb.irp.automation.adapter.dao.mapper;

import org.cresst.sb.irp.automation.adapter.domain.TDSReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TDSReportMapper implements RowMapper<TDSReport> {
    private final static Logger logger = LoggerFactory.getLogger(TDSReportMapper.class);

    private final Unmarshaller unmarshaller;

    public TDSReportMapper(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public TDSReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        TDSReport tdsReport = null;

        try {
            tdsReport = (TDSReport) unmarshaller.unmarshal(new StreamSource(rs.getCharacterStream("Contents")));
        } catch (Exception ex) {
            logger.error("Unable to deserialize TDSReport from TIS database", ex);
        }

        return tdsReport;
    }
}