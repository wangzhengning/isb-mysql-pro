package com.wzn.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OracleDBUtil {

	
	public static void main(String[] args) throws ClassNotFoundException {
		long startTime = System.currentTimeMillis();
		execute();
		System.out.println("总耗时："+(System.currentTimeMillis()-startTime));
		
	}
	
	public static List<String> execute(){
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<String> list = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement ptmt =null;
		ResultSet rs = null;
		try {
			String sql = 
			"select t1.DEVID"+
			  " from net_dev_catalog  t1,"+
			       " gps_b_info       t2,"+
			       " gps_d_policetype t3,"+
			       " gps_d_localtype  t4,"+
			       " gps_d_styletype  t5"+
			 " WHERE t1.devid = t2.devid"+
			  " and t2.policetypeid = t3.policetypeid"+
			  " and t2.localtypeid = t4.localtypeid"+
			  " and t2.styletypeid = t5.styletypeid";

			System.out.println(sql);
			conn = DriverManager.getConnection("jdbc:oracle:thin:@172.18.68.69:1521:orcl", "ezdevdata", "ezdevdata");
			ptmt = conn.prepareStatement(sql);
			rs = ptmt.executeQuery();
			while(rs.next()){
				/*rs.getString(1);*/
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ptmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
}
