package org.recommender.cf.preference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.recommender.data.LearningLog;
import org.recommender.utility.MySQLHelper;
import org.recommender.utility.PropertyHelper;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170608 16:55:08
* Title   : PreferenceTimes
* Description : 学习者学习某个视频的总次数 / 该视频被单个学习者学习的最大次数
*/
public class PreferenceTimes {
	public static HashMap<Long, HashMap<Integer, Integer>> calPreference(Connection conn, List<LearningLog> logs, String path1, String path2) {
		HashMap<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		
		HashMap<String, Integer> videos = VideoSequenceDur.readVideo(conn); // (课程视频名, 课程视频次序)

		long stuno = 0;
		String title = "";
		Integer video_sequence = 0;
		HashMap<Integer, Integer> video_times = null;
		
		for(LearningLog aLearningLog : logs) {
			if (aLearningLog.getOper() == 76) {
				stuno = aLearningLog.getStuno();
				title = aLearningLog.getTitle();
				
				video_sequence = videos.get(title);
				if (video_sequence != null) {
					if (stuno_video_times.containsKey(stuno)) { // old student
						video_times = stuno_video_times.get(stuno);				
						if (video_times.containsKey(video_sequence)) { // watched video
							video_times.put(video_sequence, video_times.get(video_sequence) + 1);
						} else { // new video
							video_times.put(video_sequence, 1);
						}
					} else { // new student with new video
						video_times = new HashMap<Integer, Integer>();
						video_times.put(video_sequence, 1);
						
						stuno_video_times.put(stuno, video_times);
					}
				}
			}
		}
		
		//System.out.println(stuno_video_times.size() + " 个学生");
		
		PreferenceTimes.storePreferenceTimes(stuno_video_times, path1, path2);
		
		return stuno_video_times;
	}
		
	public static void storePreferenceTimes(Map<Long, HashMap<Integer, Integer>> stuno_video_times, String path1, String path2) {
		
		StringBuilder preference_detail = new StringBuilder();
		StringBuilder preference = new StringBuilder();
		
		int[] max_video_times = new int[224]; // 存放每个视频被单个学习者观看的最大次数
		
		long stuno = 0;
		for (Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_times.entrySet()) {
			stuno = entry.getKey();
			HashMap<Integer, Integer> video_times = entry.getValue();
			
			int sequence = 0;
			int times = 0;
			for (Entry<Integer, Integer> entry2 : video_times.entrySet()) {
				sequence = entry2.getKey();
				times = entry2.getValue();
				if (times > max_video_times[sequence - 1]) {
					max_video_times[sequence - 1] = times;
				}
			}
		}
		
		for (Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_times.entrySet()) {
			stuno = entry.getKey();
			HashMap<Integer, Integer> video_times = entry.getValue();

			int sequence = 0;
			double times = 0.0;
			double preferenceTimes = 0;
			for (Entry<Integer, Integer> entry2 : video_times.entrySet()) {
				sequence = entry2.getKey();
				times = entry2.getValue();
				
				preferenceTimes = times / max_video_times[sequence - 1];
				
				preference_detail.append(stuno);
				preference_detail.append("," + sequence);
				preference_detail.append("," + times);
				preference_detail.append("," + max_video_times[sequence - 1]);
				preference_detail.append("\n");
				
				preference.append(stuno);
				preference.append("," + sequence);
				preference.append("," + preferenceTimes);
				preference.append("\n");
			}
		}
		
		// stroe into file
		StoreStringIntoFile.storeString(preference_detail.toString(), path1);
		StoreStringIntoFile.storeString(preference.toString(), path2);
	}
	
	public static void test() {
		Connection conn = MySQLHelper.getConn();
		
		String tableName = "my_cs_log_stulearns_4th";
		String sql = "SELECT stuno, title FROM " + tableName + " WHERE platform = 2 AND oper = 76";
		
		String path1 = PropertyHelper.getProperty("PREFERENCE_TIMES_DETAIL_PATH");
		String path2 = PropertyHelper.getProperty("PREFERENCE_TIMES_PATH");
		PreferenceTimes.calPreference(conn, sql, path1, path2);
	}
	
	/**
	 * 弃用。从数据库中读取日志数据
	 */
	public static void calPreference(Connection conn, String sql, String path1, String path2) {
		Map<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		
		HashMap<String, Integer> videos = VideoSequenceDur.readVideo(conn); // (课程视频名, 课程视频次序)

		ResultSet rs = MySQLHelper.getResultSet(conn, sql);
		long stuno = 0;
		String title = "";
		Integer video_sequence = 0;
		HashMap<Integer, Integer> video_times = null;
		try {
			while (rs.next()) {
				stuno = rs.getLong(1);
				title = rs.getString(2);
				
				video_sequence = videos.get(title);
				if (video_sequence != null) {
					if (stuno_video_times.containsKey(stuno)) { // old student
						video_times = stuno_video_times.get(stuno);				
						if (video_times.containsKey(video_sequence)) { // watched video
							video_times.put(video_sequence, video_times.get(video_sequence) + 1);
						} else { // new video
							video_times.put(video_sequence, 1);
						}
					} else { // new student with new video
						video_times = new HashMap<Integer, Integer>();
						video_times.put(video_sequence, 1);
						
						stuno_video_times.put(stuno, video_times);
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		//System.out.println(stuno_video_times.size() + " 个学生");
		
		PreferenceTimes.storePreferenceTimes(stuno_video_times, path1, path2);
	}
}
