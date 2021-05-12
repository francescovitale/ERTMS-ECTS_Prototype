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


import java.util.Random;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ApplicationLayer.DisplayBoundary;
import ApplicationLayer.OnboardControl.InputControl;
import ApplicationLayer.OnboardControl.Message;
import ApplicationLayer.OnboardControl.MissionControl;

public class DMIMQTTListener implements IMqttMessageListener {

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		InputControl IC;
		MissionControl MC;
		
		switch(topic) {
		
		case "DriverIDDMI":
			IC = InputControl.getInstance();
			IC.setTimedOut(false); // The controller hasn't timed out: the message has been received!
			synchronized(IC)
			{
			    IC.notifyAll(); // the listener wakes the thread up.
			}
			if(message.toString().equals("retry")) {
				System.out.println("The driver ID is invalid.");
				IC.sendEvent("som_retry_dmi_1",DMIMQTTClient.getInstance(null,null));
				new Thread()
				{
				    public void run() {
						try {
							
							DisplayBoundary.getInstance().insertDriverID();
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
				// store the driver ID: should be done by evc...
				System.out.println("Storing the driver ID");
				IC.sendEvent("som_storeid_evc_1",DMIMQTTClient.getInstance(null,null));
				new Thread()
				{
				    public void run() {
				        MissionControl MC;
						try {
							MC = MissionControl.getInstance();
							MC.validateLevel();
						}
						catch (Exception e) {
							System.out.println("A problem has incurred. Please, try again"); // This happens because the process couldn't communicate with the EVC, or the EVC couldn't communicate with the sensor.
							try {
								MC = MissionControl.getInstance();
							} catch (MqttException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							Random rand = new Random();
							try {
								DisplayBoundary.getInstance().setCaseID(rand.nextInt(10000000));
								DisplayBoundary.getInstance().standByMenu();// A new Case ID is created
								// The menu is re-proposed to the driver.
							} catch (Exception e1) {
								
								e1.printStackTrace();
							} 
						}
				        
				    }
				}.start();
				
			}
			
			break;
		case "LevelDMI":
			System.out.println("LevelDMI received");
			MC = MissionControl.getInstance();
			MC.setTimedOut(false); // The controller hasn't timed out: the message has been received!
			synchronized(MC)
			{
			    MC.notifyAll(); // the listener wakes the thread up.
			}
			if(message.toString().equals("ok")) {
				new Thread()
				{
				    public void run() {
						try {
							DisplayBoundary.getInstance().contactRBC();
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
				new Thread()
				{
				    public void run() {
						try {
							DisplayBoundary.getInstance().insertLevel();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // this has yet to be implemented
				        
				    }
				}.start();
				
			}
			break;
			
		case "RBCDMI":
			System.out.println("RBC received");
			MC = MissionControl.getInstance();
			MC.setTimedOut(false);
			synchronized(MC)
			{
			    MC.notify(); // the listener wakes the thread up.
			}
			if(message.toString().equals("connectionopened")) {
				// Only option implemented..
				new Thread()
				{
				    public void run() {
				        MissionControl MC;
						try {
							MC = MissionControl.getInstance();
							MC.RBCRoutine();
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							System.out.println("A problem has incurred. Please, try again"); // This happens because the process couldn't communicate with the EVC, or the EVC couldn't communicate with the sensor.
							Random rand = new Random();
							try {
								DisplayBoundary.getInstance().setSOMCaseID(rand.nextInt(10000000)); // A new Case ID is created
								DisplayBoundary.getInstance().standByMenu(); // The menu is re-proposed to the driver.
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} 
						} 
				    }
				}.start();
			}
			else if(message.toString().equals("giveup")) {
				new Thread()
				{
				    public void run() {
						try {
							DisplayBoundary DB = DisplayBoundary.getInstance();
							DB.selectMode();
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
			else if(message.toString().equals("retry")) {
				MC.sendEvent("som_retry_dmi_2",DMIMQTTClient.getInstance(null, null));
				System.out.println("The connection with the RBC has failed.");
				new Thread()
				{
				    public void run() {
						try {
							DisplayBoundary DB = DisplayBoundary.getInstance();
							DB.contactRBC();
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
			break;
		case "RBCRoutineDMI":
			System.out.println("RBCRoutineDMI received");
			MC = MissionControl.getInstance();
			MC.setTimedOut(false);
			synchronized(MC)
			{
			    MC.notify(); // the listener wakes the thread up.
			}
			new Thread()
			{
			    public void run() {
					try {
						DisplayBoundary.getInstance().selectMode();
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
		case "CheckLevSessDMI":
			System.out.println("CheckLevSessDMI received");
			String [] Parts = message.toString().split("\\:");
			DisplayBoundary.getInstance().setObtainedLevel(Integer.parseInt(Parts[1]));
			IC = InputControl.getInstance();
			IC.setTimedOut(false);
			synchronized(IC) {
				IC.notifyAll();
			}
			break;
		case "StartDMI":
			System.out.println("StartDMI received");
			DisplayBoundary.getInstance().setGrantedMA(message.toString());
			MC = MissionControl.getInstance();
			MC.setTimedOut(false);
			synchronized(MC) {
				MC.notify();
			}
			break;
		case "AckDMI":
			System.out.println("AckDMI received");
			MC = MissionControl.getInstance();
			MC.setTimedOut(false);
			synchronized(MC) {
				MC.notify();
			}
			break;
		default: throw new Exception();
		}
	}
	public Message deserializeMessage(MqttMessage message) {
		Message DeserializedMsg;
		String msgToDeserialize = message.toString();
		String [] Parts = msgToDeserialize.split("\\!");
		// Timestamp:Resource:Level:TrainData:Mode:TerminalData:Retry:Activity:CaseID
		DeserializedMsg = new Message(Parts[0],Parts[1],
				(int)Integer.parseInt(Parts[2]),(int)Integer.parseInt(Parts[3]),Parts[4],Parts[5],Boolean.getBoolean(Parts[6]),Parts[7],(int)Integer.parseInt(Parts[8]));
		return DeserializedMsg;
	}
	

}
