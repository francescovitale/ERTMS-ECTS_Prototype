package ObjectAbstraction.FileSystemAccess;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import ObjectAbstraction.DatabaseAccess.DBFacade;
import ObjectAbstraction.PMLogic.*;

public class FileSystemFacade {
	private PetriNetReader PNR;
	private BPMNReader BPMNR;
	private TRParameterReader TRPR;
	private volatile static FileSystemFacade FSF = null;

	private FileSystemFacade(String Path, String ProcessName) {
		PNR = new PetriNetReader(Path, ProcessName);
		BPMNR = new BPMNReader(Path, ProcessName);
		TRPR = new TRParameterReader(Path, ProcessName);
	}
	public static FileSystemFacade getInstance(String Path, String ProcessName) {
		if(FSF==null) {
			synchronized(FileSystemFacade.class) {
				if(FSF==null) {
					FSF = new FileSystemFacade(Path, ProcessName);
				}
			}
		}
		return FSF;
	}
	
	public PetriNet getPetriNet(ProcessModel PM, ArrayList<Activity> A) throws FileNotFoundException, IOException {
		PetriNet PN = new PetriNet(PM,PNR.getPlaces(),PNR.getTransitions(A), PNR.getPT(), PNR.getTP(),PNR.getMarking());
		return PN;
	}
	
	public BPMN getBPMN(ProcessModel PM, ArrayList<Activity> A) throws FileNotFoundException {
		BPMN BPMN_model = new BPMN();
		BPMN_model.setPM(PM);
		BPMN_model.setMR(BPMNR.getMergeRelations(A));
		BPMN_model.setSR(BPMNR.getSplitRelations(A));
		return BPMN_model;
	}
	
	public ArrayList<TRParameter> getTRParameterList(ProcessModel PMIn) throws FileNotFoundException{
		ArrayList<TRParameter> TRPList;
		
		TRPList = TRPR.getTRParameterList(PMIn);
		
		return TRPList;
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
		ProcessModel PM = new ProcessModel("start_of_mission");
		DBFacade DBF = new DBFacade();
		ArrayList<ProcessModel> PMList = DBF.getProcessList();
		ArrayList<Activity> AList = DBF.getActivityList(PMList);
		
		FileSystemFacade MyFSF = FileSystemFacade.getInstance("C:\\Users\\aceep\\OneDrive\\Desktop\\Files\\StartOfMission", "start_of_mission");
		PetriNet PN = MyFSF.getPetriNet(PM, AList);
		for(int i=0;i<PN.getPlaces().size(); i++)
			System.out.print(PN.getPlaces().get(i)+" ");
		System.out.println();
		for(int i=0;i<PN.getTransitions().size();i++)
			System.out.print(PN.getTransitions().get(i).getName()+" ");
		System.out.println();
		for(int i=0;i<PN.getTransitions().size();i++)
		{
			System.out.println();
			for(int j=0; j<PN.getPlaces().size();j++) {
				//if(PN.getTP().get(PN.getPlaces().get(j), PN.getTransitions().get(i).getName()) == null)
					System.out.print(PN.getTP().get(PN.getPlaces().get(j), PN.getTransitions().get(i).getName()) + " ");
			}
		}
		
		
		BPMN BPMN_model = MyFSF.getBPMN(PM,AList);
		for(int i=0;i<BPMN_model.getSR().size(); i++) {
			System.out.println("Precedent activity: "+BPMN_model.getSR().get(i).getPrecedentActivity().getName());
			System.out.print("Successive activities: ");
			for(int j=0;j<BPMN_model.getSR().get(i).getSuccessiveActivities().size();j++)
				System.out.print(BPMN_model.getSR().get(i).getSuccessiveActivities().get(j).getName()+" ");
			System.out.println();
		}
		System.out.println();
		for(int i=0;i<BPMN_model.getMR().size(); i++) {
			System.out.println("Successive activity: "+BPMN_model.getMR().get(i).getSuccessiveActivity().getName());
			System.out.print("Precedent activities: ");
			for(int j=0;j<BPMN_model.getMR().get(i).getPrecedentActivities().size();j++)
				System.out.print(BPMN_model.getMR().get(i).getPrecedentActivities().get(j).getName()+" ");
			System.out.println();
		}
		
		ArrayList<TRParameter> TRPList = MyFSF.getTRParameterList(PM);
		for(int i=0; i<TRPList.size(); i++) {
			System.out.println("Resource: "+ TRPList.get(i).getResource() + " Parameter value: " + TRPList.get(i).getValue());
		}
	}
	
}
