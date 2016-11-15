package experiment;

import java.io.File;
import java.io.IOException;

import prediction.mining.Method;

public class ConsistencyCheck {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Toy stuff\\Run 1");
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.75, 0.75, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
		ConsistencyChecker checker = new ConsistencyChecker(resultDir);
		checker.check();
	}
	
	
}
