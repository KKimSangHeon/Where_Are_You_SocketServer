package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class UpdataDB extends Thread{
	ArrayList<QueryData> ALQueryData;
	public UpdataDB(ArrayList<QueryData> ALQueryData){
		this.ALQueryData=(ArrayList<QueryData>)ALQueryData.clone();
		
	}
	public void myExecuteQuery(QueryData data){
		Connection con = null;
		Statement stmt = null;

		String jdbcURL = "jdbc:mysql://localhost:3306/locationdb?useSSL=true";
		String dbID = "root";
		String dbPW = "1501"; // 

		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(jdbcURL, dbID, dbPW);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	

		String strSQL = "SELECT name FROM studentdata where name='" + data.name + "'and classid='" + data.classid + "'";
		String strSQL2="INSERT INTO allstudentdata VALUES('" + data.name + "'," + data.coordinate_x + "," + data.coordinate_y + ",'"
				+ data.classid + "',now());"; 
		
		ResultSet rs;
		int queryresult;
		int queryresult2;
		String queryname = null;

		try {
			rs = stmt.executeQuery(strSQL);

			while (rs.next()) {
				queryname = rs.getString("name");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (data.name.equals(queryname) == true) // exist name in db
		{
			if (data.checker == false) // request delete
			{
				strSQL = "DELETE FROM studentdata where name='" + data.name + "'and classid='" + data.classid + "'";
				strSQL2="DELETE FROM allstudentdata where name='" + data.name + "'and classid='" + data.classid + "'";
				System.out.println(data.name+" delete.");
			} else { // request update

				strSQL = "UPDATE studentdata SET coordinate_x=" + data.coordinate_x + ",coordinate_y=" + data.coordinate_y
						+ ",date=now() WHERE NAME='" + data.name + "' AND classid='" + data.classid + "';";
			}

		} else // request insert
		{
			strSQL = "INSERT INTO studentdata VALUES('" + data.name + "'," + data.coordinate_x + "," + data.coordinate_y + ",'"
					+ data.classid + "',now());";
			strSQL2="INSERT INTO allstudentdata VALUES('" + data.name + "'," + data.coordinate_x + "," + data.coordinate_y + ",'"
					+ data.classid + "',now());"; ;
		}
		try {
			queryresult = stmt.executeUpdate(strSQL);
			queryresult2=stmt.executeUpdate(strSQL2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	} 
		

	public void run(){
		
		for(int i=0;i<ALQueryData.size();i++)
		{
			myExecuteQuery(ALQueryData.get(i));	//executeQuery
		/*
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/

		}
		
	}
	
}
