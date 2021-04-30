package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class SplitRelation {
private
	Activity PrecedentActivity;
	ArrayList<Activity> SuccessiveActivities;
	
public
	SplitRelation() {
	
	}
	public SplitRelation(Activity PA, ArrayList<Activity> SA) {
		PrecedentActivity = PA;
		SuccessiveActivities = SA;
	
	}
	SplitRelation(SplitRelation SR){
		PrecedentActivity = SR.getPrecedentActivity();
		SuccessiveActivities = SR.getSuccessiveActivities();
	}

public Activity getPrecedentActivity() {
	return PrecedentActivity;
}

public void setPrecedentActivity(Activity precedentActivity) {
	PrecedentActivity = precedentActivity;
}

public ArrayList<Activity> getSuccessiveActivities() {
	return SuccessiveActivities;
}

public void setSuccessiveActivities(ArrayList<Activity> successiveActivities) {
	SuccessiveActivities = successiveActivities;
}
}
