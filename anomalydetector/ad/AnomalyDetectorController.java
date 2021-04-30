package ad;

import org.eclipse.paho.client.mqttv3.MqttException;

import Middleware.ADMQTTClient;
import ObjectAbstraction.Logger;

public class AnomalyDetectorController {

	public AnomalyDetectorController() {}
	public void initializeClient() throws MqttException {
		
		ADMQTTClient AMC = ADMQTTClient.getInstance("tcp://127.0.0.1:1883", "adc");
		AMC.connect();
		AMC.initializeListener();
	}
	public void logEvent(Message EventToLog, String ProcessName) {
		Logger L = new Logger();
		L.logEvent(EventToLog.getTimestamp(), EventToLog.getCaseID(), EventToLog.getResource(),EventToLog.getActivity(), ProcessName);		
	
	}
	public void sendAnomalousTrace() {
	}
	
	
	public static void main(String[] args) throws Exception {
		AnomalyDetectorController ADC = new AnomalyDetectorController();
		ADC.initializeClient();
	}
}
