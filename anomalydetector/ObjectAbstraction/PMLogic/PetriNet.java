package ObjectAbstraction.PMLogic;
import java.util.ArrayList;
import java.util.HashMap;

public class PetriNet {
private
	ProcessModel P;
	ArrayList<String> Places;
	ArrayList<Activity> Transitions;
	BiHashMap<String,String,Boolean> PT;
	BiHashMap<String,String,Boolean> TP;
	HashMap<String,Integer> Marking;
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
		
		P.setM(P.getM() + countM(A.getName()));
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
	
	int countM(String ActivityName) {
		int m = 0;
		ArrayList<String> PlacesToSet = new ArrayList<String>();
		for(int i=0; i<Places.size(); i++) {
			if(PT.get(Places.get(i), ActivityName) == true && Marking.get(Places.get(i)) == 0) {
				
				m++;
				PlacesToSet.add(Places.get(i));
			}
		}
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
