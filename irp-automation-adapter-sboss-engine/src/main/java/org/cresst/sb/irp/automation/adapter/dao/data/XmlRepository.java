package org.cresst.sb.irp.automation.adapter.dao.data;

import java.util.Date;

public class XmlRepository {
	private int fileID;
	private String Location;
	private String TestName;
	private int OppId;
	private int efkTestee;
	private Date StatusDate;
	private Date DateRecorded;
	private boolean isDemo;
	private String Contents;
	private String SenderBrokerGuid;
	private String CallbackURL;
	
	@Override
	public String toString() {		
		return "fileID: " + fileID 
				+ ",  Location: "+ Location 
				+ ",  TestName: " + TestName
				+ ",  OppId: " + OppId
				+ ",  efkTestee: " + efkTestee
				+ ",  StatusDate: " + StatusDate
				+ ",  DateRecorded: " + DateRecorded
				+ ",  isDemo: " + isDemo
				//+ ",  Contents: " + Contents
				+ ",  SenderBrokerGuid: " + SenderBrokerGuid
				+ ",  CallbackURL: CallbackURL" ;
	}
	
	public int getFileID() {
		return fileID;
	}
	public void setFileID(int fileID) {
		this.fileID = fileID;
	}
	public String getLocation() {
		return Location;
	}
	public void setLocation(String location) {
		Location = location;
	}
	public String getTestName() {
		return TestName;
	}
	public void setTestName(String testName) {
		TestName = testName;
	}
	public int getOppId() {
		return OppId;
	}
	public void setOppId(int oppId) {
		OppId = oppId;
	}
	public int getEfkTestee() {
		return efkTestee;
	}
	public void setEfkTestee(int efkTestee) {
		this.efkTestee = efkTestee;
	}
	public Date getStatusDate() {
		return StatusDate;
	}
	public void setStatusDate(Date statusDate) {
		StatusDate = statusDate;
	}
	public Date getDateRecorded() {
		return DateRecorded;
	}
	public void setDateRecorded(Date dateRecorded) {
		DateRecorded = dateRecorded;
	}
	public boolean isDemo() {
		return isDemo;
	}
	public void setDemo(boolean isDemo) {
		this.isDemo = isDemo;
	}
	public String getContents() {
		return Contents;
	}
	public void setContents(String contents) {
		Contents = contents;
	}
	public String getSenderBrokerGuid() {
		return SenderBrokerGuid;
	}
	public void setSenderBrokerGuid(String senderBrokerGuid) {
		SenderBrokerGuid = senderBrokerGuid;
	}
	public String getCallbackURL() {
		return CallbackURL;
	}
	public void setCallbackURL(String callbackURL) {
		CallbackURL = callbackURL;
	}
	
	
}
