package ApplicationLogic;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import Middleware.RBCHTTPClient;
import Middleware.RBCMQTTClient;




public class RBCControl{

	RBCMQTTClient RMC;
	RBCHTTPClient RHC;
	private volatile static RBCControl RC = null;
	Position P;
	boolean Accepted = false;
	boolean Open = false;
	
	
	private RBCControl() throws MqttException {
		P = new Position();
	}
	public static RBCControl getInstance() throws MqttException {
		if(RC==null) {
			synchronized(RBCControl.class) {
				if(RC==null) {
					RC = new RBCControl();
				}
			}
		}
		return RC;
	}
	public void initializeClient() throws MqttException {
		RMC = RBCMQTTClient.getInstance("tcp://192.168.1.2:1883", "rbc");
		RHC = RBCHTTPClient.getInstance();
		RMC.connect();
		RMC.initializeListener();
	}
	public void openConnection() throws MqttPersistenceException, MqttException {
		Open = true;
		RMC.publishString("RBCEVCRBC", "");
	}
	public void elaboratePosition(int CaseID) throws Exception {
		// If the elaborated position (which is not yet to be sent) is valid, do one thing, otherwise do the other.
		// We assume for now the position (not) sent by the EVC is valid.
		if(P.isValid()) { // For any reason, this block may not be executed.. Big anomaly.
			// Store the position..
			RMC = RBCMQTTClient.getInstance(null, null);
			
			sendEvent("som_storepos_rbc_1",RHC,CaseID);
			// Store the accepted flag..
			Accepted = true;
			sendEvent("som_storevalacc_rbc_1",RHC, CaseID);
			
		}
		else {
			// To implement..
		}
		RMC.publishString("RBCRoutineEVCRBC", String.valueOf(Accepted));
	}
	public void manageMA(int CaseID) throws Exception {
		RMC = RBCMQTTClient.getInstance(null, null);
		boolean freeRoute;
		
		freeRoute = checkTrainRoute(CaseID);
		
		if(freeRoute && checkPosition(CaseID)) {
			sendEvent("som_grantFS_rbc_1",RHC, CaseID);
			RMC.publishString("StartEVCRBC", "FS");}
		else if(freeRoute & !checkPosition(CaseID)) {
			sendEvent("som_grantOS_rbc_1",RHC, CaseID);
			RMC.publishString("StartEVCRBC", "OS");
		}
		else {
			sendEvent("som_grantSR_rbc_1",RHC, CaseID);
			RMC.publishString("StartEVCRBC", "SR");
		}
		
	}
	
	private boolean checkTrainRoute(int CaseID) throws Exception {
		sendEvent("som_checktrainroute_rbc_1",RHC, CaseID);
		return true;
	}
	private boolean checkPosition(int CaseID) throws Exception {
		sendEvent("som_checkval_rbc_1",RHC, CaseID);
		return P.isValid();
	}
	public boolean isOpen() {
		return Open;
	}
	public void setOpen(boolean open) {
		Open = open;
	}
	public void setValidPos(boolean flag) {
		P.setValid(flag);
	}
	public void sendEvent(String activity,RBCHTTPClient RHC, int CaseID) throws Exception {
		String TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		Message M = new Message(TS,"rbc",0,0,"","",false,activity,CaseID);
		RHC.postEvent(M);
	}
	public static void main(String[] args) throws MqttException {
		RBCControl.getInstance().initializeClient();
	}
	

}
