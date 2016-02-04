package com.wzn.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlDBUtil {

	
	public static void main(String[] args) throws ClassNotFoundException {
		long startTime = System.currentTimeMillis();
		System.out.println(execute());
		System.out.println("总耗时："+(System.currentTimeMillis()-startTime));
		
	}
	
	public static List<Map<String,Object>> execute(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Connection conn =null;
		PreparedStatement ptmt =null;
		ResultSet rs = null;
		String sql = "select t.id,t.name,t.password from tbl_user t";
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		try {
			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/testdb_01", "root", "web");
			ptmt = conn.prepareStatement(sql);
			rs = ptmt.executeQuery();
			Map<String,Object> map = new HashMap<String, Object>();
			while(rs.next()){
				map.clear();
				map.put(rs.getString(1), rs.getString(1));
				map.put(rs.getString(2), rs.getString(2));
				map.put(rs.getString(3), rs.getString(3));
				list.add(map);
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ptmt!=null){
				try {
					ptmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(conn!=null){
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return list;
	}
	


}
