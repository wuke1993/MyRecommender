package org.recommender.measuring;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;

import org.recommender.utility.GetProperty;
import org.recommender.utility.MySqlConn;

/**
* @author : wuke
* @date   : 20170612 03:42:41
* Title   : TestDataReader
* Description : TODO store the result
*/
public class TestDataReader {
	
	/**
	 * 
	 * @param conn
	 * @param sql
	 */
	public static HashMap<Integer, HashSet<Integer>> readStunoVideos() {
		
		Connection conn = MySqlConn.getConn();
		
		String tableName = "my_maozedong_sequence";
		String sql = "SELECT stuno, sequence FROM " + tableName 
				+ " WHERE rtime BETWEEN \"2015-07-01 00:00:00\" AND \"2015-07-31 23:59:59\""; // We got 866 students!
		
		HashMap<Integer, HashSet<Integer>> stuno_videos = new HashMap<Integer, HashSet<Integer>>();
		
		String path = GetProperty.getPropertyByName("STUNO_SEQUENCE_PATH");
		HashMap<Long, Integer> stunos_sequences = TestDataReader.getStunoSequence(path);
		
		ResultSet rs = MySqlConn.getResultSet(conn, sql);
		long stuno = 0;
		String sequence = "";
		try {
			while(rs.next()) {
				stuno = rs.getLong(1);
				sequence = rs.getString(2);
				
				int video_sequence = Integer.parseInt(sequence.split("\\.")[0]);
				if(stunos_sequences.containsKey(stuno)) { // !!!
					int stuno_sequence = stunos_sequences.get(stuno);
					
					if(stuno_videos.containsKey(stuno_sequence)) {
						stuno_videos.get(stuno_sequence).add(video_sequence);					
					} else {
						HashSet<Integer> videos = new HashSet<Integer>();
						videos.add(video_sequence);
						
						stuno_videos.put(stuno_sequence, videos);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(stuno);
		}
		
		return stuno_videos;
		
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	private static HashMap<Long, Integer> getStunoSequence(String path) {
		HashMap<Long, Integer> stunos_sequences = new HashMap<Long, Integer>();
		
		BufferedReader reader = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			
			String str = "";
			long stuno = 0;
			int stuno_sequence = 0;
			while((str = reader.readLine()) != null) {
				String[] strArr = str.split(",");
								
				stuno_sequence = Integer.parseInt(strArr[0]);
				stuno = Long.parseLong(strArr[1]);
				
				stunos_sequences.put(stuno, stuno_sequence);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return stunos_sequences;
	}
	
}
