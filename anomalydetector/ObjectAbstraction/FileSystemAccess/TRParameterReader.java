package ObjectAbstraction.FileSystemAccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import ObjectAbstraction.PMLogic.ProcessModel;
import ObjectAbstraction.PMLogic.TRParameter;

public class TRParameterReader {
	
	private String Path;
	private String ProcessName;
	
	public TRParameterReader(String P, String PName) {
		Path = P;
		ProcessName = PName;
	}


	public ArrayList<TRParameter> getTRParameterList(ProcessModel PM) throws FileNotFoundException {
		
		ArrayList<TRParameter> TRPList = new ArrayList<TRParameter>();
		
		File TRParameterFile = new File(Path + "/parameters_"+ProcessName);
		Scanner Reader = new Scanner(TRParameterFile);
		
		while(Reader.hasNextLine()) {
			String TRParameterString = Reader.nextLine();
			String [] TRParameterSplitString = TRParameterString.split("\\:");
			TRPList.add(new TRParameter(TRParameterSplitString[0], Float.parseFloat(TRParameterSplitString[1]),PM));
		}
		
		Reader.close();
		
		
		return TRPList;
		
		
	}

}
