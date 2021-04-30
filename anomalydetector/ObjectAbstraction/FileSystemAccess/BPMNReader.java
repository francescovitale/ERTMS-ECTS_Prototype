package ObjectAbstraction.FileSystemAccess;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import ObjectAbstraction.PMLogic.*;

public class BPMNReader {
private
	String Path;
	String ProcessName;
public
	BPMNReader(String P, String PN) {
		Path = P;
		ProcessName = PN;
	}
	ArrayList<SplitRelation> getSplitRelations(ArrayList<Activity> A) throws FileNotFoundException{
		ArrayList<SplitRelation> SR = new ArrayList<SplitRelation>();
		File SplitRelationFile = new File(Path + "/split_relations_"+ProcessName);
		Scanner Reader = new Scanner(SplitRelationFile);
		while(Reader.hasNextLine()) {
			String SplitRelation = Reader.nextLine();
			String [] SplitRelationParts = SplitRelation.split("\\:");
			String [] SplitRelationSuccActivities = SplitRelationParts[1].split("\\,");
			Activity Prec = null;
			for(int i=0;i<A.size(); i++)
				if(A.get(i).getName().equals(SplitRelationParts[0]))
				{
					Prec = A.get(i);
				}
			ArrayList<Activity> Succ = new ArrayList<Activity>();
			for(int i=0; i<SplitRelationSuccActivities.length; i++) {
				for(int j=0; j<A.size(); j++)
					if(A.get(j).getName().equals(SplitRelationSuccActivities[i]))
						Succ.add(A.get(j));
			}
			SR.add(new SplitRelation(Prec,Succ));
		}
		Reader.close();
		return SR;
	}
	ArrayList<MergeRelation> getMergeRelations(ArrayList<Activity> A) throws FileNotFoundException{
		ArrayList<MergeRelation> MR = new ArrayList<MergeRelation>();
		File MergeRelationFile = new File(Path + "/merge_relations_"+ProcessName);
		Scanner Reader = new Scanner(MergeRelationFile);
		while(Reader.hasNextLine()) {
			String MergeRelation = Reader.nextLine();
			String [] SplitRelationParts = MergeRelation.split("\\:");
			String [] SplitRelationPrecActivities = SplitRelationParts[0].split("\\,");
			Activity Succ = null;
			for(int i=0;i<A.size();i++)
				if(A.get(i).getName().equals(SplitRelationParts[1]))
					Succ=A.get(i);
			ArrayList<Activity> Prec = new ArrayList<Activity>();
			for(int i=0; i<SplitRelationPrecActivities.length; i++)
				for(int j=0; j<A.size(); j++)
					if(A.get(j).getName().equals(SplitRelationPrecActivities[i]))
						Prec.add(A.get(j));
			MR.add(new MergeRelation(Succ,Prec));
		}
		Reader.close();
		return MR;
	}
	

}
