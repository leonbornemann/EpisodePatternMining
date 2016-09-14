package prediction.evaluation;

import java.io.File;

public class EvaluationFiles {

	private String companyID;
	private File predictionsFile;
	private File evaluationResultFile;
	
	public EvaluationFiles(String companyID, File predictionsFile, File evaluationResultFile) {
		super();
		this.companyID = companyID;
		this.predictionsFile = predictionsFile;
		this.evaluationResultFile = evaluationResultFile;
	}

	public String getCompanyID() {
		return companyID;
	}

	public File getPredictionsFile() {
		return predictionsFile;
	}

	public File getEvaluationResultFile() {
		return evaluationResultFile;
	}	
	
}
