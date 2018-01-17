package org.recommender.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: wuke 
 * @date  : 20160704 10:53:20
 * Title  : MySqlConn
 * Description : 连接数据库 dlc_data
 */
public class MySQLHelper {
	
	/**
	 * Return MySQL Connection.
	 * @return conn MySQL Connection
	 */
	public static Connection getConn() {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/dlc_data"
				+ "?characterEncoding=utf8&useSSL=false"
				+ "&useServerPrepStmts=false&rewriteBatchedStatements=true";
		String username = "root";
		String password = "1234";
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url,username,password);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 */
	public static ResultSet getResultSet(Connection conn, String sql) {
		PreparedStatement psmt = null;
		ResultSet rs = null;
		
		try {
			psmt = conn.prepareStatement(sql);
			rs = psmt.executeQuery();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return rs;
	}
	
	/**
	 * 
	 * @param conn
	 * @param sql
	 */
	public static void executeUpdate(Connection conn, String sql, String errorLogPath) {
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(sql);
			psmt.executeUpdate();
		} catch (SQLException e) {
			
			if (!errorLogPath.equals("")) {
				StoreStringIntoFile.storeString(sql + "\r\n", errorLogPath, true);
			} else {
				e.printStackTrace();
			}
			
		}
	}
}
