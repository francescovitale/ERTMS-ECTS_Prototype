package ObjectAbstraction.PMLogic;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import ObjectAbstraction.DatabaseAccess.DBFacade;
import ObjectAbstraction.FileSystemAccess.FileSystemFacade;

public class Checker {
private
	ArrayList<Event> E;
	ArrayList<ActivityInstance> AI;
	ArrayList<Activity> A;
	ArrayList<ProcessInstance> PI;
	ArrayList<ProcessModel> P;
	PetriNet PN;
	BPMN BPMN_model;
	
public
	Checker(){}
	void initializeDBDataStructures(DBFacade DBF) throws SQLException {
		P = DBF.getProcessList();
		A = DBF.getActivityList(P);
		PI = DBF.getProcessInstanceList(P);
		AI = DBF.getActivityInstanceList(A, PI);
		E = DBF.getEventList(PI,AI);
		for(int i=0;i<E.size(); i++)
			System.out.print(E.get(i).getAI().getA().getName() + " ");
		System.out.println();
		
	}
	void initializeFSDataStructures(FileSystemFacade FSF, ProcessModel PM) throws FileNotFoundException, IOException {
		PN = FSF.getPetriNet(PM, A);
		BPMN_model = FSF.getBPMN(PM, A);
	}
	public ArrayList<Trace> onlineConformanceChecking() throws FileNotFoundException, IOException, SQLException{
		DBFacade DBF = new DBFacade();
		
		initializeDBDataStructures(DBF);
		
		
		if(true) {
			return null;
		}
		else {
			ArrayList<Trace> ReturnedTraces = new ArrayList<Trace>();
			ArrayList<Description> CDL = new ArrayList<Description>();
			
			
			
			/*for(int i=0; i<E.size(); i++)
				System.out.println(E.get(i).getAI().getName());*/
			ArrayList<ProcessModel> found_PM = new ArrayList<ProcessModel>();
			for(int i = 0; i<PI.size(); i++) {
				boolean found = false;
				int k = 0;
				do {
					if(found_PM.size() != 0 && found_PM.get(k).getName().equals(PI.get(i).getP().getName()))
						found = true;
					k++;
				}while(found == false && k<found_PM.size());
				if(found == false)
					found_PM.add(PI.get(i).getP());
			}
			
			for(int i = 0; i<found_PM.size(); i++) {
				FileSystemFacade FSF = FileSystemFacade.getInstance("C:\\Users\\aceep\\OneDrive\\Desktop\\Files\\StartOfMission", "start_of_mission");
				initializeFSDataStructures(FSF,found_PM.get(i));
				/*ArrayList<String> P = PN.getPlaces();
				PN.updateMarking(P, null);
				for(int j=0;j<PN.getMarking().size();j++) {
					System.out.println(PN.getMarking().get(P.get(j)));
				}*/
				
				ArrayList<ArrayList<Trace>> ObtainedTraces;
				
				// The last transition must be taken into account.
				ObtainedTraces = buildTraces(); // the 0 position contains the normal traces and the 1 position contains the modified traces
				
				for(int j=0; j<PN.getPlaces().size(); j++)
					System.out.print(PN.getPlaces().get(j)+ " ");
				System.out.println();
				for(int j=0; j<PN.getTransitions().size(); j++)
					System.out.print(PN.getTransitions().get(j).getName()+ " ");
				System.out.println();
				
				for(int j=0; j<ObtainedTraces.get(0).size(); j++) {
					for(int k=0; k<ObtainedTraces.get(0).get(j).getAI().size(); k++) {
						System.out.print(ObtainedTraces.get(0).get(j).getAI().get(k).getA().getName()+" ");
					}
					System.out.println();
				}
				System.out.println();
				for(int j=0; j<ObtainedTraces.get(1).size(); j++) {
					for(int k=0; k<ObtainedTraces.get(1).get(j).getAI().size(); k++) {
						System.out.print(ObtainedTraces.get(1).get(j).getAI().get(k).getA().getName()+" ");
					}
					System.out.println();
				}
				System.out.println();
				
				CDL.add(tokenReplay(ObtainedTraces.get(1),ObtainedTraces.get(0)));
				
				System.out.println("Fitness: " + CDL.get(i).getFitness());
				for(int j=0;j<CDL.get(i).getT().size();j++) {
					System.out.println("Anomalous trace " + j + ": ");
					for(int k=0; k<CDL.get(i).getT().get(j).getAI().size(); k++)
						System.out.print(CDL.get(i).getT().get(j).getAI().get(k).getA().getName()+ " ");
					System.out.println();
				}
			}
			for(int i=0; i<CDL.size(); i++)
				for(int j=0; j<CDL.get(i).getT().size(); j++) {
					ReturnedTraces.add(CDL.get(i).getT().get(j));
					
				}
			/*for(int i=0; i<ReturnedTraces.size(); i++)
				for(int j=0; j<ReturnedTraces.get(i).getAI().size(); j++)
					System.out.println(ReturnedTraces.get(i).getAI().get(j));*/
			return ReturnedTraces;
		}
	}
	Description tokenReplay(ArrayList<Trace> ProcessModifiedTraces, ArrayList<Trace> ProcessTraces) {
		/*for(int i=0; i<ProcessTraces.size(); i++) {
			for(int j=0;j<ProcessTraces.get(i).getAI().size(); j++)
				System.out.print(ProcessTraces.get(i).getAI().get(j).getName() + " ");
		}
		System.out.println();*/
		Description DL = new Description();
		float Fitness = (float) 0.0;
		ReplayParameters RPGlobal = new ReplayParameters();
		for(int i=0; i<ProcessModifiedTraces.size(); i++) {
			ReplayParameters RPLocal = new ReplayParameters(1,0,0,0,false);
			boolean end = false;
			for(int j=0; j<ProcessModifiedTraces.get(i).getAI().size() && !end;j++) {
				
				Activity AToFire = null;
				for(int k=0; k<A.size(); k++)
					if(A.get(k).getName().equals(ProcessModifiedTraces.get(i).getAI().get(j).getA().getName()))
						AToFire = A.get(k);
				RPLocal = PN.fire(AToFire, RPLocal);
				
				System.out.println("Iteration " + j + " for activity " + AToFire.getName() + ": Local p: "+RPLocal.getP() + " Local c: "+RPLocal.getC() + " Local m: "+RPLocal.getM() + "Local r: "+RPLocal.getR());
				
				end = RPLocal.isEnd();
			}
			
			if(RPLocal.getM()!=0 || RPLocal.getR()!= 0) {
				boolean found = false;
				int k = 0;
				do {
					
					if(DL.getT().size() != 0 && DL.getT().get(k).equals(ProcessModifiedTraces.get(i))) {
						found = true;
					}
					k++;
				}while(found == false && k<DL.getT().size());
				if(found == false)
					DL.getT().add(ProcessTraces.get(i));
			}
			RPGlobal.setC(RPGlobal.getC()+RPLocal.getC());
			RPGlobal.setP(RPGlobal.getP()+RPLocal.getP());
			RPGlobal.setM(RPGlobal.getM()+RPLocal.getM());
			RPGlobal.setR(RPGlobal.getR()+RPLocal.getR());
			PN.initializeMarking();
		}
		Fitness = (float)1/2 * (1-((float)RPGlobal.getM()/(float)RPGlobal.getC())) + (float)1/2 * (1-((float)RPGlobal.getR()/(float)RPGlobal.getP()));
		
		DL.setFitness(Fitness);
		
		return DL;
	}
	ArrayList<ArrayList<Trace>> buildTraces() {
		ArrayList<Trace> BuiltTraces = new ArrayList<Trace>();
		ArrayList<Trace> ModifiedBuiltTraces = new ArrayList<Trace>();
		ArrayList<ArrayList<Trace>> ObtainedTraces = new ArrayList<ArrayList<Trace>>();

		
		for(int i=0; i<PI.size(); i++)
		{
			Trace ExtractedTrace = orderEvents(PI.get(i).getCaseID());
			
			if(isComplete(ExtractedTrace)) {
				BuiltTraces.add(ExtractedTrace);
				Trace ExtractedModifiedTrace = modifyTrace(ExtractedTrace, PI.get(i));
				
				ModifiedBuiltTraces.add(ExtractedModifiedTrace);
				}
		}
		
		
		
		ObtainedTraces.add(BuiltTraces);
		ObtainedTraces.add(ModifiedBuiltTraces);
		return ObtainedTraces;
		
	}

	boolean isComplete(Trace T) {
		boolean complete = false;
		ActivityInstance LastActivity = T.getAI().get(T.getAI().size()-1);
		if(LastActivity.getA().getName().equals(PN.getTransitions().get(PN.getTransitions().size()-1).getName())) {
			complete = true;
		}
		return complete;
		
	}
	
	Trace orderEvents(int CaseID) {
		Trace BuiltTrace = new Trace();
		ArrayList<Event> EventListToOrder = new ArrayList<Event>();
		ArrayList<Event> TempEvent = new ArrayList<Event>(E);
		boolean found = false;
		
		for(int i=0;i<E.size(); i++)
			System.out.print(E.get(i).getAI().getA().getName() + " ");
		System.out.println();
		
		do {
			
			found = false;
			
			for(int i = 0; i<TempEvent.size(); i++)
			{
				if(TempEvent.get(i).getPI().getCaseID() == CaseID) {
					found = true;
					EventListToOrder.add(TempEvent.get(i));
					TempEvent.remove(i);
				}
				
			}
		} while(found == true);
		
		for(int i=0;i<EventListToOrder.size(); i++)
			System.out.print(EventListToOrder.get(i).getAI().getA().getName() + " ");
		System.out.println();
		EventListToOrder = sort(EventListToOrder);
		
		ArrayList<ActivityInstance> OrderedAIArray = new ArrayList<ActivityInstance>();
		for(int i=0; i<EventListToOrder.size(); i++) {
			OrderedAIArray.add(EventListToOrder.get(i).getAI());
		}
		BuiltTrace.setAI(OrderedAIArray);
		for(int i = 0; i<BuiltTrace.getAI().size(); i++)
			System.out.print(BuiltTrace.getAI().get(i).getA().getName()+ " ");
		System.out.println();
		
		return BuiltTrace;
	}
	Trace modifyTrace(Trace BaseTrace, ProcessInstance PIToAssign) {
		Trace ModifiedTrace = new Trace(BaseTrace);
		int count;
		
		ArrayList<SplitRelation> RetrievedSR = BPMN_model.getSR();
		ArrayList<MergeRelation> RetrievedMR = BPMN_model.getMR();
		for(int i=0; i<RetrievedSR.size(); i++)
		{
			System.out.println("Split relation no."+i+":");
			System.out.println("Precedent Activity: "+ RetrievedSR.get(i).getPrecedentActivity().getName());
			System.out.print("Successive Activities: ");
			for(int j=0;j<RetrievedSR.get(i).getSuccessiveActivities().size(); j++)
				System.out.print(RetrievedSR.get(i).getSuccessiveActivities().get(j).getName()+" ");
			System.out.println();
		}
		boolean skip = false;
		for(int i=0; i<RetrievedSR.size(); i++) {
			// every iteration considers a single split relation, which has a single precedent activity and multiple possible successive activities
			Activity PrecedentActivity = RetrievedSR.get(i).getPrecedentActivity();
			ArrayList<Activity> SuccessiveActivities = RetrievedSR.get(i).getSuccessiveActivities();
			boolean found = false;
			int previousindex = -1;
			int successiveindex = -1;
			
			count = 0;
			for(int j=0; j<ModifiedTrace.getAI().size(); j++) {
				if(ModifiedTrace.getAI().get(j).getA().getName().equals(PrecedentActivity.getName())) {
					
					boolean found_internal = false;
					int k = 0;
					do {
						if(ModifiedTrace.getAI().get(j+1).getA().getName().equals(SuccessiveActivities.get(k).getName())) {
							found_internal = true;
							count++;
						}
						k++;
					}while(found_internal == false && k<SuccessiveActivities.size());
					if(found_internal == false)
						found = false;
					else {
						previousindex = j;
						successiveindex = j+1;
						System.out.println("Split relation no. " + i + " found. previous index = " + previousindex + ", successive index = "+ successiveindex);
						found = true;
						if(skip==true)
							j = ModifiedTrace.getAI().size();
						
					}
					
				}
			}
			System.out.println("Found: " + found);
			if(found == true) {
				String SuccessiveActivityName;
				found = false;
				int k = 0;
				do {
					found = false;
					
					SuccessiveActivityName = SuccessiveActivities.get(k).getName();
					if(ModifiedTrace.getAI().get(successiveindex).getA().getName().equals(SuccessiveActivityName)) {
						found = true;
						SuccessiveActivityName = ModifiedTrace.getAI().get(successiveindex).getA().getName();
					}
					
					k++;
				}while(found == false && k<SuccessiveActivities.size());
				if(found == true) {
					ModifiedTrace = insertSplit(ModifiedTrace,previousindex, successiveindex, PrecedentActivity.getName(), SuccessiveActivityName, RetrievedSR.get(i).getSuccessiveActivities(), PIToAssign);
					
				}
			}
			if(count>1) {
				skip=true;
				i--;
			}
			else
				skip=false;
			
		}
		for(int i=0; i<RetrievedMR.size(); i++)
		{
			System.out.println("Merge relation no."+i+":");
			System.out.println("Successive Activity: "+ RetrievedMR.get(i).getSuccessiveActivity().getName());
			System.out.print("Precedent Activities: ");
			for(int j=0;j<RetrievedMR.get(i).getPrecedentActivities().size(); j++)
				System.out.print(RetrievedMR.get(i).getPrecedentActivities().get(j).getName()+" ");
			System.out.println();
		}
		skip=false;
		for(int i=0; i<RetrievedMR.size(); i++) {
			// every iteration considers a single split relation, which has a single precedent activity and multiple possible successive activities
			Activity SuccessiveActivity = RetrievedMR.get(i).getSuccessiveActivity();
			ArrayList<Activity> PrecedentActivities = RetrievedMR.get(i).getPrecedentActivities();
			boolean found = false;
			count = 0;
			int previousindex = -1;
			int successiveindex = -1;
			for(int j=0; j<ModifiedTrace.getAI().size(); j++) {
				if(ModifiedTrace.getAI().get(j).getA().getName().equals(SuccessiveActivity.getName())) {
					boolean found_internal = false;
					int k = 0;
					do {
						if(ModifiedTrace.getAI().get(j-1).getA().getName().equals(PrecedentActivities.get(k).getName())) {
							found_internal = true;
							count++;
						}
						k++;
					}while(found_internal == false && k<PrecedentActivities.size());
					if(found_internal == false)
						found = false;
					else {
						previousindex = j-1;
						successiveindex = j;
						System.out.println("Merge relation no. " + i + " found. previous index = " + previousindex + ", successive index = "+ successiveindex);
						found = true;
						if(skip==true)
							j = ModifiedTrace.getAI().size();
					}
					
				}
			}
			System.out.println("Found: " + found);
			if(found == true) {
				String PrecedentActivityName;
				found = false;
				int k = 0;
				do {
					found = false;
					
					PrecedentActivityName = PrecedentActivities.get(k).getName();
					if(ModifiedTrace.getAI().get(previousindex).getA().getName().equals(PrecedentActivityName))
					{
						PrecedentActivityName = ModifiedTrace.getAI().get(previousindex).getA().getName();
						found = true;
					}
					/*for(int j=0; j<BaseTrace.getAI().size(); j++) {
						if(BaseTrace.getAI().get(j).getA().getName().equals(PrecedentActivityName)) {
							System.out.println(BaseTrace.getAI().get(j).getA().getName());
							found = true;
						}
					}*/
					k++;
				}while(found == false && k<PrecedentActivities.size());
				if(found == true) {
					ModifiedTrace = insertJoin(ModifiedTrace,previousindex, successiveindex,PrecedentActivityName, SuccessiveActivity.getName(), RetrievedMR.get(i).getPrecedentActivities(), PIToAssign);
					
				}
			}
			if(count>1) {
				skip=true;
				i--;
			}
			else
				skip=false;
			
		}
		
		
		return ModifiedTrace;
	}

	Trace insertSplit(Trace BaseTrace, int previousindex, int successiveindex, String PrecActivity, String SuccActivity,ArrayList<Activity> SuccessiveActivities, ProcessInstance PIToAssign) {
		System.out.println("Previous index: " + previousindex + ", Successive index:" + successiveindex + " Precedent activity: "+ PrecActivity + ", Successive activity: "+ SuccActivity);
		
		String MergeActivityName;
		MergeActivityName = PrecActivity+"_merge_";
		for(int i = 0; i<SuccessiveActivities.size(); i++) {
			MergeActivityName = MergeActivityName + SuccessiveActivities.get(i).getName();
			if(i != SuccessiveActivities.size()-1)
				MergeActivityName = MergeActivityName+"_";
		}
		
		System.out.println(MergeActivityName);
		ActivityInstance MergeActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(MergeActivityName))
				MergeActivity = new ActivityInstance(-1,PIToAssign,A.get(i));
		
		System.out.println(MergeActivity.getA().getName());
		
		ActivityInstance SplitActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(SuccActivity+"_split_"+PrecActivity))
				SplitActivity = new ActivityInstance(-1,PIToAssign,A.get(i));
		
		
		BaseTrace.getAI().add(previousindex+1, MergeActivity);
		BaseTrace.getAI().add(successiveindex+1, SplitActivity);
		/*for(int j=0; j<BaseTrace.getAI().size(); j++)
			System.out.print(BaseTrace.getAI().get(j).getName()+" ");*/
		
		return BaseTrace;
	}
	
	Trace insertJoin(Trace BaseTrace, int previousindex, int successiveindex, String PrecActivity, String SuccActivity,ArrayList<Activity> PrecedentActivities, ProcessInstance PIToAssign) {
		System.out.println("Previous index: " + previousindex + ", Successive index:" + successiveindex + " Precedent activity: "+ PrecActivity + ", Successive activity: "+ SuccActivity);
		
		String SplitActivityName = "";
		for(int i = 0; i<PrecedentActivities.size(); i++) {
			SplitActivityName = SplitActivityName +PrecedentActivities.get(i).getName();
			if(i != PrecedentActivities.size()-1)
				SplitActivityName = SplitActivityName + "_";
		}
		
		
		
		ActivityInstance SplitActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(SplitActivityName+"_split_"+SuccActivity))
				SplitActivity = new ActivityInstance(-1,PIToAssign,A.get(i));
		System.out.println(SplitActivityName+"_split_"+SuccActivity);
		System.out.println(SplitActivity.getA().getName());
		
		ActivityInstance MergeActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(PrecActivity+"_merge_"+SuccActivity))
				MergeActivity = new ActivityInstance(-1, PIToAssign, A.get(i));
		
		
		
		BaseTrace.getAI().add(previousindex+1, MergeActivity);
		BaseTrace.getAI().add(successiveindex+1, SplitActivity);
		
		return BaseTrace;
	}
	
	ArrayList<Event> sort(ArrayList<Event> EventListToOrder){
		//for(int i=0;i<EventListToOrder.size(); i++)
		//	System.out.print(EventListToOrder.get(i).getAI().getA().getName() + " ");
		//System.out.println();
		int n = EventListToOrder.size();
	    for (int i = 1; i < n; ++i) {
	        Event key = EventListToOrder.get(i);
	        int j = i - 1;
	        while (j >= 0 && EventListToOrder.get(j).getT() > key.getT()) {
	        	//System.out.println("key: " + key.getAI().getA().getName() + "T:" + key.getT() +", EventListToOrder.get(j):" + EventListToOrder.get(j).getAI().getA().getName() + "T:" + EventListToOrder.get(j).getT());
	        	EventListToOrder.set(j+1, EventListToOrder.get(j));
	            j = j - 1;
	        }
	        EventListToOrder.set(j + 1,key);
	    }
	    
	    return EventListToOrder;
	}
	
	/*public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
		Checker C = new Checker();
		ArrayList<Trace> NonConformantTraces = C.onlineConformanceChecking(1);
		for(int i=0; i<NonConformantTraces.size(); i++) {
			System.out.print("Non conformant trace " + i + ": ");
			for(int j=0; j<NonConformantTraces.get(i).getAI().size(); j++)
				System.out.print(NonConformantTraces.get(i).getAI().get(j).getA().getName() + " ");
			System.out.println();
		}
		
	}*/
	
}
