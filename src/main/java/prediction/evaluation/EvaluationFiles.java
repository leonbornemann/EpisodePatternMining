package prediction.evaluation;

import java.io.File;

public class EvaluationFiles {

	private String companyID;
	private File predictionsFile;
	private File evaluationResultFile;
	private File timeFile;
	
	public EvaluationFiles(String companyID, File predictionsFile, File timeFile, File evaluationResultFile) {
		super();
		this.companyID = companyID;
		this.predictionsFile = predictionsFile;
		this.timeFile = timeFile;
		this.evaluationResultFile = evaluationResultFile;
	}

	public String getCompanyID() {
		return companyID;
	}

	public File getPredictionsFile() {
		return predictionsFile;
	}
	
	public File getTimeFile() {
		return timeFile;
	}

	public File getEvaluationResultFile() {
		return evaluationResultFile;
	}	
	
}
