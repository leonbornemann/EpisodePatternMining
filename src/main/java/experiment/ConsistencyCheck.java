package experiment;

import java.io.File;
import java.io.IOException;

import prediction.mining.Method;

public class ConsistencyCheck {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Normal Runs\\Run 1");
		ConsistencyChecker checker = new ConsistencyChecker(resultDir);
		checker.check();
	}
	
	
}
