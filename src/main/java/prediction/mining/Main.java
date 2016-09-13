package prediction.mining;

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
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import prediction.data.AnnotatedEvent;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.FixedStreamWindow;
import prediction.data.stream.MultiFileAnnotatedEventStream;
import prediction.data.stream.PredictorPerformance;
import prediction.data.stream.StreamWindow;
import prediction.data.stream.StreamWindowSlider;
import prediction.evaluation.AggregatedEvaluator;
import prediction.evaluation.NoAggregationEvaluator;
import prediction.evaluation.PercentageBasedInvestmentTracker;
import prediction.util.StandardDateTimeFormatter;
import semantic.SemanticKnowledgeCollector;
import util.Pair;

public class Main {

	private static File streamDirLaptop = new File("C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\");
	
	private static File streamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\");
	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\");
	
	private static File featurebasedPredictorFile = new File("resources/saved program states/featureBasedModel.object");

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		//singleStream();
		int d = 90;
		//new PredictorPerformance().printConfusionMatrix();
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
		for(AnnotatedEventType toPredict : eventAlphabet.stream().filter(e -> e.getChange()==Change.UP).collect(Collectors.toList())){
			multiStream(d,toPredict);
		}
		//NoAggregationEvaluator evaluator = new NoAggregationEvaluator();
		//evaluator.eval(d,predictionsTargetFile,lowLevelStreamDirDesktop,APPLE);
	}


	private static void multiStream(int d,AnnotatedEventType toPredict) throws IOException, ClassNotFoundException {
		System.out.println("beginning company " + toPredict.getCompanyID());
		//parameters:
		int m = 100;
		int s=75;
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);

		//setup stream:
		System.out.println(annotatedCompanyCodes.size());
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(Arrays.stream(streamDirDesktop.listFiles()).sorted().collect(Collectors.toList()),d*2,e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		System.out.println("beginning to mine windows");
		WindowMiner winMiner = new WindowMiner(stream,toPredict,m,d);
		System.out.println("done mining windows");
		printWindows(winMiner.getPredictiveWindows());
		printWindows(winMiner.getInversePredictiveWindows());
		
		//featureBasedPrediction(d, s, toPredict, eventAlphabet, stream,winMiner);
		firstNaiveStrategy(d, s, eventAlphabet, stream,toPredict, winMiner);
	}


	private static void printWindows(List<FixedStreamWindow> windows) {
		windows.stream().
			sorted((a,b) -> a.getWindowBorders().getFirst().compareTo(b.getWindowBorders().getFirst())).
			forEachOrdered(w -> System.out.println(w.getWindowBorders()));
	}

	private static void featureBasedPrediction(int d, int s, AnnotatedEventType toPredict,
			Set<AnnotatedEventType> eventAlphabet, MultiFileAnnotatedEventStream stream,WindowMiner winMiner) throws IOException, ClassNotFoundException {
		
		//find frequent Episodes
		
		//do first method predictive mining
		//feature based predictive mining:
		FeatureBasedPredictor featureBasedPredictor;
		if(featurebasedPredictorFile.exists()){
			featureBasedPredictor = new FeatureBasedPredictor(featurebasedPredictorFile);
		} else{
			featureBasedPredictor = new FeatureBasedPredictor(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNeutralWindows(), eventAlphabet, s);
			featureBasedPredictor.saveModel(featurebasedPredictorFile);
		}
		//window Sliding
		applyPredictor(d, toPredict, stream, featureBasedPredictor);
	}

	private static void applyPredictor(int d, AnnotatedEventType toPredict, MultiFileAnnotatedEventStream stream,
			PredictiveModel featureBasedPredictor) throws IOException {
		StreamWindowSlider slider = new StreamWindowSlider(stream,d);
		List<Pair<LocalDateTime,Change>> predictions = new ArrayList<>();
		List<Pair<LocalDateTime,Change>> targetMovement = new ArrayList<>();
		do{
			StreamWindow currentWindow = slider.getCurrentWindow();
			Change predicted = featureBasedPredictor.predict(currentWindow);
			LocalDateTime curTs = currentWindow.getWindowBorders().getFirst();
			predictions.add(new Pair<>(curTs,predicted));
			List<AnnotatedEvent> droppedOut = slider.slideForward();
			droppedOut.stream().filter(e -> e.getEventType().getCompanyID().equals(toPredict.getCompanyID())).forEach(e -> targetMovement.add(new Pair<>(e.getTimestamp(),e.getEventType().getChange())));
		} while(slider.canSlide());
		//serialize results
		serializePairList(predictions,buildPredictionsTargetFile(toPredict));
		serializePairList(targetMovement,buildTargetMovementFile(toPredict));
	}

	private static File buildTargetMovementFile(AnnotatedEventType toPredict) {
		File companyDir = getOrCreateCompanyDir(toPredict);
		return new File(companyDir.getAbsolutePath() + File.separator + "targetMovement.csv");
	}


	private static File buildPredictionsTargetFile(AnnotatedEventType toPredict) {
		File companyDir = getOrCreateCompanyDir(toPredict);
		return new File(companyDir.getAbsolutePath() + File.separator + "predictions.csv");
	}


	private static void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}

	private static void firstNaiveStrategy(int d, int s, Set<AnnotatedEventType> eventAlphabet,
			MultiFileAnnotatedEventStream stream, AnnotatedEventType toPredict, WindowMiner winMiner) throws IOException, ClassNotFoundException {
		int n=20;
		File predictorsFile = buildPredictorsFilePath(toPredict);
		File inversePredictorsFile = buildInversePredictorsFilePath(toPredict);
		PredictiveMiner miner = new PredictiveMiner(winMiner,eventAlphabet,s,n);
		Map<EpisodePattern, Double> predictors;
		Map<EpisodePattern, Double> inversePredictors;
		if(predictorsFile.exists()){
			predictors = loadEpisodeMap(predictorsFile);
		} else{
			predictors = miner.getInitialPreditiveEpisodes();
			serializeEpisodeMap(predictors,predictorsFile);
		}
		if(inversePredictorsFile.exists()){
			inversePredictors = loadEpisodeMap(inversePredictorsFile);
		} else{
			inversePredictors = miner.getInitialInversePreditiveEpisodes();
			serializeEpisodeMap(inversePredictors,inversePredictorsFile);
		}
		predictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + predictors.get(p) + " confidence" ));
		System.out.println("inverse");
		inversePredictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + inversePredictors.get(p) + " confidence" ));

		PredictiveEpisodeModel model = new PredictiveEpisodeModel(predictors,inversePredictors);
		applyPredictor(d,toPredict,stream,model);
	}

	private static File buildInversePredictorsFilePath(AnnotatedEventType toPredict) {
		File companyDir = getOrCreateCompanyDir(toPredict);
		File programStateDir = getOrCreateProgramStateDir(companyDir);
		return new File(programStateDir.getAbsolutePath() + File.separator + "inversePredictors.map");
	}


	private static File buildPredictorsFilePath(AnnotatedEventType toPredict) {
		File companyDir = getOrCreateCompanyDir(toPredict);
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


	private static File getOrCreateCompanyDir(AnnotatedEventType toPredict) {
		String basePath = "resources/results/";
		File companyDir = new File(basePath + toPredict.getCompanyID() + "/");
		if(!companyDir.exists()){
			companyDir.mkdirs();
		}
		return companyDir;
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


	/*private static void singleStream() throws IOException {
		File testDay = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\NASDAQ_2016-05-09_annotated.csv");
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		AnnotatedEventStream stream = AnnotatedEventStream.read(testDay).filter(e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		PredictiveMiner miner = new PredictiveMiner(stream,new AnnotatedEventType(APPLE, Change.UP),AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes),100,15,10,50);
		Map<SerialEpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		predictors.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
	}*/

}
