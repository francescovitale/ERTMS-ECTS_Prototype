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
			EVC = EVCControl.getInstance();
			EVC.validateDriverID(message.toString());
			break;
			
		case "LevelEVC":
			System.out.println("LevelEVC received");
			EVC = EVCControl.getInstance();
			//System.out.println(Integer.parseInt(message.toString()));
			EVC.validatePositionLevel(Integer.parseInt(message.toString()));
			break;
			
		case "RBCEVC":
			System.out.println("RBCEVC received");
			EVC = EVCControl.getInstance();
			Message M = new Message(4,"rtm",0,0,"","",true,"som_openconn_rtm_1",Integer.parseInt(message.toString()));
			EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
			EMC.publishMessage("StartOfMissionEvent", M);
			new Thread()
			{
			    public void run() {
					try {
						EVCControl.getInstance().contactRBC();
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
		case "RBCEVCRBC":
			System.out.println("RBCEVCRBC received");
			EVC = EVCControl.getInstance();
			synchronized (EVC) {
				EVC.notify();
			}
			EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
			EMC.publishString("RBCDMI", "connectionopened");
			break;
		case "RBCRoutineEVC":
			System.out.println("RBCRoutineEVC received");
			final int receivedCaseId = Integer.parseInt(message.toString());
			EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
			Message msg = new Message(5,"evc",0,0,"","",true,"som_checkpos_rbc_1",receivedCaseId);
			EMC.publishMessage("StartOfMissionEvent", msg);
			
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
					} 
			    }
			}.start();
			break;
		case "CheckLevSessEVC":
			System.out.println("CheckLevSessEVC");
			EVC = EVCControl.getInstance();
			if(EVC.getLevelValue() == 2 || EVC.getLevelValue() == 3) // only possible case for this implementation
			{
				new Thread()
				{
				    public void run() {
						try {
							EVCControl.getInstance().checkRBCSession();
							
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
				// to implement..
			}
			
			break;
		case "CheckLevSessEVCRBC":
			System.out.println("CheckLevSessEVCRBC");
			EVC = EVCControl.getInstance();
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
					} 
			    }
			}.start();
			break;
		case "StartEVCRBC":
			EVC = EVCControl.getInstance();
			EMC = EVCMQTTClient.getInstance("tcp://127.0.0.1:1883", "evc");
			synchronized(EVC) {
				EVC.notify();
			}
			EMC.publishString("StartDMI", message.toString());
			
			break;
		case "AckEVC":
			final int receivedCaseId3 = Integer.parseInt(message.toString());
			new Thread()
			{
			    public void run() {
					try {
						EVCControl EVC = EVCControl.getInstance();
						EVCControl.getInstance().ackManagement(receivedCaseId3);
						
					} catch (MqttException e) {
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
		String [] Parts = msgToDeserialize.split("\\:");
		DeserializedMsg = new Message((int)Integer.parseInt(Parts[0]),Parts[1],(int)Integer.parseInt(Parts[2]),(int)Integer.parseInt(Parts[3]),Parts[4],Parts[5],Boolean.getBoolean(Parts[6]),Parts[7],(int)Integer.parseInt(Parts[8]));
		return DeserializedMsg;
	}

}
