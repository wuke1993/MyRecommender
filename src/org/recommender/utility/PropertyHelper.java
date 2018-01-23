package org.recommender.utility;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
* @author : wuke
* @date   : 20170610 23:57:10
* Title   : GetProperty
* Description : 
*/
public class PropertyHelper {

	public static void main(String[] args) {
		PropertyHelper.setProperty("USER_NUM", "444");
	}
	
	public static String getProperty(String name) {
		String result = "";
		
		Properties properties = new Properties();
		try {
			properties.load(PropertyHelper.class.getResource("/config.properties").openStream());
			result = properties.getProperty(name);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * TODO 未实现
	 * @param name
	 * @param value
	 */
	public static void setProperty(String name, String value) {
		Properties properties = new Properties();
		try {
			properties.load(PropertyHelper.class.getResource("/config.properties").openStream());
			
			OutputStream out = new FileOutputStream("/config.properties");
			
			properties.setProperty(name, value);
			properties.store(out, "Update " + name);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
