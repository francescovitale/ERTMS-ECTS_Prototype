package ObjectAbstraction.DatabaseAccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import ObjectAbstraction.PMLogic.ActivityInstance;
import ObjectAbstraction.PMLogic.Description;
import ObjectAbstraction.PMLogic.Trace;

public class DescriptionDAO {
	public static ArrayList<Description> getDescriptionList(Connection Conn) throws SQLException{
		
		ArrayList<Description> DList = new ArrayList<Description>();
		Statement stmt = Conn.createStatement();
		String query = "SELECT * FROM eventlog.description";
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()) {
			Description D = new Description();
			D.setFitness(rs.getFloat("Fitness"));
			D.setID(rs.getInt("ID"));
			DList.add(D);
		}
		
		return DList;
	}
	
	public static void insertDescription(Connection Conn, float Fitness) throws SQLException{
		Statement stmt = Conn.createStatement();
		String query = "INSERT INTO eventlog.description (Fitness) VALUES ('"+Fitness+"')";
		stmt.executeUpdate(query);
		
	}
}
