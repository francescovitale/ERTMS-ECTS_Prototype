package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class MergeRelation {
private
	Activity SuccessiveActivity;
	ArrayList<Activity> PrecedentActivities;
public
	MergeRelation() {}

	public MergeRelation(Activity SA, ArrayList<Activity> PA) {
	SuccessiveActivity = SA;
	PrecedentActivities = PA;
}
	MergeRelation(MergeRelation MR){
		SuccessiveActivity = new Activity(MR.getSuccessiveActivity());
		PrecedentActivities = new ArrayList<Activity>(MR.getPrecedentActivities());
	}
public Activity getSuccessiveActivity() {
	return SuccessiveActivity;
}
public void setSuccessiveActivity(Activity successiveActivity) {
	SuccessiveActivity = successiveActivity;
}
public ArrayList<Activity> getPrecedentActivities() {
	return PrecedentActivities;
}
public void setPrecedentActivities(ArrayList<Activity> precedentActivities) {
	PrecedentActivities = precedentActivities;
}
}
