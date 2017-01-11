package org.cresst.sb.irp.automation.adaptar.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.cresst.sb.irp.automation.adapter.web.domain.XmlRepository;
import org.springframework.jdbc.core.RowMapper;

public class XmlRepositoryMapper  implements RowMapper<XmlRepository> {
	public XmlRepository mapRow(ResultSet rs, int rowNum) throws SQLException {  
		XmlRepository object = new XmlRepository();
		object.setFileID(rs.getInt("FileID"));
		object.setLocation(rs.getString("Location"));
		object.setTestName(rs.getString("TestName"));
		object.setOppId(rs.getInt("OppId"));
		object.setEfkTestee(rs.getInt("_efk_Testee"));
		object.setStatusDate(rs.getDate("StatusDate"));
		object.setDateRecorded(rs.getDate("DateRecorded"));
		object.setDemo(rs.getBoolean("isDemo"));
		object.setContents(rs.getString("Contents"));
		object.setSenderBrokerGuid(rs.getString("SenderBrokerGuid"));
		object.setCallbackURL(rs.getString("CallbackURL"));
		 
		return object;
	}
}
