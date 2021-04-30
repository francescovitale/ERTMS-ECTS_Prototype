package Middleware;

import ad.AnomalyDetectorController;
import ad.Message;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class ADMQTTListener implements IMqttMessageListener {

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		Message msg;
		AnomalyDetectorController ADC;
		switch(topic) {
		case "StartOfMissionEvent":
			System.out.println("Received");
			msg = deserializeMessage(message);
			ADC = new AnomalyDetectorController();
			ADC.logEvent(msg, "StartOfMission");
			break;
			
		default:
			throw new Exception();
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
