package Middleware;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import ApplicationLogic.Message;

public class RBCHTTPClient {
	private volatile static RBCHTTPClient RHC;
	
	private RBCHTTPClient() {
		
	}
	
	public static RBCHTTPClient getInstance() {
		if(RHC == null) {
			synchronized(RBCHTTPClient.class) {
				if(RHC == null) {
					RHC = new RBCHTTPClient();
				}
			}
		}
		return RHC;
	}
	
	public void postEvent(Message Event) throws IOException, InterruptedException{
		
		String SerializedEvent = serializeMessage(Event);
		
		HttpClient client = HttpClient.newBuilder()
			      .version(Version.HTTP_1_1)
			      .build();
		HttpRequest request = HttpRequest.newBuilder()
			      .uri(URI.create("http://192.168.1.2:8080/ad/log"))
			      .header("Content-Type", "text/html")
			      .POST(BodyPublishers.ofString(SerializedEvent))
			      .build();
		
		client.send(request, BodyHandlers.ofString());
		
		/*HttpResponse<String> response =
			      
			System.out.println(response.statusCode());
			System.out.println(response.body());*/
	}
	
	public String serializeMessage(Message msg) {
		// Timestamp:Resource:Level:TrainData:Mode:TerminalData:Retry:Activity:CaseID
		String SerializedMsg = msg.getTimestamp()+"!"+msg.getResource()+"!"+msg.getLevel()+"!"+msg.getTrainData()+"!"+msg.getMode()
		+"!"+msg.getTD()+"!"+msg.getRetry()+"!"+msg.getActivity()+"!"+msg.getCaseID();
		return SerializedMsg;
	}
}
