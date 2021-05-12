package ad;


public class Message {
	String Timestamp;
	String Resource;
	String Activity;
	int Level;
	int TrainData;
	String Mode;
	String TD;
	boolean Retry;
	int CaseID;
	
	public Message(String T, String R, int L, int TData, String M, String TermD, boolean Ret, String A, int CID) {
		Timestamp = T;
		Resource = R;
		Activity = A;
		Level = L;
		TrainData = TData;
		Mode = M;
		TD = TermD;
		Retry = Ret;
		CaseID = CID;
	}

	public String getTimestamp() {
		return Timestamp;
	}

	public void setTimestamp(String timestamp) {
		Timestamp = timestamp;
	}

	public String getResource() {
		return Resource;
	}

	public void setResource(String resource) {
		Resource = resource;
	}

	public int getLevel() {
		return Level;
	}

	public void setLevel(int level) {
		Level = level;
	}

	public int getTrainData() {
		return TrainData;
	}

	public void setTrainData(int trainData) {
		TrainData = trainData;
	}

	public String getMode() {
		return Mode;
	}

	public void setMode(String mode) {
		Mode = mode;
	}

	public String getTD() {
		return TD;
	}

	public void setTD(String tD) {
		TD = tD;
	}

	public boolean getRetry() {
		return Retry;
	}

	public void setRetry(boolean retry) {
		Retry = retry;
	}

	public String getActivity() {
		return Activity;
	}

	public void setActivity(String activity) {
		Activity = activity;
	}

	public int getCaseID() {
		return CaseID;
	}

	public void setCaseID(int caseID) {
		CaseID = caseID;
	}
	
	
}
