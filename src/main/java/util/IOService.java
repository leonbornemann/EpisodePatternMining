package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import data.events.Change;
import episode.pattern.EpisodePattern;
import prediction.models.Method;

/***
 * Class for diverse IO-operations and (relative) file locations 
 * @author Leon Bornemann
 *
 */
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
	
	public static File buildInversePredictorsFilePath(String companyId, File parentDir) {
		File companyDir = getOrCreateCompanyDir(companyId,parentDir);
		File programStateDir = getOrCreateProgramStateDir(companyDir);
		return new File(programStateDir.getAbsolutePath() + File.separator + "inversePredictors.map");
	}


	public static File buildPredictorsFilePath(String companyId, File parentDir) {
		File companyDir = getOrCreateCompanyDir(companyId,parentDir);
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


	private static File getOrCreateCompanyDir(String companyId, File parentDir) {
		String parentDirString = parentDir.getAbsolutePath();
		if(parentDirString.charAt(parentDirString.length()-1)!='/'){
			parentDirString = parentDirString + "/";
		}
		String basePath = parentDirString + "resources/results/";
		File companyDir = new File(basePath + companyId + "/");
		if(!companyDir.exists()){
			companyDir.mkdirs();
		}
		return companyDir;
	}
	
	public static File getEvaluationResultFile(String companyId, Method method, File parentDir) {
		File comp = getOrCreateCompanyDir(companyId,parentDir);
		File programState = getOrCreateProgramStateDir(comp);
		if(method==Method.PERMS){
			return new File(programState.getAbsolutePath() + File.separator + "evaluationResult.obj");
		} else if ( method == Method.FBSWC){
			return new File(programState.getAbsolutePath() + File.separator + Method.FBSWC + "_evaluationResult.obj");
		} else{
			return new File(programState.getAbsolutePath() + File.separator + Method.RandomGuessing + "_evaluationResult.obj");
		}
	}
	

	public static File buildTargetMovementFile(String companyId, Method method, File parentDir) {
		File companyDir = getOrCreateCompanyDir(companyId,parentDir);
		if(method==Method.PERMS){
			return new File(companyDir.getAbsolutePath() + File.separator + "targetMovement.csv");
		} else if ( method == Method.FBSWC){
			return new File(companyDir.getAbsolutePath() + File.separator + Method.FBSWC + "_targetMovement.csv");
		} else{
			return new File(companyDir.getAbsolutePath() + File.separator + Method.RandomGuessing + "_targetMovement.csv");
		}
	}


	public static File buildPredictionsTargetFile(String companyId, Method method, File parentDir) {
		File companyDir = getOrCreateCompanyDir(companyId,parentDir);
		if(method==Method.PERMS){
			return new File(companyDir.getAbsolutePath() + File.separator + "predictions.csv");
		} else if ( method == Method.FBSWC){
			return new File(companyDir.getAbsolutePath() + File.separator + Method.FBSWC + "_predictions.csv");
		} else{
			return new File(companyDir.getAbsolutePath() + File.separator + Method.RandomGuessing + "_predictions.csv");
		}
	}

	public static File getFeatureBasedPredictorFile(String companyID, File parentDir) {
		File companyDir = getOrCreateCompanyDir(companyID,parentDir);
		File programStateDir = getOrCreateProgramStateDir(companyDir);
		return new File(programStateDir.getAbsolutePath() + File.separator + "featureBasedPredictor.object");
	}

	public static File getCSVResultFile(String companyID, Method method, File parentDir) {
		File companyDir = getOrCreateCompanyDir(companyID,parentDir);
		return new File(companyDir + File.separator +"resultsAsCSV_" + method + ".csv");
	}

	public static File getTotalResultByDayCsvFile(Method method, File parentDir) {
		String parentDirString = parentDir.getAbsolutePath();
		if(parentDirString.charAt(parentDirString.length()-1)!='/'){
			parentDirString = parentDirString + "/";
		}
		new File(parentDirString + "resources/AveragedResults/").mkdir();
		return new File(parentDirString + "resources/AveragedResults/"+method+".csv");
	}

	public static File getTotalResultByCompanyCsvFile(Method method, File parentDir) {
		String parentDirString = parentDir.getAbsolutePath();
		if(parentDirString.charAt(parentDirString.length()-1)!='/'){
			parentDirString = parentDirString + "/";
		}
		return new File(parentDirString + "resources/AveragedResults/"+method+"_byCompany.csv");
	}

	public static Map<String, Set<String>> getSectorInfo(Set<String> codes) throws IOException {
		String companyListPath = "resources/stock_data/companyInfo/companylist.csv";
		BufferedReader br = new BufferedReader(new FileReader(new File(companyListPath)));
		br.readLine();
		String line = br.readLine();
		Map<String,Set<String>> bySector = new HashMap<>();
		int sectorIndex = 5;
		while(line!=null){
			String[] lineTokens = line.split("\",\"");
			String curCompanyCode = lineTokens[0].replaceAll("\"", "").trim();
			if(codes == null || codes.contains(curCompanyCode)){
				String sector = lineTokens[sectorIndex].replaceAll("\"", "");
				if(bySector.containsKey(sector)) {
					bySector.get(sector).add(curCompanyCode);
				} else{
					bySector.put(sector, new HashSet<>(Arrays.asList(curCompanyCode)));
				}
			}
			line = br.readLine();
		}
		br.close();
		// remove those that don't have a sector
		bySector.remove("n/a");
		return bySector;
	}

	public static Map<String, Set<String>> getCodeBySector() throws IOException {
		return getSectorInfo(null);
	}

	public static List<Pair<LocalDateTime, BigDecimal>> readTimeSeriesData(File source) throws IOException {
		System.out.println("beginning file "+source.getName());
		BufferedReader br = new BufferedReader(new FileReader(source));
		try{
			List<Pair<LocalDateTime, BigDecimal>> events = new ArrayList<>();
			br.readLine();
			String line = br.readLine();
			int lineCount = 2;
			while(line!=null && !line.equals("")){
				String[] tokens = line.split(",");
				if(tokens.length!=2){
					System.out.println(line);
					System.out.println(lineCount);
					assert(false);
				}
				Pair<LocalDateTime, BigDecimal> curPair = new Pair<>(LocalDateTime.parse(tokens[0], StandardDateTimeFormatter.getStandardDateTimeFormatter()),new BigDecimal(tokens[1]));
				events.add(curPair);
				lineCount++;
				line = br.readLine();
			}
			return events.stream().sorted((p1,p2) -> p1.getFirst().compareTo(p2.getFirst())).collect(Collectors.toList());
		} finally{
			br.close();	
		}
	}

	public static File buildTimeTargetFile(String companyID, Method method, File parentDir) {
		File companyDir = getOrCreateCompanyDir(companyID,parentDir);
		if(method==Method.PERMS){
			return new File(companyDir.getAbsolutePath() + File.separator + "runtimePerformance.csv");
		} else if(method == Method.FBSWC){
			return new File(companyDir.getAbsolutePath() + File.separator + Method.FBSWC + "runtimePerformance.csv");
		} else{
			return new File(companyDir.getAbsolutePath() + File.separator + Method.RandomGuessing + "runtimePerformance.csv");
		}
	}

	public static void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}

	public static List<Pair<LocalDateTime, Change>> deserializePairList(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		List<Pair<LocalDateTime, Change>> list = new ArrayList<>();
		while(line!=null){
			String[] tokens = line.split(",");
			assert(tokens.length==2);
			list.add(new Pair<LocalDateTime, Change>(LocalDateTime.parse(tokens[0], StandardDateTimeFormatter.getStandardDateTimeFormatter()),Change.valueOf(tokens[1])));
			line = reader.readLine();
		}
		reader.close();
		return list;
	}

	public static TreeMap<LocalDateTime,BigDecimal> readTimeSeries(String cmpId, File timeSeriesDirectory) throws IOException {
		List<File> matchingFiles = Arrays.asList(timeSeriesDirectory.listFiles()).stream().filter(f -> f.getName().equals(cmpId +".csv")).collect(Collectors.toList());
		assert(matchingFiles.size()==1);
		TreeMap<LocalDateTime,BigDecimal> timeSeries = new TreeMap<>();
		List<Pair<LocalDateTime, BigDecimal>> asList = readTimeSeriesData(matchingFiles.get(0));
		asList.stream().forEach(p -> timeSeries.put(p.getFirst(), p.getSecond()));
		return timeSeries;
	}
	
	public static TreeMap<LocalDateTime,BigDecimal> readTimeSeries(File file) throws IOException {
		TreeMap<LocalDateTime,BigDecimal> timeSeries = new TreeMap<>();
		List<Pair<LocalDateTime, BigDecimal>> asList = readTimeSeriesData(file);
		asList.stream().forEach(p -> timeSeries.put(p.getFirst(), p.getSecond()));
		return timeSeries;
	}
}
