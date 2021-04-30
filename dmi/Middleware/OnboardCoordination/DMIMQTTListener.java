package Middleware.OnboardCoordination;


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
			synchronized(IC)
			{
			    IC.notify(); // the listener wakes the thread up.
			}
			DMIMQTTClient DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883","dmi");
			Message msg = deserializeMessage(message);
			msg.setTimestamp(2);
			if(msg.getRetry() == true) {
				msg.setActivity("som_retry_dmi_1");
				msg.setCaseID(DisplayBoundary.getCaseID());
				msg.setResource("dmi");
				DMC.publishMessage("StartOfMissionEvent", msg);
				new Thread()
				{
				    public void run() {
				    	DisplayBoundary DB;
						try {
							DB = new DisplayBoundary();
							DB.insertDriverID();
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
				msg.setActivity("som_storeid_evc_1");
				msg.setCaseID(DisplayBoundary.getCaseID());
				msg.setResource("evc");
				DMC.publishMessage("StartOfMissionEvent", msg);
				
				new Thread()
				{
				    public void run() {
				        MissionControl MC;
						try {
							MC = MissionControl.getInstance();
							MC.validateLevel();
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        
				    }
				}.start();
				
			}
			
			break;
			
		case "LevelDMI":
			System.out.println("LevelDMI received");
			MC = MissionControl.getInstance();
			synchronized(MC)
			{
			    MC.notify(); // the listener wakes the thread up.
			}
			DMC = DMIMQTTClient.getInstance("tcp://127.0.0.1:1883","dmi");
			if(message.toString().equals("ok")) {
				Message M = new Message(3,"evc",0,0,"","",true,"som_validate_evc_1",DisplayBoundary.getCaseID());
				DMC.publishMessage("StartOfMissionEvent", M);
				new Thread()
				{
				    public void run() {
						DisplayBoundary DB = new DisplayBoundary();
						try {
							DB.contactRBC();
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
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
				    	DisplayBoundary DB;
						DB = new DisplayBoundary();
						DB.insertLevel(); // this has yet to be implemented
				        
				    }
				}.start();
				
			}
			break;
			
		case "TerminalDMI":
			System.out.println("Terminal received");
			break;
			
		case "RBCDMI":
			System.out.println("RBC received");
			MC = MissionControl.getInstance();
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
						} 
				    }
				}.start();
			}
			else {
				// To implement..
			}
			break;
			
		case "RBCRoutineDMI":
			System.out.println("RBCRoutineDMI received");
			MC = MissionControl.getInstance();
			synchronized(MC)
			{
			    MC.notify(); // the listener wakes the thread up.
			}
			new Thread()
			{
			    public void run() {
			        DisplayBoundary DB = new DisplayBoundary();
					try {
						DB.selectMode();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			}.start();
			
			break;
		case "CheckLevSessDMI":
			System.out.println("CheckLevSessDMI received");
			String [] Parts = message.toString().split("\\:");
			DisplayBoundary.setObtainedLevel(Integer.parseInt(Parts[1]));
			IC = InputControl.getInstance();
			synchronized(IC) {
				IC.notify();
			}
			break;
		case "TrainDataDMI":
			System.out.println("TrainData received");
			break;
			
		case "StartDMI":
			System.out.println("StartDMI received");
			DisplayBoundary.setGrantedMA(message.toString());
			MC = MissionControl.getInstance();
			synchronized(MC) {
				MC.notify();
			}
			break;
			
		case "AckDMI":
			System.out.println("AckDMI received");
			MC = MissionControl.getInstance();
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
		String [] Parts = msgToDeserialize.split("\\:");
		// Timestamp:Resource:Level:TrainData:Mode:TerminalData:Retry:Activity:CaseID
		DeserializedMsg = new Message((int)Integer.parseInt(Parts[0]),Parts[1],
				(int)Integer.parseInt(Parts[2]),(int)Integer.parseInt(Parts[3]),Parts[4],Parts[5],Boolean.getBoolean(Parts[6]),Parts[7],(int)Integer.parseInt(Parts[8]));
		return DeserializedMsg;
	}
	

}
