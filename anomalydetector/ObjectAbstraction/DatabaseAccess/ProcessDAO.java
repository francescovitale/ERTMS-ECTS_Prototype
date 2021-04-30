package ObjectAbstraction.DatabaseAccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import ObjectAbstraction.PMLogic.ProcessModel;

public class ProcessDAO {
	static public ArrayList<ProcessModel> getProcessList(Connection Conn) throws SQLException{
		ArrayList<ProcessModel> PMList = new ArrayList<ProcessModel>();
		Statement stmt = Conn.createStatement();
		String query = "SELECT * FROM eventlog.process";
		ResultSet rs = stmt.executeQuery(query);
		while(rs.next()) {
			ProcessModel PM = new ProcessModel();
			PM.setName(rs.getString("Name"));
			PMList.add(PM);
		}
		return PMList;
	}

	public static void insertProcessModel(String PM, Connection Conn) throws SQLException {
		Statement stmt = Conn.createStatement();
		String query = "INSERT INTO eventlog.process 'Name' VALUES '"+ PM  +"'";
		stmt.executeUpdate(query);
	}

}
