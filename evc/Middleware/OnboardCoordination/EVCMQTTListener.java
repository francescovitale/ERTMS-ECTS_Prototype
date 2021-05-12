/* The listener has the job to receive the messages published on the Broker by other MQTT clients.
 * 
 * Reading the flow might be difficult, so here it's described what's the flow of topics related to a certain part of the procedure.
 * 
 * 1) Inserting the Driver ID
 * 		The DMI publishes a message to the Broker addressing the topic 'DriverIDEVC'
 * 		The EVC elaborates the request and publishes a message to the Broker addressing the topic 'DriverIDDMI'
 * 2) Validating the level
 * 		The DMI publishes a message to the Broker addressing the topic 'LevelEVC'
 *		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'retrievepos'
 *		The OB elaborates the message and publishes a message to the Broker addressing the topic 'LevelEVCOB'
 *		The EVC elaborates the message and publishes a message to the Broker addressing 'LevelDMI'
 * 3) Contacting the RBC
 * 		The DMI publishes a message to the Broker addressing the topic 'RBCEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker depending on the stored level
 * 			If the level is < 2, then it publishes a message to the Broker addressing the topic 'RBCDMI'
 * 			If the level is >= 2, then it publishes a message to the Broker addressing 'OpenConnRBC'
 * 				The RBC elaborates the message and publishes a message to the Broker addressing 'RBCEVCRBC'
 * 				The EVC elaborates the message and publishes a message to the Broker addressing 'RBCDMI'
 * 4) Performing the RBC routine
 * 		The DMI publishes a message to the Broker addressing the topic 'RBCRoutineEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'ElabPosRBC'
 * 		The RBC elaborates the message and publishes a message to the Broker addressing the topic 'RBCRoutineEVCRBC'
 * 		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'RBCRoutineDMI'
 * 5) Select the Mode
 * 		The DMI publishes a message to the Broker addressing the topic 'CheckLevSessEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker depending on the level
 * 			If the level is <2, then it publishes a message to the Broker addressing the topic 'CheckLevSessDMI'
 * 			If the level is >=2, then it publishes a message to the Broker addressing the topic 'CheckSessRBC'
 * 				The RBC elaborates the message and publishes a message to the Broker addressing the topic 'CheckLevSessEVCRBC'
 * 				The EVC elaborates the message and publishes a message to the Broker addressing the topic 'CheckLevSessDMI'
 * 6) Start procedure
 * 		The DMI publishes a message to the Broker addressing the topic 'StartEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker depending on the level
 * 			If the level is <2, then it publishes a message to the Broker addressing the topic 'StartDMI'
 * 			If the level is >=2, then it publishes a message to the Broker addressing the topic 'MAManagementRBC'
 * 				The RBC elaborates the message and publishes a message to the Broker addressing the topic 'StartEVCRBC'
 * 				The EVC elaborates the message and publishes a message to the Broker addressing the topic 'StartDMI'
 * 7) Acknowledgement
 * 		The DMI publishes a message to the Broker addressing the topic 'AckEVC'
 * 		The EVC elaborates the message and publishes a message to the Broker addressing the topic 'AckDMI'
 */

package Middleware.OnboardCoordination;

import ApplicationLayer.OnboardControl.EVCControl;
import ApplicationLayer.OnboardControl.Message;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class EVCMQTTListener implements IMqttMessageListener {

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		EVCControl EVC;
		EVCMQTTClient EMC;
		switch(topic) {
		case "DriverIDEVC":
			System.out.println("DriverIDEVC received");
			final MqttMessage driverID = message;
			new Thread()
			{
			    public void run() {
					try {
						EVCControl.getInstance().validateDriverID(driverID.toString());
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			}.start();
			break;
			
		case "LevelEVC":
			System.out.println("LevelEVC received");
			EVC = EVCControl.getInstance();
			//System.out.println(Integer.parseInt(message.toString()));
			final MqttMessage msgToPass = message;
			new Thread()
			{
			    public void run() {
					try {
						EVCControl.getInstance().validatePositionLevel(Integer.parseInt(msgToPass.toString()));
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
			    }
			}.start();
			break;
			
		case "LevelEVCOB":
			String[] DeserializedMsg = deserializeString(message);
			float lon = Float.parseFloat(DeserializedMsg[0]);
			float lat = Float.parseFloat(DeserializedMsg[1]);
			if(lon != -1 && lat != -1) {
				EVCControl.getInstance().setPCoordinates(lat, lon);
				EVCControl.getInstance().setPLastTimestamp(1);
			}
			// The validity of the position should be set here.
			
			EVCControl.getInstance().setTimedOut(false); // Set the Timed Out flag to false
			synchronized(EVCControl.getInstance()) {
				EVCControl.getInstance().notify();
			}
			break;
		case "RBCEVC":
			System.out.println("RBCEVC received");
			EVC = EVCControl.getInstance();
			String[] RetrievedParts = deserializeString(message);
			if(RetrievedParts[0].equals("yes"))
				EVC.setRetry(true);
			else
				EVC.setRetry(false);
			
			EMC = EVCMQTTClient.getInstance(null,null);
			EVC.sendEvent("som_openconn_rtm_1", EMC, Integer.parseInt(RetrievedParts[1]));
			final String CaseIDtoPass = RetrievedParts[1];
			new Thread()
			{
			    public void run() {
					try {
						EVCControl.getInstance().contactRBC(Integer.parseInt(CaseIDtoPass));
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
			    }
			}.start();
			
			break;
		case "RBCEVCRBC":
			System.out.println("RBCEVCRBC received");
			EVC = EVCControl.getInstance();
			EVC.setTimedOut(false);
			synchronized (EVC) {
				EVC.notifyAll();
			}
			EMC = EVCMQTTClient.getInstance(null,null);
			EMC.publishString("RBCDMI", "connectionopened");
			break;
		case "RBCRoutineEVC":
			System.out.println("RBCRoutineEVC received");
			final int receivedCaseId = Integer.parseInt(message.toString());
			EMC = EVCMQTTClient.getInstance(null, null);
			EVCControl.getInstance().sendEvent("som_checkpos_rbc_1", EMC, receivedCaseId);
			
			new Thread()
			{
			    public void run() {
					try {
						EVCControl.getInstance().RBCRoutineControl(receivedCaseId);
						
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
			    }
			}.start();
			break;
		case "CheckLevSessEVC":
			System.out.println("CheckLevSessEVC");
			EVC = EVCControl.getInstance();
			final int receivedCaseIDLevSess = Integer.parseInt(message.toString());
			if(EVC.getLevelValue() == 2 || EVC.getLevelValue() == 3) // only possible case for this implementation
			{
				new Thread()
				{
				    public void run() {
						try {
							EVCControl.getInstance().checkRBCSession(receivedCaseIDLevSess);
							
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
				    }
				}.start();
			}
			else {
				// If the level is 0,1 or NTC, no check should be done for the RBC session.
				EMC = EVCMQTTClient.getInstance(null, null);
				EMC.publishString("CheckLevSessDMI", "false"+":"+EVC.getLevelValue());
			}
			
			break;
		case "CheckLevSessEVCRBC":
			System.out.println("CheckLevSessEVCRBC");
			EVC = EVCControl.getInstance();
			EVC.setTimedOut(false);
			if(message.toString().equals("true"))
				EVC.setOpen(true);
			else
				EVC.setOpen(false);
			synchronized(EVC) {
				EVC.notify();
			}
			
			break;
		case "RBCRoutineEVCRBC":
			System.out.println("RBCRoutineEVCRBC");
			EVCControl.getInstance().setTimedOut(false);
			synchronized(EVCControl.getInstance()) {
				EVCControl.getInstance().notify();
			}
			break;
		case "TrainDataEVC":
			System.out.println("TrainData received");
			break;
			
		case "StartEVC":
			System.out.println("StartEVC received");
			final int receivedCaseId2 = Integer.parseInt(message.toString());
			new Thread()
			{
			    public void run() {
					try {
						EVCControl.getInstance().startProcedureControl(receivedCaseId2);
						
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
			    }
			}.start();
			break;
		case "StartEVCRBC":
			EVC = EVCControl.getInstance();
			EMC = EVCMQTTClient.getInstance(null,null);
			synchronized(EVC) {
				EVC.notify();
			}
			EMC.publishString("StartDMI", message.toString());
			
			break;
		case "AckEVC":
			System.out.println("AckEVC received");
			final int receivedCaseId3 = Integer.parseInt(message.toString());
			new Thread()
			{
			    public void run() {
					try {
						EVCControl.getInstance().ackManagement(receivedCaseId3);
						
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
			    }
			}.start();
			break;
		}
	}
	
	public Message deserializeMessage(MqttMessage message) {
		Message DeserializedMsg;
		String msgToDeserialize = message.toString();
		String [] Parts = msgToDeserialize.split("\\!");
		DeserializedMsg = new Message(Parts[0],Parts[1],(int)Integer.parseInt(Parts[2]),(int)Integer.parseInt(Parts[3]),Parts[4],Parts[5],Boolean.getBoolean(Parts[6]),Parts[7],(int)Integer.parseInt(Parts[8]));
		return DeserializedMsg;
	}
	
	public String[] deserializeString(MqttMessage message) {
		String msgToDeserialize = message.toString();
		String [] Parts = msgToDeserialize.split("\\:");
		return Parts;
	}
 
}
