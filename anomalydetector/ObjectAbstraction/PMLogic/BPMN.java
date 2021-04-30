package ObjectAbstraction.PMLogic;
import java.util.ArrayList;

public class BPMN {
private
	ProcessModel PM;
	ArrayList<MergeRelation> MR;
	ArrayList<SplitRelation> SR;
public
	BPMN() {
		PM = null;
		MR = null;
		SR = null;
	}

	public BPMN(BPMN B_in){
		PM = B_in.PM;
		MR = B_in.MR;
		SR = B_in.SR;
	}

	public ArrayList<MergeRelation> getMR() {
		return MR;
	}

	public void setMR(ArrayList<MergeRelation> mR) {
		MR = mR;
	}

	public ArrayList<SplitRelation> getSR() {
		return SR;
	}

	public void setSR(ArrayList<SplitRelation> sR) {
		SR = sR;
	}

	public ProcessModel getPM() {
		return PM;
	}

	public void setPM(ProcessModel pM) {
		PM = pM;
	}
}
