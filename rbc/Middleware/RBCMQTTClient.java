package Middleware;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import ApplicationLogic.Message;

public class RBCMQTTClient {
	private volatile static RBCMQTTClient RMC = null;
	
	static MqttClient Client;
	RBCMQTTListener Listener;
	
	private RBCMQTTClient(String broker, String cid) throws MqttException {
		Client = new MqttClient(broker, cid);
	}
	public static RBCMQTTClient getInstance(String broker, String cid) throws MqttException {
		if(RMC==null) {
			synchronized(RBCMQTTClient.class) {
				if(RMC==null) {
					RMC = new RBCMQTTClient(broker, cid);
				}
			}
		}
		return RMC;
	}
	public void connect() throws MqttSecurityException, MqttException {
		MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        Client.connect(connOpts);
        System.out.println("Connection established to the broker");
	}
	public void initializeListener() throws MqttException {
		Listener = new RBCMQTTListener();
		Client.subscribe("OpenConnRBC", Listener);
		Client.subscribe("ElabPosRBC", Listener);
		Client.subscribe("CheckSessRBC", Listener);
		Client.subscribe("MAManagementRBC", Listener);
	}
	public void disconnect() throws MqttException {
		Client.disconnect();
	}
	public void publishMessage(String Topic, Message msg) throws MqttPersistenceException, MqttException {
		String SerializedMsg = serializeMessage(msg);
		MqttMessage msgToSend = new MqttMessage(SerializedMsg.getBytes());
		Client.publish(Topic, msgToSend);
        System.out.println("Message published");
	}
	public void publishString(String Topic, String msg) throws MqttPersistenceException, MqttException {
		MqttMessage msgToSend = new MqttMessage(msg.getBytes());
		Client.publish(Topic, msgToSend);
		System.out.println("Message published");
	}
	public String serializeMessage(Message msg) {
		// Timestamp:Resource:Level:TrainData:Mode:TerminalData:Retry:Activity:CaseID
		String SerializedMsg = msg.getTimestamp()+"!"+msg.getResource()+"!"+msg.getLevel()+"!"+msg.getTrainData()+"!"+msg.getMode()
		+"!"+msg.getTD()+"!"+msg.getRetry()+"!"+msg.getActivity()+"!"+msg.getCaseID();
		return SerializedMsg;
	}
}
