package org.recommender.cf.preference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20180115 17:13:46
* Title   : VideoSequence
* Description : 读取 (课程视频名, 课程视频次序)
*/
public class VideoSequenceDur {
	private static String SQL_OS = "SELECT item_title, urlppt FROM my_kj_courseitems WHERE courseid = 23";
	private static String SQL_CN = "SELECT item_title, urlppt FROM my_kj_courseitems WHERE courseid = 182";
	private static String SQL_JAVA = "SELECT item_title, urlppt FROM my_kj_courseitems WHERE courseid = 5";
	
	private static String SQL_OS_DUR = "SELECT urlppt, dur FROM my_kj_courseitems WHERE courseid = 23";
	private static String SQL_CN_DUR = "SELECT urlppt, dur FROM my_kj_courseitems WHERE courseid = 182";
	private static String SQL_JAVA_DUR = "SELECT urlppt, dur FROM my_kj_courseitems WHERE courseid = 5";

	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		HashMap<String, Integer> video_sequence = VideoSequenceDur.readVideo(conn);
		Map<Integer, Integer> video_dur = VideoSequenceDur.getVideosDur(conn);
		
		for (Entry<String, Integer> entry : video_sequence.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		
		for (Entry<Integer, Integer> entry : video_dur.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}

	public static HashMap<String, Integer> readVideo(Connection conn) {
		HashMap<String, Integer> video_sequence = new HashMap<String, Integer>();
		
		String name = "";
		int sequence = 0;
		ResultSet rs = null;
		try {
			rs = MySQLHelper.getResultSet(conn, SQL_OS);
			while(rs.next()) {
				name = rs.getString(1);
				sequence = Integer.parseInt(rs.getString(2).split("\\.")[0]);
				
				video_sequence.put(name, sequence);
			}
			
			rs = MySQLHelper.getResultSet(conn, SQL_CN);
			while(rs.next()) {
				name = rs.getString(1);
				sequence = Integer.parseInt(rs.getString(2).split("\\.")[0]) + 100;
				
				video_sequence.put(name, sequence);
			}
			
			rs = MySQLHelper.getResultSet(conn, SQL_JAVA);
			while(rs.next()) {
				name = rs.getString(1);
				sequence = Integer.parseInt(rs.getString(2).split("\\.")[0]) + 154;
				
				video_sequence.put(name, sequence);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return video_sequence;
	}
	
	public static Map<Integer, Integer> getVideosDur(Connection conn) {
		Map<Integer, Integer> video_dur = new HashMap<Integer, Integer>();
		
		int sequence = 0;
		int dur = 0;
		ResultSet rs = null;
		try {
			rs = MySQLHelper.getResultSet(conn, SQL_OS_DUR);
			while(rs.next()) {
				sequence = Integer.parseInt(rs.getString(1).split("\\.")[0]);
				dur = rs.getInt(2);
				
				video_dur.put(sequence, dur);
			}
			
			rs = MySQLHelper.getResultSet(conn, SQL_CN_DUR);
			while(rs.next()) {
				sequence = Integer.parseInt(rs.getString(1).split("\\.")[0]) + 100;
				dur = rs.getInt(2);
				
				video_dur.put(sequence, dur);
			}
			
			rs = MySQLHelper.getResultSet(conn, SQL_JAVA_DUR);
			while(rs.next()) {
				sequence = Integer.parseInt(rs.getString(1).split("\\.")[0]) + 154;
				dur = rs.getInt(2);
				
				video_dur.put(sequence, dur);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return video_dur;
	}
}
