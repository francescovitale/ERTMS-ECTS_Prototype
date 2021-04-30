package ApplicationLayer;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttException;

import ApplicationLayer.OnboardControl.InputControl;
import ApplicationLayer.OnboardControl.MissionControl;

public class DisplayBoundary {
	private static Integer SOMCaseID = 1;
	private static int obtainedLevel = 2;
	private static String grantedMA = "FS";
	
	public DisplayBoundary() {}
	public void engage() throws Exception {
		MissionControl MC = MissionControl.getInstance();
		MC.initializeSOM();
		insertDriverID();
	}
	public void insertDriverID() throws Exception {
		System.out.print("Insert the Driver ID:");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		System.out.println(InsertedOption);
		InputControl IC = InputControl.getInstance();
		IC.insertDriverIDControl(InsertedOption);
	}
	public void insertLevel() {}
	public void contactRBC() throws MqttException, InterruptedException {
		System.out.println("Trying to open the connection to RBC...");
		MissionControl MC = MissionControl.getInstance();
		MC.contactRBCControl();
	}
	
	
	public void giveUp() {}
	public void ackTrainData() {}
	public void start() throws MqttException, InterruptedException {
		System.out.println("Write 'start' to trigger the starting procedure.");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		while(!InsertedOption.equals("start"))
		{
			System.out.println("Try again.");
			InsertedOption = sc1.nextLine();
		}
		MissionControl MC = MissionControl.getInstance();
		MC.startControl();
		ackMode();
		
	}
	public void selectMode() throws MqttException, InterruptedException {
		System.out.println("You'll now select the mode of choice. Beware that if the level selected is 2 or 3, a session with RBC must be open if you want to insert Train Data");
		InputControl IC = InputControl.getInstance();
		IC.selectModeControl();
		
		System.out.println("Select an option among the ones enlisted:");
		System.out.println("(a) - NL");
		System.out.println("(b) - SH");
		System.out.println("(c) - Insert train data");
		System.out.print("Select :");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		System.out.println(InsertedOption);
		switch(InsertedOption.charAt(0)) {
		case 'a':
			break;
		case 'b':
			break;
		case 'c':
			// Only option implemented.
			System.out.print("Insert the Train Data:");
			InsertedOption = sc1.nextLine();
			System.out.println(InsertedOption);
			// Check the train data... 
			IC.insertTrainDataControl(Integer.parseInt(InsertedOption));
			break;
		default: System.out.println("Invalid option");
		}
		start();
		
	}
	public void ackMode() throws MqttException, InterruptedException {
		System.out.println("Acknowledge the following mode:" + grantedMA + " mode");
		System.out.print("Write 'ack':");
		Scanner sc1 = new Scanner(System.in);
		String InsertedOption = sc1.nextLine();
		System.out.println(InsertedOption);
		while(!InsertedOption.equals("ack"))
		{
			System.out.println("Try again.");
			InsertedOption = sc1.nextLine();
		}
		MissionControl MC = MissionControl.getInstance();
		MC.ackControl();
		System.out.println("The system is now in "+ grantedMA + " mode.");
	}
	
	public void standByMenu() {
		System.out.println("This is the Driver Interface stand-by menu.");
		System.out.println("Select an option among the ones enlisted:");
		System.out.println("(a) - Start of Mission");
		
		System.out.println();
		System.out.println("Make your selection: ");
		Scanner sc = new Scanner(System.in);
		String InsertedOption = sc.nextLine();
		char choosenoption = InsertedOption.charAt(0);
		switch(choosenoption) {
		case 'a':
			try {
				engage();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default: System.out.println("The option doesn't exist.");
			break;
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		DisplayBoundary DB = new DisplayBoundary();
		DB.standByMenu();
	}
	public static Integer getCaseID() {
		return SOMCaseID;
	}
	public static void setCaseID(int caseID) {
		SOMCaseID = caseID;
	}
	public static int getObtainedLevel() {
		return obtainedLevel;
	}
	public static void setObtainedLevel(int obtainedLevel) {
		DisplayBoundary.obtainedLevel = obtainedLevel;
	}
	public static String getGrantedMA() {
		return grantedMA;
	}
	public static void setGrantedMA(String grantedMA) {
		DisplayBoundary.grantedMA = grantedMA;
	}
	public static Integer getSOMCaseID() {
		return SOMCaseID;
	}
	public static void setSOMCaseID(Integer sOMCaseID) {
		SOMCaseID = sOMCaseID;
	}
}
