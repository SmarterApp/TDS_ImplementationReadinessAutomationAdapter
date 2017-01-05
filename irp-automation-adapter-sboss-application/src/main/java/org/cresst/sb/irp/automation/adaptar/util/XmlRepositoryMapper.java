package org.cresst.sb.irp.automation.adaptar.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cresst.sb.irp.automation.adapter.web.domain.XmlRepository;
import org.springframework.jdbc.core.RowMapper;

public class XmlRepositoryMapper  implements RowMapper<XmlRepository> {
	public XmlRepository mapRow(ResultSet rs, int rowNum) throws SQLException {  
		XmlRepository object = new XmlRepository();
		object.setFileID(rs.getInt("FileID"));
		
		return object;
	}
}
