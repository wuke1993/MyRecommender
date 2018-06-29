package abandon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.recommender.utility.MySQLHelper;


/**
* @author : wuke
* @date   : 20180515 18:39:14
* Title   : DeleteDragLogs
* Description : 弃用
*/
public class DeleteDragLogs {

	public static void main(String[] args) {
		String tableName = "my_cs_log_stulearns_4th";
		String sql = "SELECT logid, rtime, stuno FROM " + tableName + " WHERE platform = 2 AND oper = 79 order by stuno, rtime";
		
		Connection conn = MySQLHelper.getConn();
		HashMap<Long, ArrayList<String[]>> dragLogs= new HashMap<Long, ArrayList<String[]>>();
		
		ResultSet rs = MySQLHelper.getResultSet(conn, sql);
		int logid = 0;
		Timestamp rtime = null;
		long stuno = 0;
		ArrayList<String[]> arrList = null;
		String[] arr = null;
		try {
			while (rs.next()) {
				logid = rs.getInt(1);
				rtime = rs.getTimestamp(2);
				stuno = rs.getLong(3);
				
				if (dragLogs.containsKey(stuno)) { // old student
					arrList = dragLogs.get(stuno);
					arr = new String[2];
					arr[0] = String.valueOf(logid);
					arr[1] = String.valueOf(rtime);
					arrList.add(arr);
				} else { // new student
					arrList = new ArrayList<String[]>();
					arr = new String[2];
					arr[0] = String.valueOf(logid);
					arr[1] = String.valueOf(rtime);
					arrList.add(arr);
					dragLogs.put(stuno, arrList);
				}
			}
		} catch (SQLException e) {
			System.out.println(rtime);
			e.printStackTrace();
		}
		
		/*for (Entry<Long, ArrayList<String[]>> entry : dragLogs.entrySet()) {
			System.out.print(entry.getKey() + " ");
			arrList = entry.getValue();
			for (int i = 0; i < arrList.size(); i++) {
				arr = arrList.get(i);
				System.out.print(arr[0] + " " + arr[1] + ",");
			}
			System.out.println();
		}*/
		
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		
		ArrayList<String> logidToDelete = new ArrayList<String>();
		for (Entry<Long, ArrayList<String[]>> entry : dragLogs.entrySet()) {
			arrList = entry.getValue();
			
			String[] front = null;
			String front_rtime = null;
			String[] behind = null;
			String behind_rtime = null;
			for (int i = 0; i < arrList.size(); i++) {
				front = arrList.get(i);
				if ((i + 1) < arrList.size()) {
					behind = arrList.get(i + 1);
					
					front_rtime = front[1];
					behind_rtime = behind[1];
					
				    front_rtime = front_rtime.split("[.]")[0];
					behind_rtime = behind_rtime.split("[.]")[0];
					
					try {
						//System.out.println(front_rtime + " " + behind_rtime);
						
						long from = simpleFormat.parse(front_rtime).getTime();
						long to = simpleFormat.parse(behind_rtime).getTime();
						
						//System.out.println(from + " " +to);
						
						int seconds = (int) ((to - from) / 1000);
						
						//System.out.println(seconds);
						
						if (seconds < 10) {
							logidToDelete.add(front[0]);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
	    }
		
		System.out.println(logidToDelete.size());
		
		// 批量删除
		String sql2 ="DELETE FROM my_cs_log_stulearns_4th WHERE logid = ?";
		try {
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement(sql2);
		    for (String s : logidToDelete) {
		        ps.setInt(1, Integer.parseInt(s));  
		        ps.addBatch();  
		    }
		    ps.executeBatch();
		    conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}