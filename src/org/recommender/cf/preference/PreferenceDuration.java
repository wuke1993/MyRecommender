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
	/*public static void main(String[] args) {

		Connection conn = MySQLHelper.getConn();
		
		String tableName = "my_cs_log_stulearns_4th";
		String sql = "SELECT stuno, title, tlen FROM " + tableName + " WHERE platform = 2 AND oper = 76";
		
		String path1 = GetProperty.getPropertyByName("PREFERENCE_DURATION_DETAIL_PATH");
		String path2 = GetProperty.getPropertyByName("PREFERENCE_DURATION_PATH");
		PreferenceDuration.calPreference(conn, sql, path1, path2);
	}*/
	
	/**
	 * 从数据库中读取日志数据
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
	
	public static void calPreference(Connection conn, List<LearningLog> logs, String path1, String path2) {
		Map<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen = new HashMap<Long, HashMap<Integer, Integer>>();
		
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
		}
		
		System.out.println(stuno_video_times.size() + " 个学生");
		
		Map<Integer, Integer> video_dur = VideoSequenceDur.getVideosDur(conn);
		
		PreferenceDuration.storePreferenceDuration(stuno_video_times, stuno_video_totalTlen, video_dur, path1, path2); // store the result
	}
	
	private static void storePreferenceDuration(Map<Long, HashMap<Integer, Integer>> stuno_video_times, Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen, 
			Map<Integer, Integer> video_dur, String path1, String path2) {
		
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
			try {
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
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// stroe into file
		StoreStringIntoFile.storeString(preference_detail.toString(), path1);
		StoreStringIntoFile.storeString(preference.toString(), path2);
	}
}
