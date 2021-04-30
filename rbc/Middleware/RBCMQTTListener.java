package Middleware;

import ApplicationLogic.Message;
import ApplicationLogic.RBCControl;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class RBCMQTTListener implements IMqttMessageListener {

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		RBCControl RC;
		RBCMQTTClient RMC;
		switch(topic) {
		case "OpenConnRBC":
			System.out.println("OpenConnRBC received");
			RC = RBCControl.getInstance();
			RC.openConnection();
			break;
			
		case "ElabPosRBC":
			System.out.println("ElabPosRBC received");
			RC = RBCControl.getInstance();
			RC.elaboratePosition(Integer.parseInt(message.toString()));
			break;
			
		case "CheckSessRBC":
			System.out.println("CheckSessRBC received");
			RMC = RBCMQTTClient.getInstance("tcp://127.0.0.1:1883", "rbc");
			System.out.println(String.valueOf(RBCControl.getInstance().isOpen()));
			RMC.publishString("CheckLevSessEVCRBC",String.valueOf(RBCControl.getInstance().isOpen()));
			
			break;
		case "MAManagementRBC":
			RC = RBCControl.getInstance();
			RC.manageMA(Integer.parseInt(message.toString()));
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
