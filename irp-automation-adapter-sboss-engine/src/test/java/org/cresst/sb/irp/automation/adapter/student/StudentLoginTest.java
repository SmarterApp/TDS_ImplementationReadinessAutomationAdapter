package org.cresst.sb.irp.automation.adapter.student;

import AIR.Common.data.ResponseData;
import org.cresst.sb.irp.automation.adapter.student.data.LoginInfo;
import org.cresst.sb.irp.automation.testhelpers.IntegrationTests;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;


/**
@author Ernesto De La Luz Martinez
*/

@Ignore("TDS Server is IP restricted. Remove Ignore when machine has access to sever.")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations = { "classpath*:root-context.xml"})
public class StudentLoginTest {
	private final static Logger logger = LoggerFactory.getLogger(StudentLoginTest.class);
	
	@Autowired
	private StudentLogin studentLogin;

    @Category(IntegrationTests.class)
	@Test
	public void login(){
		//String url ="http://localhost:8080/student/Pages/API/MasterShell.axd/loginStudent";
		String url ="https://tds.smarterapp.cresst.net:8443/student/Pages/API/MasterShell.axd/loginStudent";
		logger.info("started");
		ResponseData<LoginInfo> response = studentLogin.login(url,"GUEST Session", "ID:GUEST;FirstName:GUEST", "");
		assertEquals("OK", response.getReplyText());
		logger.debug("finished");
	}
}