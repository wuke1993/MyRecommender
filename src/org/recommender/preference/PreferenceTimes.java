package org.recommender.preference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.recommender.utility.MySqlConn;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170608 16:55:08
* Title   : PreferenceTimes
* Description : 
*/
public class PreferenceTimes implements Preference {

	public static void main(String[] args) {

		Connection conn = MySqlConn.getConn();
		String tableName = "my_maozedong_sequence";
		String sql = "SELECT stuno, tlen, title, sequence FROM " + tableName;
		String path1 = "E:\\data\\DLC_forum\\recommender\\cf_preferenceTimes_detail.txt";
		String path2 = "E:\\data\\DLC_forum\\recommender\\cf_preferenceTimes.txt";
		
		PreferenceTimes preferenceTimes = new PreferenceTimes();
		preferenceTimes.calPreference(conn, sql, path1, path2);
	}

	@Override
	public void calPreference(Connection conn, String sql, String path1, String path2) {
		Map<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		Map<Long, Integer> stuno_videos_times_sum = new HashMap<Long, Integer>();
				
		Map<Long, HashSet<String>> stuno_videos = new HashMap<Long, HashSet<String>>();
        Map<Long, Integer> stuno_videos_num = new HashMap<Long, Integer>();

		ResultSet rs = MySqlConn.getResultSet(conn, sql);
		long stuno = 0;
		// int tlen = 0;
		String title = "";
		String sequence = "";
		try {
			while(rs.next()) {
				stuno = rs.getLong(1);
				// tlen = rs.getInt(2);
				title = rs.getString(3);
				sequence = rs.getString(4);

				int video_sequence = Integer.parseInt(sequence.split("\\.")[0]);
				
				// calculate stuno_video_times
				if(stuno_video_times.containsKey(stuno)) { // old student					
					HashMap<Integer, Integer> video_times = stuno_video_times.get(stuno);					
					if(video_times.containsKey(video_sequence)) { // watched video	
						video_times.put(video_sequence, video_times.get(video_sequence) + 1);
					} else { // new video
						video_times.put(video_sequence, 1);
					}
				} else { // new student with new video
					HashMap<Integer, Integer> video_times = new HashMap<Integer, Integer>();
					video_times.put(video_sequence, 1);
					
					stuno_video_times.put(stuno, video_times);
				}
				
				// calculate stuno_videos_times_sum
				if(stuno_videos_times_sum.containsKey(stuno)) {
					stuno_videos_times_sum.put(stuno, stuno_videos_times_sum.get(stuno) + 1);
				} else {
					stuno_videos_times_sum.put(stuno, 1);
				}
				
				// calculate stuno_videos
				if(stuno_videos.containsKey(stuno)) {
					stuno_videos.get(stuno).add(title);
				} else {
					HashSet<String> videos = new HashSet<String>();
					videos.add(title);
					stuno_videos.put(stuno, videos);
				}			
			}
			
			// calculate stuno_videos_num
			for(Entry<Long, HashSet<String>> entry :stuno_videos.entrySet()) {
				stuno_videos_num.put(entry.getKey(), entry.getValue().size());
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
		
		// store the result
		this.storePreferenceTimes(stuno_video_times, stuno_videos_times_sum, stuno_videos_num, path1, path2);
	}
	
	/**
	 * 
	 * @param stuno_video_times
	 * @param stuno_videos_times_sum
	 * @param stuno_videos_num
	 */
	private void storePreferenceTimes(Map<Long, HashMap<Integer, Integer>> stuno_video_times, 
			Map<Long, Integer> stuno_videos_times_sum, Map<Long, Integer> stuno_videos_num, 
			String path1, String path2) {
		
		StringBuilder preference_detail = new StringBuilder();
		StringBuilder preference = new StringBuilder();
		
		long stuno = 0;
		int video_sequence = 0;
		double video_times = 0; // double!
		double videos_times_sum = 0; // double!
		double videos_num = 0; // double!
		
		double preferenceTimes = 0;
		for(Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_times.entrySet()) {
			stuno = entry.getKey();
			HashMap<Integer, Integer> hs_video_times = entry.getValue();
			
			for(Entry<Integer, Integer> entry2 : hs_video_times.entrySet()) {
				video_sequence = entry2.getKey();
				video_times = entry2.getValue();
				
				videos_times_sum = stuno_videos_times_sum.get(stuno);
				videos_num = stuno_videos_num.get(stuno);
				
				preferenceTimes = video_times / (videos_times_sum / videos_num);
				
				preference_detail.append(stuno);
				preference_detail.append("," + video_sequence);
				preference_detail.append("," + video_times);
				preference_detail.append("," + videos_times_sum);
				preference_detail.append("," + videos_num);
				preference_detail.append("\n");
				
				preference.append(stuno);
				preference.append("," + video_sequence);
				preference.append("," + preferenceTimes);
				preference.append("\n");
			}
		}
		
		// stroe into file
		StoreStringIntoFile.storeString(preference_detail.toString(), path1);
		StoreStringIntoFile.storeString(preference.toString(), path2);
	}
	
}
