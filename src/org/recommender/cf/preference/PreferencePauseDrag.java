package org.recommender.cf.preference;

import java.sql.Connection;
import java.util.List;

import org.recommender.data.LearningLog;
import org.recommender.utility.GetProperty;
import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20170608 16:57:11
* Title   : PreferenceDrag
* Description : 
*/
public class PreferencePauseDrag implements Preference {
	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		String tableName = "my_cs_log_stulearns_4th";
		String sql = "SELECT stuno, title FROM " + tableName + " WHERE platform = 2 AND oper = 78 OR oper = 79"; // 385 个学生

		String path1 = GetProperty.getPropertyByName("PREFERENCE_PAUSE_DRAG_DETAIL_PATH");
		String path2 = GetProperty.getPropertyByName("PREFERENCE_PAUSE_DRAG_PATH");
		PreferencePauseDrag preferenceDrag = new PreferencePauseDrag();
		preferenceDrag.calPreference(conn, sql, path1, path2);
	}
	
	@Override
	/**
	 * 从数据库中读取日志数据
	 */
	public void calPreference(Connection conn, String sql, String path1, String path2) {
		PreferenceTimes preferenceTimes = new PreferenceTimes();
		preferenceTimes.calPreference(conn, sql, path1, path2);
	}
	
	/**
	 * 从处理好的 List 中读取。重载
	 * @param conn
	 * @param logs
	 * @param path1
	 * @param path2
	 */
	public void calPreference(Connection conn, List<LearningLog> logs, String path1, String path2) {
		PreferenceTimes preferenceTimes = new PreferenceTimes();
		preferenceTimes.calPreference(conn, logs, path1, path2);
	}
}