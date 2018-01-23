package org.recommender.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20180117 10:13:39
* Title   : TrainingSetTestSet
* Description : 
*/
public class TrainingSetTestSet {
	/**
	 * 从数据库中读取日志数据
	 * @param conn
	 * @return
	 */
	public static List<LearningLog> readLogs(Connection conn) {
		List<LearningLog> logs = new ArrayList<LearningLog>();
		
		String tableName = "my_cs_log_stulearns_4th";
		String sql = "SELECT stuno, oper, title, tlen FROM " + tableName + " WHERE platform = 2 AND oper IN (76, 78, 79) ORDER BY stuno, rtime";
		
		ResultSet rs = null;
		long stuno = 0L;
		int oper = 0;
		String title = null;
		int tlen = 0;
		LearningLog aLearningLog = null;
		try {
			rs = MySQLHelper.getResultSet(conn, sql);
			while (rs.next()) {
				stuno = rs.getLong(1);
				oper = rs.getInt(2);
				title = rs.getString(3);
				tlen = rs.getInt(4);
				
				aLearningLog = new LearningLog(stuno, oper, title, tlen);
				
				logs.add(aLearningLog);
			}
		} catch (SQLException e) {
			e.printStackTrace(); 
		}
		
		return logs;
	}
	
	/**
	 * 将读取的日志数据分为 5 份
	 * @param logs
	 * @return
	 */
	public static List<List<LearningLog>> genTrainingSetTestSet(List<LearningLog> logs) {
		List<List<LearningLog>> logsAll = new ArrayList<List<LearningLog>>();
		
		List<LearningLog> logs_1th = new ArrayList<LearningLog>();
		List<LearningLog> logs_2th = new ArrayList<LearningLog>();
		List<LearningLog> logs_3th = new ArrayList<LearningLog>();
		List<LearningLog> logs_4th = new ArrayList<LearningLog>();
		List<LearningLog> logs_5th = new ArrayList<LearningLog>();
		
		logsAll.add(logs_1th);
		logsAll.add(logs_2th);
		logsAll.add(logs_3th);
		logsAll.add(logs_4th);
		logsAll.add(logs_5th);
		
		int i = 1;
		for (LearningLog aLearningLog : logs) {
			logsAll.get((i - 1) % 5).add(aLearningLog);
			i++;
		}
		
		return logsAll;
	}
	
	/**
	 * 从文件（计算评分阶段所生成的文件）中读取学习者的序号
	 * @param path
	 * @return
	 */
	public static HashMap<Long, Integer> getStunoSequence(String path) {
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
	
	/**
	 * 处理测试集，只保留训练集中包含的学习者及其记录
	 * @param testSet
	 * @param stunos_sequences
	 * @param videos_sequences
	 * @return
	 */
	public static HashMap<Integer, HashSet<Integer>> processTestSet(List<LearningLog> testSet, HashMap<Long, Integer> stunos_sequences, 
			HashMap<String, Integer> videos_sequences) {
		HashMap<Integer, HashSet<Integer>> stuno_videos = new HashMap<Integer, HashSet<Integer>>();
		
		long stuno = 0;
		String title = "";
		int stuno_sequence = 0;
		Integer video_sequence = 0;
		HashSet<Integer> videos = null;
		for (LearningLog aLearningLog : testSet) {
			stuno = aLearningLog.getStuno();
			title = aLearningLog.getTitle();
			
			video_sequence = videos_sequences.get(title);
			if (video_sequence != null) {
				if (stunos_sequences.containsKey(stuno)) { // 训练集中包含测试集的当前用户
					stuno_sequence = stunos_sequences.get(stuno);
					
					if (stuno_videos.containsKey(stuno_sequence)) {
						stuno_videos.get(stuno_sequence).add(video_sequence);
					} else {
						videos = new HashSet<Integer>();
						videos.add(video_sequence);
						
						stuno_videos.put(stuno_sequence, videos);
					}
				}
			}
		}
		
		return stuno_videos;
	}
	
	/**
	 * 从数据库中读取测试集。弃用
	 * @param conn
	 * @param stunos_sequences
	 * @return
	 */
    public static HashMap<Integer, HashSet<Integer>> readStunoVideos(Connection conn, HashMap<Long, Integer> stunos_sequences) {
		String tableName = "my_maozedong_sequence";
		String sql = "SELECT stuno, sequence FROM " + tableName 
				+ " WHERE rtime BETWEEN \"2015-07-01 00:00:00\" AND \"2015-08-31 23:59:59\"";
		
		HashMap<Integer, HashSet<Integer>> stuno_videos = new HashMap<Integer, HashSet<Integer>>();
		
		ResultSet rs = MySQLHelper.getResultSet(conn, sql);
		long stuno = 0;
		String sequence = "";
		try {
			while (rs.next()) {
				stuno = rs.getLong(1);
				sequence = rs.getString(2);
				
				int video_sequence = Integer.parseInt(sequence.split("\\.")[0]);
				if (stunos_sequences.containsKey(stuno)) { // !!!
					int stuno_sequence = stunos_sequences.get(stuno);
					
					if (stuno_videos.containsKey(stuno_sequence)) {
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
}
