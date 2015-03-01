package ramsden.ryan;

import java.sql.*;

public class SQL 
{ 
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost:3306/project";

	static final String USER = "root";
	static final String PASS = "";

	private Connection conn = null;
	private Statement stmt = null;

	public SQL()
	{

	}

	public String getValue(String query, String columnLabel)
	{
		try {
			ResultSet rs = query(query);
			while(rs.next()) {
				String s = rs.getString(columnLabel);
				rs.close();
				return s;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public ResultSet query(String query)
	{
		if(isConnected()) {
			try {
				ResultSet rs = stmt.executeQuery(query);
				return rs;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public void updateQuery(String query)
	{
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean connect() {
		if(!isConnected()) {
			try{
				Class.forName("com.mysql.jdbc.Driver");
				System.out.println("Connecting to database...");
				conn = DriverManager.getConnection(DB_URL,USER,PASS);
				System.out.println("Creating statement...");
				stmt = conn.createStatement();
			} catch(SQLException se) {
				//se.printStackTrace();
				return false;
			} catch(Exception e) {
				//e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public boolean isConnected()
	{
		if(conn != null) {
			try {
				return !conn.isClosed();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean close()
	{
		try {
			if(conn!=null) {
				conn.close();
				return true;
			}
			if(stmt!=null) {
				stmt.close();
				return true;
			}
		} catch(SQLException se) {
			se.printStackTrace();
		}
		return false;
	}
}