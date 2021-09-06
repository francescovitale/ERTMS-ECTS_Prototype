package ad;


import java.sql.SQLException;
import java.util.ArrayList;

import ObjectAbstraction.Logger;
import ObjectAbstraction.PMLogic.Trace;

public class AnomalyDetectorController {
	
	private volatile static AnomalyDetectorController ADC = null;

	private AnomalyDetectorController() {}
	
	public static AnomalyDetectorController getInstance() {
		if(ADC == null) {
			synchronized(AnomalyDetectorController.class) {
				if(ADC == null) {
					ADC = new AnomalyDetectorController();
				}
			}
		}
		return ADC;
	}
	
	public void initializeClient()  {
		
	}
	public void logEvent(Message EventToLog, String ProcessName, String Path) {
		Logger L = new Logger();
		try {
			L.logEvent(EventToLog.getTimestamp(), EventToLog.getCaseID(), EventToLog.getResource(),EventToLog.getActivity(),EventToLog.isAware(), ProcessName, Path);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	
	}
	public String getAnomalousTraces() {
		Logger L = new Logger();
		String Traces = "";
		try {
			ArrayList<Trace> AT = L.getAnomalousTraces();
			for(int i=0; i<AT.size(); i++)
			{
				for(int j=0;j<AT.get(i).getAI().size();j++) {
					System.out.print(AT.get(i).getAI().get(j).getA().getName() + " ");
				}
				System.out.println();
			}
			System.out.println(AT.size());
			System.out.println(AT.get(1).getAI().size());
			for(int i = 0; i<AT.size(); i++) {
				for(int j=0; j<AT.get(i).getAI().size(); j++) {
					
					if(j==0) {
						Traces = Traces + "<" + AT.get(i).getAI().get(j).getA().getName() + ",";
					}
					if(j > 0 && j < AT.get(i).getAI().size()-1) {
						Traces = Traces + AT.get(i).getAI().get(j).getA().getName() + ",";
					}
					if(j == AT.get(i).getAI().size()-1) {
						Traces = Traces + AT.get(i).getAI().get(j).getA().getName() + ">:";
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Traces;
	}
	
	
	public static void main(String[] args) throws Exception {
		AnomalyDetectorController ADC = new AnomalyDetectorController();
		ADC.initializeClient();
		ADC.getAnomalousTraces();
		
		
	}
}
