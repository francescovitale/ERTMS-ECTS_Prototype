package ObjectAbstraction.PMLogic;
import java.util.ArrayList;
import java.util.HashMap;


public class PetriNet {
	
class UnfiredActivity{
	Activity A;
	boolean taken;
	
	UnfiredActivity(Activity Ain){
		A = Ain;
		taken = false;
	}
	
	boolean isTaken() {
		return taken;
	}
	
	void setTaken(boolean t) {
		taken = t;
	}
	
	Activity getActivity() {
		return A;
	}
}
	
private
	ProcessModel P;
	ArrayList<String> Places;
	ArrayList<Activity> Transitions;
	BiHashMap<String,String,Boolean> PT;
	BiHashMap<String,String,Boolean> TP;
	HashMap<String,Integer> Marking;
	
	static ArrayList<UnfiredActivity> UA = new ArrayList<UnfiredActivity>();
public
	PetriNet() {
		P = null;
		Places = null;
		Transitions = null;
		PT = null;
		TP = null;
		Marking = null;
	}

	public PetriNet(ProcessModel P_in, ArrayList<String> Places_in, ArrayList<Activity> Transitions_in, BiHashMap<String,String,Boolean> PT_in, BiHashMap<String,String,Boolean> TP_in,HashMap<String,Integer> Marking_in ) {
		P = P_in;
		Places = Places_in;
		Transitions = Transitions_in;
		PT = PT_in;
		TP = TP_in;
		Marking = Marking_in;
	}

	public PetriNet(PetriNet PN){
		P = new ProcessModel(PN.getP());
		Places = PN.Places;
		Transitions = new ArrayList<Activity>(PN.getTransitions());
		PT = new BiHashMap<String,String,Boolean>(PN.getPT());
		TP = new BiHashMap<String,String,Boolean>(PN.getTP());
		Marking = new HashMap<String,Integer>(PN.getMarking());
	}

	ReplayParameters fire(Activity A, ReplayParameters P) {
		
		int missingTokens = countM(A.getName(),false);
		
		if(P.getM()<P.getM()+missingTokens) {
			
			ArrayList<Activity> EnabledActivities = getEnabledActivities();
			//System.out.println(EnabledActivities.size());
			
			//System.out.println();
			for(int i=0; i<EnabledActivities.size(); i++) {
				if(UA.size() == 0) {
					UA.add(new UnfiredActivity(EnabledActivities.get(i)));
				}
				else {
					boolean found = false;
					int k = 0;
					while(found == false && k < UA.size()) {
						if(EnabledActivities.get(i).getName().equals(UA.get(k).getActivity().getName())) {
							found = true;
						}
						k++;
					}
					if(found == false) {
						UA.add(new UnfiredActivity(EnabledActivities.get(i)));
					}
					
				}
			}
			/*for(int i=0; i<UA.size(); i++) {
				System.out.println(UA.get(i).isTaken());
			}*/
		}
		
		P.setM(P.getM() + countM(A.getName(),true));
		P.setC(P.getC() + countC(A.getName()));
		P.setP(P.getP() + countP(A.getName()));
		if(Marking.get(Places.get(Places.size()-1)) != 0) {
			P.setC(P.getC()+1);
			Marking.put(Places.get(Places.size()-1), Marking.get(Places.get(Places.size()-1))-1);
			P.setEnd(true);
			P.setR(countR());
		}
		
		return P;
	};
	
	private ArrayList<Activity> getEnabledActivities() {
		ArrayList<Activity> EnabledActivities = new ArrayList<Activity>();
		int[] MarkingVector = new int[Places.size()];
		int[] NeededMarking = new int[Places.size()];
		for(int i=0; i<MarkingVector.length; i++)
			MarkingVector[i] = 0;
		for(int i=0; i<Places.size(); i++) {
			MarkingVector[i] = Marking.get(Places.get(i));
		}
		for(int i=0; i<Transitions.size(); i++) {
			for(int j=0; j<Places.size(); j++) {
				NeededMarking[j] =  PT.get(Places.get(j), Transitions.get(i).getName()) ? 1 : 0;
			}
			boolean canFire = true;
			for(int j=0; j<Places.size(); j++) {
				if(MarkingVector[j] < NeededMarking[j])
					canFire = false;
			}
			if(canFire) {
				// This section is for when the successive activities are artificial: there's
				// the need to extract the next non-artificial activity.
							
				// We can exploit the fact that the next artificial activity can only be:
				// 		1) The merge activity of a join gateway. In this case, the merge activity
				//		   has the form <internal_act>_merge_<succ_act>
				// 		2) The merge activity of a split gateway. In this case, the merge activity
				// 		   has the form <prev_act>_merge_<internal_act>_..._<internal_act>
				
				String [] Parts = Transitions.get(i).getName().split("_merge_");
				if(Parts.length<=1) {
					//System.out.println("The activity is not an artificial activity");
					EnabledActivities.add(Transitions.get(i));
				}
				else {
					String [] NextActivities = Parts[1].split("som_");
					if(NextActivities.length <= 2) {
						/*System.out.println("The found artificial activity is the merge activity of a join gateway");
						System.out.println(Parts[1]);*/
						NextActivities[1] = "som_" + NextActivities[1];
						for(int k=0; k<Transitions.size(); k++) {
							if(Transitions.get(k).getName().equals(NextActivities[1])) {
								/*System.out.println("Transitions.get(k).getName(): "+ Transitions.get(k).getName());
								System.out.println("NextActivities[1]: "+ NextActivities[1]);		*/
								EnabledActivities.add(Transitions.get(k));
							}
						}
						//System.out.println(NextActivities[1]);
					}
					else if(NextActivities.length >2) {
						/*System.out.println("The found artificial activity is the merge activity of a split gateway");
						System.out.println(Parts[1]);*/
						for(int j = 1; j<NextActivities.length; j++) {
							if(j<NextActivities.length-1) {
								NextActivities[j] = "som_" + NextActivities[j].substring(0,NextActivities[j].length()-1);
								for(int k=0; k<Transitions.size(); k++) {
									
									if(Transitions.get(k).getName().equals(NextActivities[j])) {
										/*System.out.println("Transitions.get(k).getName(): "+ Transitions.get(k).getName());
										System.out.println("NextActivities[j]: "+ NextActivities[j]);*/
										EnabledActivities.add(Transitions.get(k));
									}
								}
							}
							else {
								NextActivities[j] = "som_" + NextActivities[j];
								for(int k=0; k<Transitions.size(); k++) {
									if(Transitions.get(k).getName().equals(NextActivities[j])) {
										/*System.out.println("Transitions.get(k).getName(): "+ Transitions.get(k).getName());
										System.out.println("NextActivities[j]: "+ NextActivities[j]);*/
										EnabledActivities.add(Transitions.get(k));
									}
								}
							}
						}
					}
				}
			}
		}
		
		
		return EnabledActivities;
	}
	
	public ArrayList<Activity> getUnfiredActivities(){
		ArrayList<Activity> ReturnedList = new ArrayList<Activity>();
		
		for(int i=0; i<UA.size(); i++) {
			//System.out.println("getUnfiredActivities() " + UA.get(i).getActivity().getName() + " " + UA.get(i).isTaken());
			if(!UA.get(i).isTaken()) {
				
				UA.get(i).setTaken(true);
				
				ReturnedList.add(UA.get(i).getActivity());
			}
			
		}
		
		return ReturnedList;
	}

	int countC(String ActivityName) {
		int c = 0;
		ArrayList<String> PlacesToReset = new ArrayList<String>();
		for(int i=0; i<Places.size(); i++) {
			if(PT.get(Places.get(i), ActivityName) == true) {
				c++;
				PlacesToReset.add(Places.get(i));
			}
		}
		updateMarking(null, PlacesToReset);
		return c;
	}
	
	int countP(String ActivityName) {
		int p = 0;
		ArrayList<String> PlacesToSet = new ArrayList<String>();
		for(int i=0; i<Places.size(); i++) {
			if(TP.get(Places.get(i), ActivityName) == true) {
				p++;
				PlacesToSet.add(Places.get(i));
			}
		}
		updateMarking(PlacesToSet, null);
		return p;
	}
	
	int countM(String ActivityName, boolean clear) {
		int m = 0;
		ArrayList<String> PlacesToSet = new ArrayList<String>();
		for(int i=0; i<Places.size(); i++) {
			if(PT.get(Places.get(i), ActivityName) == true && Marking.get(Places.get(i)) == 0) {
				
				m++;
				PlacesToSet.add(Places.get(i));
			}
		}
		if(clear == true)
			updateMarking(PlacesToSet, null);
		return m;
	}
	
	int countR() {
		int r = 0;
		for(int i=0; i<Places.size(); i++) {
			r = r + Marking.get(Places.get(i));
		}
		return r;
	}
	
	void initializeMarking() {
		for(int i=0; i<Places.size(); i++) {
			if(i == 0)
				Marking.put(Places.get(i), 1);
			else
				Marking.put(Places.get(i), 0);
		}
	};
	void updateMarking(ArrayList<String> PlacesToSet, ArrayList<String> PlacesToReset) {
		if(PlacesToSet != null) {
			for(int i=0; i<PlacesToSet.size(); i++) {
				Marking.put(PlacesToSet.get(i), Marking.get(PlacesToSet.get(i))+1);
			}
		}
		if(PlacesToReset != null) {
			for(int i=0; i<PlacesToReset.size(); i++) {
				Marking.put(PlacesToReset.get(i), Marking.get(PlacesToReset.get(i))-1);
			}
		}
	}
	
	void resetUnfiredActivities() {
		UA = new ArrayList<UnfiredActivity>();
	}
	
	
	
	public ProcessModel getP() {
		return P;
	}
	public void setP(ProcessModel p) {
		P = p;
	}
	public ArrayList<String> getPlaces() {
		return Places;
	}
	public void setPlaces(ArrayList<String> places) {
		Places = places;
	}
	public ArrayList<Activity> getTransitions() {
		return Transitions;
	}
	public void setTransitions(ArrayList<Activity> transitions) {
		Transitions = transitions;
	}
	public BiHashMap<String, String, Boolean> getPT() {
		return PT;
	}
	public void setPT(BiHashMap<String, String, Boolean> pT) {
		PT = pT;
	}
	public BiHashMap<String, String, Boolean> getTP() {
		return TP;
	}
	public void setTP(BiHashMap<String, String, Boolean> tP) {
		TP = tP;
	}
	public HashMap<String, Integer> getMarking() {
		return Marking;
	}
	public void setMarking(HashMap<String, Integer> marking) {
		Marking = marking;
	};
}
