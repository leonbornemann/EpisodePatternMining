package prediction.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import prediction.data.AnnotatedEventType;

public class IOService {

	
	public static Set<String> getAllCompanyCodes() throws IOException {
		//String companyListPath = "resources" + File.separator + "stock_data" + File.separator + "companyInfo" + File.separator + "companyList.csv";
		String companyListPath = "resources/stock_data/companyInfo/companylist.csv";
		BufferedReader br = new BufferedReader(new FileReader(new File(companyListPath)));
		br.readLine();
		String line = br.readLine();
		Set<String> allCompanyCodes = new HashSet<>();
		while(line!=null){
			String curCompanyCode = line.split(",")[0].replaceAll("\"", "").trim();
			assert(!allCompanyCodes.contains(curCompanyCode));
			allCompanyCodes.add(curCompanyCode);
			line = br.readLine();
		}
		br.close();
		return allCompanyCodes;
	}

	public static void writeErrorLogEntry(String errorLogLocation, Throwable e, LocalDateTime timestamp) {
		PrintStream stream;
		try {
			stream = new PrintStream(new FileOutputStream(new File(errorLogLocation),true));
			stream.println("-----------------------------");
			stream.println(timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter()));
			e.printStackTrace(stream);
			stream.println("-----------------------------");
			stream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("error logging broken");
			throw new AssertionError("error logging broken");
		}
	}
	
	public static void writeLogEntry(String outLogLocation,String message){
		PrintStream stream;
		try {
			stream = new PrintStream(new FileOutputStream(new File(outLogLocation),true));
			stream.println(message);
			stream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("logging broken");
		}
	}
	

	public static Map<EpisodePattern, Double> loadEpisodeMap(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in  = new ObjectInputStream(new FileInputStream(file));
		@SuppressWarnings("unchecked")
		Map<EpisodePattern, Double> episodeMap = (Map<EpisodePattern, Double>) in.readObject();
		in.close();
		return episodeMap;
	}

	public static void serializeEpisodeMap(Map<EpisodePattern, Double> predictors, File file) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(predictors);
		out.close();
	}	
	
	public static File buildInversePredictorsFilePath(String companyId) {
		File companyDir = getOrCreateCompanyDir(companyId);
		File programStateDir = getOrCreateProgramStateDir(companyDir);
		return new File(programStateDir.getAbsolutePath() + File.separator + "inversePredictors.map");
	}


	public static File buildPredictorsFilePath(String companyId) {
		File companyDir = getOrCreateCompanyDir(companyId);
		File programStateDir = getOrCreateProgramStateDir(companyDir);
		return new File(programStateDir.getAbsolutePath() + File.separator + "predictors.map");
	}


	private static File getOrCreateProgramStateDir(File companyDir) {
		String basePath = companyDir.getAbsolutePath();
		File programStateDir = new File(basePath + File.separator +"program state" + File.separator );
		if(!programStateDir.exists()){
			programStateDir.mkdirs();
		}
		return programStateDir;
	}


	private static File getOrCreateCompanyDir(String companyId) {
		String basePath = "resources/results/";
		File companyDir = new File(basePath + companyId + "/");
		if(!companyDir.exists()){
			companyDir.mkdirs();
		}
		return companyDir;
	}
	
	public static File getEvaluationResultFile(String companyId) {
		File comp = getOrCreateCompanyDir(companyId);
		File programState = getOrCreateProgramStateDir(comp);
		return new File(programState.getAbsolutePath() + File.separator + "evaluationResult.obj");
	}
	

	public static File buildTargetMovementFile(String companyId) {
		File companyDir = getOrCreateCompanyDir(companyId);
		return new File(companyDir.getAbsolutePath() + File.separator + "targetMovement.csv");
	}


	public static File buildPredictionsTargetFile(String companyId) {
		File companyDir = getOrCreateCompanyDir(companyId);
		return new File(companyDir.getAbsolutePath() + File.separator + "predictions.csv");
	}

}
