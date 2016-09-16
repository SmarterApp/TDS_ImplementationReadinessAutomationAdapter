/*************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package org.cresst.sb.irp.automation.adapter.student.data;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * @author mskhan
 * 
 */

public class LoginInfo {

	private TestSession                         _session;
	private Testee                              _testee;
	private UUID                                _token;
	private long                                _timestamp;
	private String                              _returnUrl;
	private String                              _appName;
	private String                              _clientName;
	private String                              _testeeID;
	private String                              _sessionID;
	private List<String>                        _validationErrors;
	private List<String> _grades;
	private GeoComponent                        _satellite;
	private List<LoginInfoAccommocation>        _globalAccs;

	@JsonProperty("proctor")
	private Object                              _proctor = null;

	private UUID createToken () {
		// TODO sajib
		return UUID.randomUUID ();
	}

	private long createTimestamp () {
		// TODO Shaji/Shiva
		// TimeSpan timeSpan = DateTime.UtcNow - new DateTime (1970, 1, 1, 0, 0, 0,
		// 0, DateTimeKind.Utc);
		// Convert.ToInt64 (timeSpan.TotalSeconds);
		return Calendar.getInstance ().getTimeInMillis ();
	}

	// / <summary>
	// / Check if the login data is valid.
	// / </summary>
	// / <returns></returns>
	public boolean isValid () {
		// TODO Shajib
		// if proctor object exists and fails validation, return false
    /*
     * if (Proctor != null && !Proctor.IsValid ()) return false;
     *
     * return Token != Guid.Empty && Token == CreateToken ();
     */
		return true;
	}

	// This is only required for deserialization purposes.
	// TODO Shiva: try it again.
	public void setValid (boolean validFlag) {
		// DO nothing.
	}

	// / <summary>
	// / Check if the login data is expired.
	// / </summary>
	public boolean isExpired () {
		long now = createTimestamp ();
		long diffSecs = now - _timestamp;
		// TODO Shajib
		return diffSecs > 15 * 60 * 1000/* _defaultExpSecs */;
	}

	public void setExpired (boolean value) {
		// TODO Shiva: are we sending expired in the .NET version.
		// If so then leave this empty.
	}

	public LoginInfo () {
		_validationErrors = new ArrayList<String>();
	}

	public void addValidationMessage (String error) {
		_validationErrors.add (error);
	}

	public void addValidationError (String error) {
		_validationErrors.add (error);
	}

	// / <summary>
	// / The session data loaded from the db.
	// / </summary>
	@JsonProperty ("session")
	public TestSession getSession () {
		return _session;
	}

	public void setSession (TestSession _session) {
		this._session = _session;
	}

	@JsonProperty ("testee")
	public Testee getTestee () {
		return _testee;
	}

	public void setTestee (Testee _testee) {
		this._testee = _testee;
	}

	@JsonProperty ("token")
	public UUID getToken () {
		return _token;
	}

	public void setToken (UUID value) {
		this._token = value;
	}

	@JsonProperty ("timestamp")
	public long getTimestamp () {
		return _timestamp;
	}

	@JsonProperty ("returnUrl")
	public String getReturnUrl () {
		return _returnUrl;
	}

	public void setReturnUrl (String value) {
		this._returnUrl = value;
	}

	@JsonProperty ("appName")
	public String getAppName () {
		return _appName;
	}

	public void setAppName (String value) {
		this._appName = value;
	}

	@JsonProperty ("client")
	public String getClientName () {
		return _clientName;
	}

	public void setClientName (String value) {
		this._clientName = value;
	}

	@JsonProperty ("testeeID")
	public String getTesteeID () {
		return _testeeID;
	}

	public void setTesteeID (String _testeeID) {
		this._testeeID = _testeeID;
	}

	@JsonProperty ("sessionID")
	public String getSessionID () {
		return _sessionID;
	}

	public void setSessionID (String _sessionID) {
		this._sessionID = _sessionID;
	}

	@JsonProperty ("errors")
	public List<String> getValidationErrors () {
		return _validationErrors;
	}

	public void setValidationErrors (List<String> _validationErrors) {
		this._validationErrors = _validationErrors;
	}

	@JsonProperty ("grades")
	public List<String> getGrades () {
		return _grades;
	}

	public void setGrades (List<String> _grades) {
		this._grades = _grades;
	}

	@JsonProperty ("globalAccs")
	public List<LoginInfoAccommocation> getGlobalAccs () {
		return _globalAccs;
	}

	public void setGlobalAccs (List<LoginInfoAccommocation> _globalAccs) {
		this._globalAccs = _globalAccs;
	}

	@JsonProperty ("satellite")
	public GeoComponent getSatellite () {
		return _satellite;
	}

	public void setSatellite (GeoComponent _satellite) {
		this._satellite = _satellite;
	}

	public void setTimestamp (long _timestamp) {
		this._timestamp = _timestamp;
	}
}
