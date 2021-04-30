package ApplicationLogic;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import Middleware.RBCMQTTClient;




public class RBCControl{

	RBCMQTTClient RMC;
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
		RMC = RBCMQTTClient.getInstance("tcp://127.0.0.1:1883", "rbc");
		RMC.connect();
		RMC.initializeListener();
	}
	
	public void openConnection() throws MqttPersistenceException, MqttException {
		Open = true;
		RMC.publishString("RBCEVCRBC", "");
	}
	public void elaboratePosition(int CaseID) throws MqttException {
		// If the elaborated position (which is not yet to be sent) is valid, do one thing, otherwise do the other.
		// We assume for now the position (not) sent by the EVC is valid.
		if(P.isValid()) {
			// Store the position..
			RMC = RBCMQTTClient.getInstance("tcp://127.0.0.1:1883", "rbc");
			Message M = new Message(6,"rbc",0,0,"","",true,"som_storepos_rbc_1",CaseID);
			RMC.publishMessage("StartOfMissionEvent", M);
			// Store the accepted flag..
			Accepted = true;
			M = new Message(7,"rbc",0,0,"","",true,"som_storevalacc_rbc_1",CaseID);
			RMC.publishMessage("StartOfMissionEvent", M);
			RMC.publishString("RBCRoutineEVCRBC", "");
		}
		else {
			// To implement..
		}
	}
	public void checkSession() {
		
	}
	
	public void manageMA(int CaseID) throws MqttException {
		// Check if the position is valid..
		
		// Check the train route.. Should contact the IXL.
		
		// Only option: the position is valid and the train route is free. FS is granted.
		
		RMC = RBCMQTTClient.getInstance("tcp://127.0.0.1:1883", "rbc");
		Message M = new Message(14,"rbc",0,0,"","",true,"som_checktrainroute_rbc_1",CaseID);
		RMC.publishMessage("StartOfMissionEvent", M);
		M = new Message(15,"rbc",0,0,"","",true,"som_checkval_rbc_1",CaseID);
		RMC.publishMessage("StartOfMissionEvent", M);
		M = new Message(16,"rbc",0,0,"","",true,"som_grantFS_rbc_1",CaseID);
		RMC.publishMessage("StartOfMissionEvent", M);
		
		RMC.publishString("StartEVCRBC", "FS");
		
	}

	public static void main(String[] args) throws MqttException {
		RBCControl.getInstance().initializeClient();

	}
	public boolean isOpen() {
		return Open;
	}
	public void setOpen(boolean open) {
		Open = open;
	}
	

}
