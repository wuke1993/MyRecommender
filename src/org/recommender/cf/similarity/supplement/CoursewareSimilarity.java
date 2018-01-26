package org.recommender.cf.similarity.supplement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.recommender.data.TrainingSetTestSet;
import org.recommender.utility.MySQLHelper;
import org.recommender.utility.PropertyHelper;

/**
* @author : wuke
* @date   : 20180124  10:45:05
* Title   : PreferenceCourseware
* Description : 学习者观看课件的次数
*/
public class CoursewareSimilarity {
	public static HashMap<Integer, Integer> genCoursewareTimes(Connection conn) {
		HashMap<Integer, Integer> result = null;
		
		HashMap<Long, Integer> stuno_coursewareTimes = CoursewareSimilarity.readCoursewareTimes(conn);
		HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(PropertyHelper.getProperty("PREFERENCE_PATH"));
		
		result = CoursewareSimilarity.selectCoursewareTimes(stuno_coursewareTimes, stunos_sequences);
		return result;
	}
	private static HashMap<Long, Integer> readCoursewareTimes(Connection conn) {
		HashMap<Long, Integer> stuno_coursewareTimes = new HashMap<Long, Integer>();
		
		String sql = "SELECT stuno, COUNT(*) AS times FROM my_cs_log_stulearns_4th WHERE 1 = 1 AND platform = 2 AND oper = 4 GROUP BY stuno";
		ResultSet rs = MySQLHelper.getResultSet(conn, sql);
		long stuno = 0L;
		int times = 0;
		try {
			while (rs.next()) {
				stuno = rs.getLong(1);
				times = rs.getInt(2);
				
				stuno_coursewareTimes.put(stuno, times);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return stuno_coursewareTimes;
	}
	
	/**
	 * 筛选训练集中包含的学习者的课件学习记录
	 * @param stuno_coursewareTimes
	 * @param stunos_sequences
	 * @return
	 */
	private static HashMap<Integer, Integer> selectCoursewareTimes(HashMap<Long, Integer> stuno_coursewareTimes, HashMap<Long, Integer> stunos_sequences) {
		HashMap<Integer, Integer> select_result = new HashMap<Integer, Integer>();
		
		int max_times = 0;
		
		long stuno = 0L;
		int times = 0;
		int stuno_sequence = 0;
		for (Entry<Long, Integer> entry : stuno_coursewareTimes.entrySet()) {
			stuno = entry.getKey();
			times = entry.getValue();
			
			if (stunos_sequences.containsKey(stuno)) {
				stuno_sequence = stunos_sequences.get(stuno);
				
				select_result.put(stuno_sequence, times);
			}
			
			if (times > max_times) {
				max_times = times;
			}
		}
		
		return select_result;
	}
}