package org.cresst.sb.irp.automation.adaptar.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class AdapterUtils {
	private AdapterUtils() {}
	
	
	public static Date parseStringToDate(String value){
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	Date inputDate = null;
		try {
			inputDate = dateFormat.parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return inputDate;
	}
}
