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
 * Description : 
 */
public class MySqlConn {
	
	/**
	 * Return MySQL Connection.
	 * @return conn MySQL Connection
	 */
	public static Connection getConn() {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/logstat2015_new_backup?characterEncoding=utf8&useSSL=false";
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
	 * @param sql
	 * @return rs ResultSet
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
	
	public static void executeUpdate(Connection conn, String sql) {
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(sql);
			psmt.executeUpdate();
			System.out.println("Successfully update!");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
