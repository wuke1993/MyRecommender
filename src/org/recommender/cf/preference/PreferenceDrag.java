package org.recommender.cf.preference;

import java.sql.Connection;

import org.recommender.utility.MySqlConn;

/**
* @author : wuke
* @date   : 20170608 16:57:11
* Title   : PreferenceDrag
* Description : 
*/
public class PreferenceDrag implements Preference {

	public static void main(String[] args) {

		Connection conn = MySqlConn.getConn();
		String tableName = "my_maozedong_drag_sequence";
		// String sql = "SELECT stuno, tlen, title, sequence FROM " + tableName;
		String sql = "SELECT stuno, tlen, title, sequence FROM " + tableName
				+ " WHERE rtime BETWEEN \"2015-02-01 00:00:00\" AND \"2015-07-31 23:59:59\""; // We got 1160 students!
		String path1 = "E:\\data\\DLC_forum\\recommender\\cf_preferenceDrag_detail.txt";
		String path2 = "E:\\data\\DLC_forum\\recommender\\cf_preferenceDrag.txt";
		
		PreferenceDrag preferenceDrag = new PreferenceDrag();
		preferenceDrag.calPreference(conn, sql, path1, path2);
	}
	
	@Override
	public void calPreference(Connection conn, String sql, String path1, String path2) {
		PreferenceTimes preferenceTimes = new PreferenceTimes();
		preferenceTimes.calPreference(conn, sql, path1, path2);
	}
	
}
