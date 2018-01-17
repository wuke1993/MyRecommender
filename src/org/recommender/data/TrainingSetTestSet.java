package org.recommender.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20180117 10:13:39
* Title   : TrainingSetTestSet
* Description : 
*/
public class TrainingSetTestSet {

	public static void main(String[] args) {
        Connection conn = MySQLHelper.getConn();
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs);
	}
	
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
}
