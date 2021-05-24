package ObjectAbstraction.DatabaseAccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import ObjectAbstraction.PMLogic.Activity;
import ObjectAbstraction.PMLogic.ProcessModel;

public class ActivityDAO {
	public static ArrayList<Activity> getActivityList(ArrayList<ProcessModel> PMList, Connection Conn) throws SQLException {
		
		ArrayList<Activity> AList = new ArrayList<Activity>();
		Statement stmt = Conn.createStatement();
		String query = "SELECT * FROM eventlog.activity";
		ResultSet rs = stmt.executeQuery(query);
		while(rs.next()) {
			Activity A;
			String ActivityPMName = rs.getString("ProcessModel");
			for(int i=0; i<PMList.size(); i++) {
				if(PMList.get(i).getName().equals(ActivityPMName)) {
					A = new Activity(rs.getString("Name"),PMList.get(i),rs.getString("Resource"));
					AList.add(A);
				}
				
			}
		}
		
		
		
		return AList;
	}

	public static void insertActivity(Activity A, Connection Conn) throws SQLException {
		Statement stmt = Conn.createStatement();
		String query = "INSERT INTO eventlog.activity 'Name', 'ProcessModel','Resource' VALUES '"+A.getName()+"','"+ A.getPM().getName() +"','"+A.getResource()+"'";      
		stmt.executeUpdate(query);
	}
}
