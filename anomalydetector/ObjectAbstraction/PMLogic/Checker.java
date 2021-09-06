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
	ArrayList<TRParameter> TRP;
	
public
	Checker(){}
	// This method fetches the contents of the database and place them in the private data structures
	void initializeDBDataStructures(DBFacade DBF) throws SQLException {
		P = DBF.getProcessList();
		A = DBF.getActivityList(P);
		PI = DBF.getProcessInstanceList(P);
		AI = DBF.getActivityInstanceList(A, PI);
		E = DBF.getEventList(PI,AI);
		
	}
	
	// This method fetches the contents in the files that contain what's necessary for applying the Conformance Checking algorithm
	void initializeFSDataStructures(FileSystemFacade FSF, ProcessModel PM) throws FileNotFoundException, IOException {
		PN = FSF.getPetriNet(PM, A);
		BPMN_model = FSF.getBPMN(PM, A);
		TRP = FSF.getTRParameterList(PM);
	}
	
	// This method performs the Conformance Checking through use of the Token Replay technique.
	public ArrayList<Trace> onlineConformanceChecking(boolean aware, String Path) throws FileNotFoundException, IOException, SQLException{
		DBFacade DBF = new DBFacade();
		
		// The private data structures stored in the Database are fetched
		initializeDBDataStructures(DBF);
		
		// What's needed throughout the algorithm is here instantiated
		ArrayList<Trace> ReturnedTraces = new ArrayList<Trace>();
		ArrayList<Description> CDL = new ArrayList<Description>();
		ArrayList<ProcessModel> found_PM = new ArrayList<ProcessModel>();
		
		// In this cycle only one Process Model is considered per group of Process Instances
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
		
		// This is the main cycle. It cycles over all the Process Models found in the Database.
		for(int i = 0; i<found_PM.size(); i++) {
			FileSystemFacade FSF = FileSystemFacade.getInstance(Path+"/process_files", "start_of_mission");
			// The private data structures stored in the files are fetched
			initializeFSDataStructures(FSF,found_PM.get(i));
			
			// This ArrayList of ArrayLists will contain the base traces and the extended traces
			ArrayList<ArrayList<Trace>> ObtainedTraces;
				
			// The last transition must be taken into account.
			ObtainedTraces = buildTraces(); // the 0 position contains the normal traces and the 1 position contains the modified traces
			
			System.out.println("Base trace n." + i);
			for(int j=0; j<ObtainedTraces.get(0).size(); j++) {
				for(int k=0; k<ObtainedTraces.get(0).get(j).getAI().size(); k++) {
					System.out.print(ObtainedTraces.get(0).get(j).getAI().get(k).getA().getName()+" ");
				}
				System.out.println();
			}
			System.out.println();
			System.out.println("Extended trace n." + i);
			for(int j=0; j<ObtainedTraces.get(1).size(); j++) {
				for(int k=0; k<ObtainedTraces.get(1).get(j).getAI().size(); k++) {
					System.out.print(ObtainedTraces.get(1).get(j).getAI().get(k).getA().getName()+" ");
				}
				System.out.println();
			}
			System.out.println();
			
			// To the ArrayList of Descriptions the results of the tokenReplay() method are considered.
			// The tokenReplay method applies the Token Replay technique and infers a fitness parameter.
			CDL.add(tokenReplay(ObtainedTraces.get(1),ObtainedTraces.get(0),aware));
			
			System.out.println("Fitness: " + CDL.get(i).getFitness());
			for(int j=0;j<CDL.get(i).getT().size();j++) {
				System.out.println("Anomalous trace " + j + ": ");
				for(int k=0; k<CDL.get(i).getT().get(j).getAI().size(); k++)
					System.out.print(CDL.get(i).getT().get(j).getAI().get(k).getA().getName()+ " ");
				System.out.println();
				int[] ActID = new int[CDL.get(i).getT().get(j).getAI().size()];
				for(int k=0; k<ActID.length; k++)
					ActID[k] = CDL.get(i).getT().get(j).getAI().get(k).getID();
				DBF.insertAnomalousTrace(ActID, CDL.get(i).getFitness());
				
			}
			int[] ActID;
			int CaseID;
			for(int j=0; j<ObtainedTraces.get(0).size(); j++) {
				ActID = new int[ObtainedTraces.get(0).get(j).getAI().size()];
				CaseID = ObtainedTraces.get(0).get(j).getAI().get(j).getPI().getCaseID();
				for(int k=0; k<ObtainedTraces.get(0).get(j).getAI().size(); k++)
					ActID[k] = ObtainedTraces.get(0).get(j).getAI().get(k).getID();
				//DBF.deleteEvent(ActID, CaseID);
			}
		}
		for(int i=0; i<CDL.size(); i++)
			for(int j=0; j<CDL.get(i).getT().size(); j++) {
				ReturnedTraces.add(CDL.get(i).getT().get(j));
				
			}
		
		for(int i=0; i<TRP.size(); i++) {
			System.out.println("Resource: " + TRP.get(i).getResource() + " Counter: " + TRP.get(i).getCounter());
		}
		/*for(int i=0; i<ReturnedTraces.size(); i++)
			for(int j=0; j<ReturnedTraces.get(i).getAI().size(); j++)
				System.out.println(ReturnedTraces.get(i).getAI().get(j));*/
		DBF.closeConnection();
		return ReturnedTraces;
	}
	
	// This method performs the so-called Token Replay technique.
	Description tokenReplay(ArrayList<Trace> ProcessModifiedTraces, ArrayList<Trace> ProcessTraces, boolean Aware) {
		/*for(int i=0; i<ProcessTraces.size(); i++) {
			for(int j=0;j<ProcessTraces.get(i).getAI().size(); j++)
				System.out.print(ProcessTraces.get(i).getAI().get(j).getName() + " ");
		}
		System.out.println();*/
		
		// The data structures used are here defined. The ReplayParameters variable is initialized to
		// hold the p, c, m and r variables. However, one should note that there are two of these variables:
		// 		1) RPGlobal considers *all* the p,c,m and r calculated for each trace
		// 		2) RPLocal considers the p,c,m and r calculated for the single trace.
		Description DL = new Description();
		float Fitness = (float) 0.0;
		ReplayParameters RPGlobal = new ReplayParameters();
		ReplayParameters RPLocalTemp;
		String ActResource;
		// The Token Replay techniques is applied for each trace.
		for(int i=0; i<ProcessModifiedTraces.size(); i++) {
			ReplayParameters RPLocal = new ReplayParameters(1,0,0,0,false);
			boolean end = false;
			// The Token Replay technique fires each activity found in the trace.
			for(int j=0; j<ProcessModifiedTraces.get(i).getAI().size() && !end;j++) {
				
				Activity AToFire = null;
				// The activity to fire in the Petri Net is recovered from the trace.
				for(int k=0; k<A.size(); k++)
					if(A.get(k).getName().equals(ProcessModifiedTraces.get(i).getAI().get(j).getA().getName()))
						AToFire = A.get(k);
				// The activity is fired on the Petri Net. The Petri Net itself updates the parameters
				
				RPLocalTemp = new ReplayParameters(RPLocal);
				
				RPLocal = PN.fire(AToFire, RPLocal);
				
				if(RPLocal.getM()-RPLocalTemp.getM()>0) {
					
					ArrayList<Activity> UnfiredActivities;
					UnfiredActivities = PN.getUnfiredActivities();
					for(int k = 0; k<UnfiredActivities.size(); k++) {
						System.out.println("Unfired activities: " + UnfiredActivities.get(k).getName());
					}
					System.out.println();
					/*ArrayList<Activity> RecoveredProcessActivities = new ArrayList<Activity>();
					ArrayList<String> RecoveredResources = new ArrayList<String>();
					for(int k=0; k<E.size(); k++) {
						
						for(int l=0; l<ProcessModifiedTraces.get(i).getAI().size(); l++) {
							if(E.get(k).getAI().getA().getName().equals(ProcessModifiedTraces.get(i).getAI().get(l).getA().getName())) {
								if(E.get(k).getAI().getID() == ProcessModifiedTraces.get(i).getAI().get(l).getID()) {
									RecoveredProcessActivities.add(E.get(k).getAI().getA());
									RecoveredResources.add(E.get(k).getResource());
								}
							}
						}
					}*/
					//System.out.println(RecoveredProcessActivities.size());
					for(int k=0; k<UnfiredActivities.size(); k++) {
						for(int l=0; l<A.size(); l++) {
							if(UnfiredActivities.get(k).getName().equals(A.get(l).getName())) {
								for(int s=0; s<TRP.size(); s++) {
									if(TRP.get(s).getResource().equals(A.get(l).getResource())) {
										TRP.get(s).setCounter(TRP.get(s).getCounter()+1);
									}
								}
							}
						}
					}
				}
				
				
				System.out.println("Iteration " + j + " for activity " + AToFire.getName() + ": Local p: "+RPLocal.getP() + " Local c: "+RPLocal.getC() + " Local m: "+RPLocal.getM() + "Local r: "+RPLocal.getR());
				
				end = RPLocal.isEnd(); // Check if the Token Replay has ended for the current trace
			}
			// The anomalous traces are here considered and added to the Description variable that was defined before.
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
			// RPGlobal is updated.
			RPGlobal.setC(RPGlobal.getC()+RPLocal.getC());
			RPGlobal.setP(RPGlobal.getP()+RPLocal.getP());
			RPGlobal.setM(RPGlobal.getM()+RPLocal.getM());
			RPGlobal.setR(RPGlobal.getR()+RPLocal.getR());
			// The Petri Net is re-initialized.
			PN.initializeMarking();
			PN.resetUnfiredActivities();
		}
		// The Fitness parameter is updated.
		/*
		 * Fitness(sigma, N, lambda_i) = (1/2(1-m/c)+1/2(1-r/p))(a/(1+sum_i lambda_i*m_i)+(1-a))
		 */
		float Second_factor_den_sum = (float) 1.0;
		for(int i=0; i<TRP.size(); i++) {
			Second_factor_den_sum += (float)((float)TRP.get(i).getCounter()*(float)TRP.get(i).getValue());
		}
		float Second_factor = (float)(Aware ? 0 : 1)/((float)Second_factor_den_sum) + 1-(float)(Aware ? 0 : 1);
		System.out.println("Second factor:" + Second_factor);
		
		float First_factor = ((float)1/2 * (1-((float)RPGlobal.getM()/(float)RPGlobal.getC())) + (float)1/2 * (1-((float)RPGlobal.getR()/(float)RPGlobal.getP())));
		System.out.println("First factor:" + First_factor);
		Fitness = (float)(First_factor * Second_factor);
		
		DL.setFitness(Fitness);
		
		return DL;
	}
	
	// This method build the Base Traces and the Extended Traces.
	ArrayList<ArrayList<Trace>> buildTraces() {
		ArrayList<Trace> BuiltTraces = new ArrayList<Trace>();
		ArrayList<Trace> ModifiedBuiltTraces = new ArrayList<Trace>();
		ArrayList<ArrayList<Trace>> ObtainedTraces = new ArrayList<ArrayList<Trace>>();

		
		for(int i=0; i<PI.size(); i++)
		{
			// The trace, for each Process Instance, is obtained through ordering the events using the 
			// orderEvents() method
			Trace ExtractedTrace = orderEvents(PI.get(i).getCaseID());
			
			// The trace is checked for completeness through the isComplete() method.
			if(isComplete(ExtractedTrace)) {
				BuiltTraces.add(ExtractedTrace);
				// The Modified Trace is here built
				Trace ExtractedModifiedTrace = modifyTrace(ExtractedTrace, PI.get(i));
				// The Modified Trace is added to the list of traces.
				ModifiedBuiltTraces.add(ExtractedModifiedTrace);
				}
		}
		// In the first position of this ArrayList is the ArrayList of Base Traces. In the second position
		// of this ArrayList is the ArrayList of the Modified Base Traces.
		ObtainedTraces.add(BuiltTraces);
		ObtainedTraces.add(ModifiedBuiltTraces);
		return ObtainedTraces;
		
	}

	// Checks for the trace completeness.
	boolean isComplete(Trace T) {
		boolean complete = false;
		if(T.getAI().size() != 0) {
			ActivityInstance LastActivity = T.getAI().get(T.getAI().size()-1);
			if(LastActivity.getA().getName().equals(PN.getTransitions().get(PN.getTransitions().size()-1).getName())) {
				complete = true;
			}
		}
		return complete;
		
	}
	
	// Orders the events for a particular Process Instance
	Trace orderEvents(int CaseID) {
		Trace BuiltTrace = new Trace();
		// This will be the Event List to order.
		ArrayList<Event> EventListToOrder = new ArrayList<Event>();
		// A copy of the Event List is made. This is to not lose the references for the original list.
		ArrayList<Event> TempEvent = new ArrayList<Event>(E);
		boolean found = false;
		
		do {
			
			found = false;
			
			// The copy of the Event List is cycled through: if the CaseID matches with what's expected
			// then the event is added to the Event List to order and is removed from the copy (this is why the copy was made)
			for(int i = 0; i<TempEvent.size(); i++)
			{
				if(TempEvent.get(i).getPI().getCaseID() == CaseID) {
					found = true;
					EventListToOrder.add(TempEvent.get(i));
					TempEvent.remove(i);
				}
				
			}
		} while(found == true);
		
		// Through the sort method, which simply implements an Insertion Sort, the events are sorted by their timestamp
		EventListToOrder = sort(EventListToOrder);
		
		// An ArrayList of ActivityInstance is made and built with the activity instances recovered from
		// the Event List to order.
		ArrayList<ActivityInstance> OrderedAIArray = new ArrayList<ActivityInstance>();
		for(int i=0; i<EventListToOrder.size(); i++) {
			OrderedAIArray.add(EventListToOrder.get(i).getAI());
		}
		// The Built Trace is populated with the recovered Activity Instance ArrayList.
		BuiltTrace.setAI(OrderedAIArray);
		
		return BuiltTrace;
	}
	
	// This is the core method that modifies the trace by adding the artificial events.
	Trace modifyTrace(Trace BaseTrace, ProcessInstance PIToAssign) {
		Trace ModifiedTrace = new Trace(BaseTrace);
		// The count variable is used to manage multiple instances of the same activity (this happens for loops)
		int count;
		
		// The Split Relations and Merge Relations (Join Relations) are retrieved from the BPMN model previously fetched
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
		// The skip variable is used to manage multiple instances of the same activity (this happens for loops)
		int skip = 0;
		for(int i=0; i<RetrievedSR.size(); i++) {
			// Every iteration considers a single split relation, which has a single precedent activity and multiple possible successive activities
			Activity PrecedentActivity = RetrievedSR.get(i).getPrecedentActivity();
			ArrayList<Activity> SuccessiveActivities = RetrievedSR.get(i).getSuccessiveActivities();
			boolean found = false;
			// These two indices will be necessary when inserting the artificial activity in the trace
			int previousindex = -1;
			int successiveindex = -1;
			
			count = 0;
			// The loop iterates over the Modified Trace (it gets extended each time when a Split Relation or a Merge (join) Relation is found.
			for(int j=0; j<ModifiedTrace.getAI().size(); j++) {
				if(ModifiedTrace.getAI().get(j).getA().getName().equals(PrecedentActivity.getName())) { // Here the algorithm is checking if the activity equals the name of the Precedent Activity of the Split Relation in exam
					// found_internal is necessary for determining if there are any successive activities that follow the precedent activity. If that's not the case, then the relation is corrupted and there will be an anomaly detected by the algorithm
					boolean found_internal = false;
					int k = 0;
					do {
						if(ModifiedTrace.getAI().get(j+1).getA().getName().equals(SuccessiveActivities.get(k).getName())) {
							found_internal = true;
							count++; // Count is incremented to take into account how many Split Relations have been found
						}
						k++;
					}while(found_internal == false && k<SuccessiveActivities.size());
					if(found_internal == false) // If there's no successive activity found, then the whole found variable is put to false
						found = false;
					else {
						// The two indices are modified.
						previousindex = j;
						successiveindex = j+1;
						System.out.println("Split relation no. " + i + " found. previous index = " + previousindex + ", successive index = "+ successiveindex);
						found = true;
						if(skip==1)
							j = ModifiedTrace.getAI().size(); // This is to exit the cycle, when 'skip' is set to true
						else
							skip--;
					}
					
				}
			}
			
			// This branch is executed if a Split Relation has been found
			if(found == true) {
				// This variable contains the successive activity name
				String SuccessiveActivityName;
				found = false;
				int k = 0;
				do {
					found = false;
					// The algorithm tries to find, among the successive activities of the split relation, which is the one that matches the one found in the trace
					SuccessiveActivityName = SuccessiveActivities.get(k).getName();
					if(ModifiedTrace.getAI().get(successiveindex).getA().getName().equals(SuccessiveActivityName)) {
						found = true;
						SuccessiveActivityName = ModifiedTrace.getAI().get(successiveindex).getA().getName();
					}
					
					k++;
				}while(found == false && k<SuccessiveActivities.size());
				if(found == true) {
					// This is where the artificial activity is inserted. 
					ModifiedTrace = insertSplit(ModifiedTrace,previousindex, successiveindex, PrecedentActivity.getName(), SuccessiveActivityName, RetrievedSR.get(i).getSuccessiveActivities(), PIToAssign);
					
				}
			}
			//System.out.println(count);
			skip = count-1;
			
			if(skip >0) {
				i--;
			}
			
		}
		// Every iteration considers a single merge relation, which has a single successive activity and multiple possible precedent activities
		for(int i=0; i<RetrievedMR.size(); i++)
		{
			System.out.println("Merge relation no."+i+":");
			System.out.println("Successive Activity: "+ RetrievedMR.get(i).getSuccessiveActivity().getName());
			System.out.print("Precedent Activities: ");
			for(int j=0;j<RetrievedMR.get(i).getPrecedentActivities().size(); j++)
				System.out.print(RetrievedMR.get(i).getPrecedentActivities().get(j).getName()+" ");
			System.out.println();
		}
		skip=0;
		for(int i=0; i<RetrievedMR.size(); i++) {
			System.out.println("Modified trace:");
			for(int j = 0; j<ModifiedTrace.getAI().size(); j++) {
				System.out.print(ModifiedTrace.getAI().get(j).getA().getName()+ " ");
				
			}
			System.out.println();
			// every iteration considers a single merge relation, which has a single successive activity and multiple possible precedent activities
			Activity SuccessiveActivity = RetrievedMR.get(i).getSuccessiveActivity();
			ArrayList<Activity> PrecedentActivities = RetrievedMR.get(i).getPrecedentActivities();
			boolean found = false;
			count = 0;
			int previousindex = -1;
			int successiveindex = -1;
			for(int j=0; j<ModifiedTrace.getAI().size(); j++) {
				if(ModifiedTrace.getAI().get(j).getA().getName().equals(SuccessiveActivity.getName())) { // Here the algorithm is checking if the activity equals the name of the Successive Activity of the Merge Relation in exam
					// found_internal is necessary for determining if there are any precedent activities that precede the successive activity. If that's not the case, then the relation is corrupted and there will be an anomaly detected by the algorithm
					boolean found_internal = false;
					int k = 0;
					do {
						if(j!= 0 && ModifiedTrace.getAI().get(j-1).getA().getName().equals(PrecedentActivities.get(k).getName())) {
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
						if(skip==1)
							j = ModifiedTrace.getAI().size();
						else
							skip--;
					}
					
					
				}
			}
			// This branch is executed if a Merge Relation has been found
			if(found == true) {
				// This variable contains the precedent activity name
				String PrecedentActivityName;
				found = false;
				int k = 0;
				do {
					found = false;
					// The algorithm tries to find, among the precedent activities of the merge relation, which is the one that matches the one found in the trace
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
					// This is where the artificial activity is inserted. 
					ModifiedTrace = insertJoin(ModifiedTrace,previousindex, successiveindex,PrecedentActivityName, SuccessiveActivity.getName(), RetrievedMR.get(i).getPrecedentActivities(), PIToAssign);
					
				}
			}
			System.out.println(count);
			skip = count-1;
			
			if(skip >0) {
				i--;
			}
			
			
			
		}
		
		
		return ModifiedTrace;
	}
	
	// This method inserts the artificial activities needed when a split relation is found in the original trace.
	// Recall that the artificial activities to insert are two:
	// 		1) <prec_act>_merge_<first_act>_..._<last_act>
	// 		2) <first_act>_split_<prec_act>
	// The method has therefore two parts: the first adds the activity 1) and the second adds the activity 2)
	Trace insertSplit(Trace BaseTrace, int previousindex, int successiveindex, String PrecActivity, String SuccActivity,ArrayList<Activity> SuccessiveActivities, ProcessInstance PIToAssign) {
		System.out.println("Previous index: " + previousindex + ", Successive index:" + successiveindex + " Precedent activity: "+ PrecActivity + ", Successive activity: "+ SuccActivity);
		
		// This is the first part of the method that adds the activity 1). The MergeActivityName is built
		// considering first the PrecActivity concatenated with the _merge_ string, and afterwards all the
		// successive activities of the SplitRelation are inserted.
		String MergeActivityName;
		MergeActivityName = PrecActivity+"_merge_";
		for(int i = 0; i<SuccessiveActivities.size(); i++) {
			MergeActivityName = MergeActivityName + SuccessiveActivities.get(i).getName();
			if(i != SuccessiveActivities.size()-1)
				MergeActivityName = MergeActivityName+"_";
		}
		
		System.out.println("MergeActivityName: " + MergeActivityName);
		// The artificial activity is here built.
		// Beware a REALLY important thing: the artificial name of the activity, namely the MergeActivityName MUST be found in
		// the activity list that is fetched from the Database. Misalignments will result in crashes (they're not handled.)
		ActivityInstance MergeActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(MergeActivityName)) // This must be true eventually.
				MergeActivity = new ActivityInstance(-1,PIToAssign,A.get(i));
		
		System.out.println("MergeActivityName of the actual Activity: " + MergeActivity.getA().getName()); // This print will help determining if the activity has been found.
		
		// This is the second part of the method that adds the activity 2). This name is easier to build. Beware of misalignments.
		ActivityInstance SplitActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(SuccActivity+"_split_"+PrecActivity))
				SplitActivity = new ActivityInstance(-1,PIToAssign,A.get(i));
		
		System.out.println("SplitActivityName of the actual activity"+ SplitActivity.getA().getName());
		System.out.println();
		// The activity instance is added to the BaseTrace,
		BaseTrace.getAI().add(previousindex+1, MergeActivity);
		BaseTrace.getAI().add(successiveindex+1, SplitActivity);
		/*for(int j=0; j<BaseTrace.getAI().size(); j++)
			System.out.print(BaseTrace.getAI().get(j).getName()+" ");*/
		
		return BaseTrace;
	}
	
	// This method inserts the artificial activities needed when a merge relation is found in the original trace.
		// Recall that the artificial activities to insert are two:
		//		1) <first_act>_..._<last_act>_split_<prec_act>
		// 		2) <first_act>_merge_<succ_act>
		// The method has therefore two parts: the first adds the activity 1) and the second adds the activity 2)
	Trace insertJoin(Trace BaseTrace, int previousindex, int successiveindex, String PrecActivity, String SuccActivity,ArrayList<Activity> PrecedentActivities, ProcessInstance PIToAssign) {
		System.out.println("Previous index: " + previousindex + ", Successive index:" + successiveindex + " Precedent activity: "+ PrecActivity + ", Successive activity: "+ SuccActivity);
		
		// This is the first part of the method that adds the activity 1). The SplitActivityName is built
		// considering first the empty string, followed by all the activities of the merge relation.
		String SplitActivityName = "";
		for(int i = 0; i<PrecedentActivities.size(); i++) {
			SplitActivityName = SplitActivityName +PrecedentActivities.get(i).getName();
			if(i != PrecedentActivities.size()-1)
				SplitActivityName = SplitActivityName + "_";
		}
		
		// The activity is here built, by comparing the activity names fetched initially with the
		// SplitActivityName augmented with _split_ and the name of the successive activity.
		// Beware of misalignments.
		ActivityInstance SplitActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(SplitActivityName+"_split_"+SuccActivity))
				SplitActivity = new ActivityInstance(-1,PIToAssign,A.get(i));
		System.out.println("SplitActivityName: " + SplitActivityName+"_split_"+SuccActivity);
		System.out.println("SplitActivityName of the actual activity: " + SplitActivity.getA().getName());
		
		// Here the artificial activity 2) is added. Beware of misalignments.
		ActivityInstance MergeActivity = null;
		for(int i=0; i<A.size(); i++)
			if(A.get(i).getName().equals(PrecActivity+"_merge_"+SuccActivity))
				MergeActivity = new ActivityInstance(-1, PIToAssign, A.get(i));
		System.out.println("MergeActivityName of the actual activity: " + MergeActivity.getA().getName());
		System.out.println();
		// The activity instance is added to the Base Trace.
		BaseTrace.getAI().add(previousindex+1, MergeActivity);
		BaseTrace.getAI().add(successiveindex+1, SplitActivity);
		
		return BaseTrace;
	}
	
	// This method sorts the events by their timestamps
	ArrayList<Event> sort(ArrayList<Event> EventListToOrder){
		int n = EventListToOrder.size();
	    for (int i = 1; i < n; ++i) {
	        Event key = EventListToOrder.get(i);
	        int j = i - 1;
	        while (j >= 0 && EventListToOrder.get(j).getT() > key.getT()) {
	        	EventListToOrder.set(j+1, EventListToOrder.get(j));
	            j = j - 1;
	        }
	        EventListToOrder.set(j + 1,key);
	    }
	    
	    return EventListToOrder;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
		/*Checker C = new Checker();
		boolean aware = false;
		ArrayList<Trace> NonConformantTraces = C.onlineConformanceChecking(aware);
		for(int i=0; i<NonConformantTraces.size(); i++) {
			System.out.print("Non conformant trace " + i + ": ");
			for(int j=0; j<NonConformantTraces.get(i).getAI().size(); j++)
				System.out.print(NonConformantTraces.get(i).getAI().get(j).getA().getName() + " ");
			System.out.println();
		}
		
		String Trial = "som_selstart_dmi_1_merge_som_sendMAreq";
		
		String [] Parts = Trial.split("_merge_");
		for(int i=0; i<Parts.length; i++) {
			System.out.println(Parts[i]);
		}
		if(Parts.length<=1) {
			System.out.println("The activity is not an artificial activity");
		}
		else {
			String [] NextActivities = Parts[1].split("som_");
			for(int i=0; i<NextActivities.length; i++)
				System.out.println(NextActivities[i]);
			if(NextActivities.length <= 2) {
				System.out.println("The found artificial activity is the merge activity of a join gateway");
				System.out.println(NextActivities.length);
				NextActivities[1] = "som_" + NextActivities[1];
				System.out.println(NextActivities[1]);
			}
			else if(NextActivities.length >2) {
				System.out.println("The found artificial activity is the merge activity of a split gateway");
				for(int i = 1; i<NextActivities.length; i++) {
					if(i<NextActivities.length-1) {
						NextActivities[i] = "som_" + NextActivities[i].substring(0,NextActivities[i].length()-1);
					}
					else {
						NextActivities[i] = "som_" + NextActivities[i];
					}
				}
				for(int i=0;i<NextActivities.length; i++) {
					System.out.println(NextActivities[i]);
				}
			}
		}
		*/
		/*String [] Parts2 = Parts[1].split("som_");
		for(int i=0; i<Parts2.length; i++) {
			System.out.println(Parts2[i]);
		}*/
	}
	
}
