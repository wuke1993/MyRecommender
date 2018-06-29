package org.recommender.cf.preference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.recommender.data.LearningLog;
import org.recommender.utility.PropertyHelper;
import org.recommender.utility.MySQLHelper;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170608 16:58:07
* Title   : PreferenceDuration
* Description : 
*/
public class PreferenceDuration {	
	/**
	 * 
	 * @param conn
	 * @param logs
	 * @param path1
	 * @param path2
	 */
	public static HashMap<Long, HashMap<Integer, Integer>> calPreference(Connection conn, List<LearningLog> logs, String path1, String path2) {
		HashMap<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		HashMap<Long, HashMap<Integer, Integer>> stuno_video_totalTlen = new HashMap<Long, HashMap<Integer, Integer>>();
		
		HashMap<String, Integer> videos = VideoSequenceDur.readVideo(conn); // (课程视频名, 课程视频次序)
				
		long stuno = 0;
		String title = "";
		int tlen = 0;
		Integer video_sequence = 0;
		HashMap<Integer, Integer> video_times = null;
		HashMap<Integer, Integer> video_totalTlen = null;
		for(LearningLog aLearningLog : logs) {
			if (aLearningLog.getOper() == 76) {
				stuno = aLearningLog.getStuno();
				title = aLearningLog.getTitle();
				tlen = aLearningLog.getTlen();
					
				video_sequence = videos.get(title);
				if (video_sequence != null) { // 可剔除错误日志
					if (stuno_video_times.containsKey(stuno)) { // old student
						video_times = stuno_video_times.get(stuno);
						video_totalTlen = stuno_video_totalTlen.get(stuno);
						
						if (video_times.containsKey(video_sequence)) { // watched video
							video_times.put(video_sequence, video_times.get(video_sequence) + 1);
							video_totalTlen.put(video_sequence, video_totalTlen.get(video_sequence) + tlen);
						} else { // new video
							video_times.put(video_sequence, 1);					
							video_totalTlen.put(video_sequence, tlen);
						}
					} else { // new student with new video
						video_times = new HashMap<Integer, Integer>();
						video_times.put(video_sequence, 1);
						
						video_totalTlen = new HashMap<Integer, Integer>();
						video_totalTlen.put(video_sequence, tlen);
						
						stuno_video_times.put(stuno, video_times);
						stuno_video_totalTlen.put(stuno, video_totalTlen);
					}
				}
			}
		}
		
		//System.out.println(stuno_video_times.size() + " 个学生");
		
		Map<Integer, Integer> video_dur = VideoSequenceDur.getVideosDur(conn);
		
		PreferenceDuration.storePreferenceDuration2(stuno_video_totalTlen, video_dur, path1, path2);
		//PreferenceDuration.storePreferenceDuration(stuno_video_times, stuno_video_totalTlen, path1, path2);
		//PreferenceDuration.storePreferenceDuration(stuno_video_times, stuno_video_totalTlen, video_dur, path1, path2);
		
		return stuno_video_totalTlen;
	}
	
	/**
	 * 学习者学习某视频的总时长 / 该视频自身时长。大于 3，赋值为 3，并进行规范化 [0,1]
	 * @param stuno_video_totalTlen
	 * @param video_dur 视频时长
	 * @param path1
	 * @param path2
	 */
	private static void storePreferenceDuration2(Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen, Map<Integer, Integer> video_dur, 
			String path1, String path2) {
		
		StringBuilder preference_detail = new StringBuilder();
		StringBuilder preference = new StringBuilder();
		
		long stuno = 0;
		HashMap<Integer, Integer> video_totalTlen = null;
		for (Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_totalTlen.entrySet()) {
			stuno = entry.getKey();
			video_totalTlen = entry.getValue();
			
			int sequence = 0;
			double totalTlen = 0;
			double dur = 0;
			double preferenceDuration = 0;
			for (Entry<Integer, Integer> entry2 : video_totalTlen.entrySet()) {
				sequence = entry2.getKey();
				totalTlen = entry2.getValue();
					
				dur = video_dur.get(sequence);
				
				preferenceDuration = totalTlen / dur;
				
				if (preferenceDuration > 3.0) {
					preferenceDuration = 3.0;
				}
				
				preferenceDuration /= 3;
				
				// preference_detail
				preference_detail.append(stuno);
				preference_detail.append("," + sequence);
				preference_detail.append("," + totalTlen);
				preference_detail.append("," + dur);
				preference_detail.append("\n");
				
				// preference
				preference.append(stuno);
				preference.append("," + sequence);
				preference.append("," + preferenceDuration);
				preference.append("\n");
			}
		}
		
		// stroe into file
		StoreStringIntoFile.storeString(preference_detail.toString(), path1);
		StoreStringIntoFile.storeString(preference.toString(), path2);
	}
	
	
	/*public static void test() {
		Connection conn = MySQLHelper.getConn();
		
		String tableName = "my_cs_log_stulearns_4th";
		String sql = "SELECT stuno, title, tlen FROM " + tableName + " WHERE platform = 2 AND oper = 76";
		
		String path1 = PropertyHelper.getProperty("PREFERENCE_DURATION_DETAIL_PATH");
		String path2 = PropertyHelper.getProperty("PREFERENCE_DURATION_PATH");
		PreferenceDuration.calPreference(conn, sql, path1, path2);
	}*/
	
	/**
	 * 弃用。(学习者学习某视频的总时长 / 次数) / 该视频自身时长
	 * @param stuno_video_times
	 * @param stuno_video_totalTlen
	 * @param video_dur 视频时长
	 * @param path1
	 * @param path2
	 */
	private static void storePreferenceDuration(Map<Long, HashMap<Integer, Integer>> stuno_video_times, 
			Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen, Map<Integer, Integer> video_dur, String path1, String path2) {
		
		StringBuilder preference_detail = new StringBuilder();
		StringBuilder preference = new StringBuilder();
		
		long stuno = 0;
		HashMap<Integer, Integer> video_times = null;
		for (Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_times.entrySet()) {
			stuno = entry.getKey();
			video_times = entry.getValue();
			
			int sequence = 0;
			double times = 0;
			double totalTlen = 0;
			double dur = 0;
			double preferenceDuration = 0;
			for (Entry<Integer, Integer> entry2 : video_times.entrySet()) {
				sequence = entry2.getKey();
				times = entry2.getValue();
				
				totalTlen = stuno_video_totalTlen.get(stuno).get(sequence);
				
				dur = video_dur.get(sequence);
				
				preferenceDuration = (totalTlen / times) / dur;
				
				// preference_detail
				preference_detail.append(stuno);
				preference_detail.append("," + sequence);
				preference_detail.append("," + times);
				preference_detail.append("," + totalTlen);
				preference_detail.append("," + dur);
				preference_detail.append("\n");
				
				// preference
				preference.append(stuno);
				preference.append("," + sequence);
				preference.append("," + preferenceDuration);
				preference.append("\n");
			}
		}
		
		// stroe into file
		StoreStringIntoFile.storeString(preference_detail.toString(), path1);
		StoreStringIntoFile.storeString(preference.toString(), path2);
	}
	
	/**
	 * 弃用。学习者学习某视频的平均时长 / 该视频被学习的最大平均时长
	 * @param stuno_video_times
	 * @param stuno_video_totalTlen
	 * @param path1
	 * @param path2
	 */
	private static void storePreferenceDuration(Map<Long, HashMap<Integer, Integer>> stuno_video_times, 
			Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen, String path1, String path2) {
		
		StringBuilder preference_detail = new StringBuilder();
		StringBuilder preference = new StringBuilder();
		
		double[] max_average_duration = new double[224]; // 存放每个视频被单个学习者观看的最大平均时长
		
		long stuno = 0;
		HashMap<Integer, Integer> video_times = null;
		for (Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_times.entrySet()) {
			stuno = entry.getKey();
			video_times = entry.getValue();
			
			int sequence = 0;
			double times = 0.0;
			double totalTlen = 0.0;
			double averageTlen = 0.0;
			for (Entry<Integer, Integer> entry2 : video_times.entrySet()) {
				sequence = entry2.getKey();
				times = entry2.getValue();
				
				totalTlen = stuno_video_totalTlen.get(stuno).get(sequence);
				averageTlen = totalTlen / times;
				if (averageTlen > max_average_duration[sequence - 1]) {
					max_average_duration[sequence - 1] = averageTlen;
				}
			}
		}
		
		for (Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_times.entrySet()) {
			stuno = entry.getKey();
			video_times = entry.getValue();
			
			int sequence = 0;
			double times = 0.0;
			double totalTlen = 0.0;
			double preferenceDuration = 0.0;
			for (Entry<Integer, Integer> entry2 : video_times.entrySet()) {
				sequence = entry2.getKey();
				times = entry2.getValue();
				
				totalTlen = stuno_video_totalTlen.get(stuno).get(sequence);
				
				preferenceDuration = (totalTlen / times) / max_average_duration[sequence - 1];
				
				// preference_detail
				preference_detail.append(stuno);
				preference_detail.append("," + sequence);
				preference_detail.append("," + times);
				preference_detail.append("," + totalTlen);
				preference_detail.append("," + max_average_duration[sequence - 1]);
				preference_detail.append("\n");
				
				// preference
				preference.append(stuno);
				preference.append("," + sequence);
				preference.append("," + preferenceDuration);
				preference.append("\n");
			}
		}
		
		// stroe into file
		StoreStringIntoFile.storeString(preference_detail.toString(), path1);
		StoreStringIntoFile.storeString(preference.toString(), path2);
	}
	
	/**
	 * 弃用。从数据库中读取日志数据
	 * @param conn
	 * @param sql
	 * @param path1
	 * @param path2
	 */
	public static void calPreference(Connection conn, String sql, String path1, String path2) {
		Map<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen = new HashMap<Long, HashMap<Integer, Integer>>();
		
		HashMap<String, Integer> videos = VideoSequenceDur.readVideo(conn); // (课程视频名, 课程视频次序)
				
        ResultSet rs = MySQLHelper.getResultSet(conn, sql);
		long stuno = 0;
		String title = "";
		int tlen = 0;
		Integer video_sequence = 0;
		HashMap<Integer, Integer> video_times = null;
		HashMap<Integer, Integer> video_totalTlen = null;
		try {
			while (rs.next()) {
				stuno = rs.getLong(1);
				title = rs.getString(2);
				tlen = rs.getInt(3);
				
				video_sequence = videos.get(title);
				if (video_sequence != null) {
					if (stuno_video_times.containsKey(stuno)) { // old student
						video_times = stuno_video_times.get(stuno);
						video_totalTlen = stuno_video_totalTlen.get(stuno);
						
						if (video_times.containsKey(video_sequence)) { // watched video
							video_times.put(video_sequence, video_times.get(video_sequence) + 1);
							video_totalTlen.put(video_sequence, video_totalTlen.get(video_sequence) + tlen);
						} else { // new video
							video_times.put(video_sequence, 1);					
							video_totalTlen.put(video_sequence, tlen);
						}
					} else { // new student with new video
						video_times = new HashMap<Integer, Integer>();
						video_times.put(video_sequence, 1);
						
						video_totalTlen = new HashMap<Integer, Integer>();
						video_totalTlen.put(video_sequence, tlen);
						
						stuno_video_times.put(stuno, video_times);
						stuno_video_totalTlen.put(stuno, video_totalTlen);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println(stuno_video_times.size() + " 个学生");
		
		Map<Integer, Integer> video_dur = VideoSequenceDur.getVideosDur(conn);
		
		PreferenceDuration.storePreferenceDuration(stuno_video_times, stuno_video_totalTlen, video_dur, path1, path2); // store the result
	}
}
