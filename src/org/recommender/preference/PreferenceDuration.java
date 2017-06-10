package org.recommender.preference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.recommender.utility.MySqlConn;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170608 16:58:07
* Title   : PreferenceDuration
* Description : 
*/
public class PreferenceDuration implements Preference {

	public static void main(String[] args) {

		Connection conn = MySqlConn.getConn();
		String tableName = "my_maozedong_sequence";
		String sql = "SELECT stuno, tlen, title, sequence FROM " + tableName;
		String path1 = "E:\\data\\DLC_forum\\recommender\\cf_preferenceDuration_detail.txt";
		String path2 = "E:\\data\\DLC_forum\\recommender\\cf_preferenceDuration.txt";
		
		PreferenceDuration preferenceDuration = new PreferenceDuration();
		preferenceDuration.calPreference(conn, sql, path1, path2);
	}
	
	@Override
	public void calPreference(Connection conn, String sql, String path1, String path2) {
		Map<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen = new HashMap<Long, HashMap<Integer, Integer>>();
				
        ResultSet rs = MySqlConn.getResultSet(conn, sql);
		long stuno = 0;
		int tlen = 0;
		String title = "";
		String sequence = "";
		try {
			while(rs.next()) {
				stuno = rs.getLong(1);
				tlen = rs.getInt(2);
				title = rs.getString(3);
				sequence = rs.getString(4);

				int video_sequence = Integer.parseInt(sequence.split("\\.")[0]);
				
				// calculate stuno_video_times and stuno_video_totalTlen
				if(stuno_video_times.containsKey(stuno)) { // old student
					HashMap<Integer, Integer> video_times = stuno_video_times.get(stuno);
					HashMap<Integer, Integer> video_totalTlen = stuno_video_totalTlen.get(stuno);
					
					if(video_times.containsKey(video_sequence)) { // watched video
						video_times.put(video_sequence, video_times.get(video_sequence) + 1);						
						video_totalTlen.put(video_sequence, video_totalTlen.get(video_sequence) + tlen);
					} else { // new video
						video_times.put(video_sequence, 1);					
						video_totalTlen.put(video_sequence, tlen);
					}
				} else { // new student with new video
					HashMap<Integer, Integer> video_times = new HashMap<Integer, Integer>();
					video_times.put(video_sequence, 1);
					
					HashMap<Integer, Integer> video_totalTlen = new HashMap<Integer, Integer>();
					video_totalTlen.put(video_sequence, tlen);
					
					stuno_video_times.put(stuno, video_times);
					stuno_video_totalTlen.put(stuno, video_totalTlen);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String sql2 = "SELECT dur, urlppt FROM my_maozedong_kj_courseitems";
		Map<Integer, Integer> videos_dur = PreferenceDuration.getVideosDur(conn, sql2);
		
		// store the result
		this.storePreferenceDuration(stuno_video_times, stuno_video_totalTlen, videos_dur, path1, path2);
	}

	/**
	 * Get videos' duration from table 'my_maozedong_kj_courseitems'.
	 * @param conn
	 * @param sql
	 * @return
	 */
	private static Map<Integer, Integer> getVideosDur(Connection conn, String sql) {
		Map<Integer, Integer> videos_dur = new HashMap<Integer, Integer>();
		
		ResultSet rs = MySqlConn.getResultSet(conn, sql);
		
		int dur = 0;
		String sequence = "";
		try {
			while(rs.next()) {
				dur = rs.getInt(1);
				sequence = rs.getString(2);
				
				int video_sequence = Integer.parseInt(sequence.split("\\.")[0]);
				
				videos_dur.put(video_sequence, dur);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return videos_dur;
	}
	
	/**
	 * 
	 * @param stuno_video_times
	 * @param stuno_video_totalTlen
	 * @param videos_dur
	 * @param path1
	 * @param path2
	 */
	private void storePreferenceDuration(Map<Long, HashMap<Integer, Integer>> stuno_video_times, 
			Map<Long, HashMap<Integer, Integer>> stuno_video_totalTlen, 
			Map<Integer, Integer> videos_dur, String path1, String path2) {
		
		StringBuilder preference_detail = new StringBuilder();
		StringBuilder preference = new StringBuilder();
		
		long stuno = 0;
		int video_sequence = 0;
		double video_totalTlen = 0; // double!
		double video_times = 0; // double!
		double video_dur = 0; // double!
		
		double preferenceDuration = 0;
		for(Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_times.entrySet()) {
			stuno = entry.getKey();
			HashMap<Integer, Integer> hs_video_times = entry.getValue();
			
			try {
			
			for(Entry<Integer, Integer> entry2 : hs_video_times.entrySet()) {
				video_sequence = entry2.getKey();
				video_times = entry2.getValue();
								
				video_totalTlen = stuno_video_totalTlen.get(stuno).get(video_sequence);
				video_dur = videos_dur.get(video_sequence);
				
				preferenceDuration = (video_totalTlen / video_times) / video_dur;
				
				preference_detail.append(stuno);
				preference_detail.append("," + video_sequence);
				preference_detail.append("," + video_totalTlen);
				preference_detail.append("," + video_times);
				preference_detail.append("," + video_dur);
				preference_detail.append("\n");
				
				preference.append(stuno);
				preference.append("," + video_sequence);
				preference.append("," + preferenceDuration);
				preference.append("\n");
			}
			
			} catch(Exception e) {
				// e.printStackTrace();
				System.out.println(stuno + " *** " + video_sequence);
			}
		}
		
		// stroe into file
		StoreStringIntoFile.storeString(preference_detail.toString(), path1);
		StoreStringIntoFile.storeString(preference.toString(), path2);
	}
	
}
