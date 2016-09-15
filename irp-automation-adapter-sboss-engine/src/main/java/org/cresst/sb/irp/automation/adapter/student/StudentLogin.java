package org.cresst.sb.irp.automation.adapter.student;

import AIR.Common.data.ResponseData;
import org.cresst.sb.irp.automation.adapter.student.data.LoginInfo;


/**
@author Ernesto De La Luz Martinez
*/

public interface StudentLogin {
	 
	ResponseData<LoginInfo> login(String url, String sessionID, String keyValues, String forbiddenApps);
}
