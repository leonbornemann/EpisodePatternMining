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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import prediction.data.AnnotatedEvent;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.InvestmentTracker;
import prediction.data.stream.MultiFileAnnotatedEventStream;
import prediction.data.stream.PredictorPerformance;
import prediction.data.stream.StreamMonitor;
import prediction.data.stream.StreamWindow;
import prediction.data.stream.StreamWindowSlider;
import prediction.util.StandardDateTimeFormatter;
import semantic.SemanticKnowledgeCollector;
import util.Pair;

public class Main {

	private static final String APPLE = "AAPL"; //TODO: make these enums?
	private static File predictionsTargetFile = new File("resources/results/FeatureBased/predictions.csv");
	private static File targetMovementTargetFile = new File("resources/results/FeatureBased/targetMovement.csv");
	
	private static File streamDirLaptop = new File("C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\");
	
	private static File streamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\");
	
	private static File featurebasedPredictorFile = new File("resources/saved program states/featureBasedModel.object");
	public static final File predictorsFile = new File("resources/saved program states/predictors.map");
	public static final File inversePredictorsFile = new File("resources/saved program states/inversePredictors.map");


	public static void main(String[] args) throws IOException, ClassNotFoundException {
		//singleStream();
		multiStream();
		evalFeatureBased();
	}

	private static void evalFeatureBased() throws IOException {
		List<Pair<LocalDateTime, Change>> predictions = deserializePairList(predictionsTargetFile);
		List<Pair<LocalDateTime, Change>> targetMovement = deserializePairList(targetMovementTargetFile);
		InvestmentTracker tracker = new InvestmentTracker(0.001);
		int predIndex = 0;
		int targetMovementIndex = 0;
		System.out.println("starting price: " + tracker.netWorth());
		while(true){
			if(predIndex==predictions.size() && targetMovementIndex == targetMovement.size()){
				break;
			} else if(predIndex==predictions.size()){
				Pair<LocalDateTime, Change> targetMovementEventPair = targetMovement.get(targetMovementIndex);
				processTargetMovement(tracker, targetMovementEventPair);
				targetMovementIndex++;
			} else if(targetMovementIndex == targetMovement.size()){
				Pair<LocalDateTime, Change> predEventPair = predictions.get(predIndex);
				processPredictionEvent(tracker, predEventPair);
				predIndex++;
			} else{
				LocalDateTime predElement = predictions.get(predIndex).getFirst();
				LocalDateTime targetElement = targetMovement.get(targetMovementIndex).getFirst();
				if(predElement.compareTo(targetElement)<0){
					//process predElement
					Pair<LocalDateTime, Change> predEventPair = predictions.get(predIndex);
					processPredictionEvent(tracker, predEventPair);
					predIndex++;
				} else{
					//process targetElement
					Pair<LocalDateTime, Change> targetMovementEventPair = targetMovement.get(targetMovementIndex);
					processTargetMovement(tracker, targetMovementEventPair);
					targetMovementIndex++;
				}
			}
		}
		long upCount = targetMovement.stream().map(e -> e.getSecond()).filter(e -> e==Change.UP).count();
		long downCount = targetMovement.stream().map(e -> e.getSecond()).filter(e -> e==Change.DOWN).count();
		System.out.println("Num upwards movements: " + upCount);
		System.out.println("Num downwards movements: " + downCount);
		System.out.println("Ending price: " + tracker.netWorth());
		System.out.println("rate of return: " + tracker.rateOfReturn());
	}

	private static void processPredictionEvent(InvestmentTracker tracker, Pair<LocalDateTime, Change> predEventPair) {
		if(predEventPair.getSecond()==Change.UP){
			tracker.buyIfPossible();
		} else if(predEventPair.getSecond()==Change.DOWN){
			tracker.sellIfPossible();
		} else{
			//hold
		}
	}

	private static void processTargetMovement(InvestmentTracker tracker,
			Pair<LocalDateTime, Change> targetMovementEventPair) {
		if(targetMovementEventPair.getSecond()==Change.UP){
			tracker.up();
		} else{
			assert(targetMovementEventPair.getSecond()==Change.DOWN);
			tracker.down();
		}
	}

	private static void multiStream() throws IOException, ClassNotFoundException {
		//parameters:
		int d = 180;
		int m = 100;
		int s=15;
		AnnotatedEventType toPredict = new AnnotatedEventType(APPLE, Change.UP);
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);

		//setup stream:
		System.out.println(annotatedCompanyCodes.size());
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(Arrays.stream(streamDirLaptop.listFiles()).sorted().collect(Collectors.toList()),d*2,e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		System.out.println("beginning to mine windows");
		WindowMiner winMiner = new WindowMiner(stream,toPredict,m,d);
		System.out.println("done mining windows");
		//featureBasedPrediction(d, s, toPredict, eventAlphabet, stream,winMiner);
		firstNaiveStrategy(d, s, eventAlphabet, stream,toPredict, winMiner);
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
		serializePairList(predictions,predictionsTargetFile);
		serializePairList(targetMovement,targetMovementTargetFile);
	}

	private static void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}
	
	private static List<Pair<LocalDateTime, Change>> deserializePairList(File file) throws IOException{
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

	private static void firstNaiveStrategy(int d, int s, Set<AnnotatedEventType> eventAlphabet,
			MultiFileAnnotatedEventStream stream, AnnotatedEventType toPredict, WindowMiner winMiner) throws IOException, ClassNotFoundException {
		int n=20;
		PredictiveMiner miner = new PredictiveMiner(winMiner,eventAlphabet,s,n);
		Map<EpisodePattern, Integer> predictors;
		Map<EpisodePattern, Integer> inversePredictors;
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
		PredictiveEpisodeModel model = new PredictiveEpisodeModel(predictors,inversePredictors);
		applyPredictor(d,toPredict,stream,model);
		
		/*StreamMonitor monitor = new StreamMonitor(predictors,inversePredictors, stream, toPredict, d,new File("resources/logs/performanceLog.txt"));
		System.out.println(monitor.getInvestmentTracker().netWorth());
		System.out.println(monitor.getInvestmentTracker().getPrice());
		monitor.monitor();
		Map<EpisodePattern, PredictorPerformance> trustScores = monitor.getCurrentTrustScores();
		printTrustScores(trustScores);
		System.out.println(monitor.getInvestmentTracker().netWorth());
		System.out.println(monitor.getInvestmentTracker().getPrice());*/
	}

	public static Map<EpisodePattern, Integer> loadEpisodeMap(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in  = new ObjectInputStream(new FileInputStream(file));
		@SuppressWarnings("unchecked")
		Map<EpisodePattern, Integer> episodeMap = (Map<EpisodePattern, Integer>) in.readObject();
		in.close();
		return episodeMap;
	}

	public static void serializeEpisodeMap(Map<EpisodePattern, Integer> predictors, File file) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(predictors);
		out.close();
	}	

	private static void printTrustScores(Map<EpisodePattern, PredictorPerformance> trustScores) {
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Precision: "+v.getPrecision() + " and Recall: " + v.getRecall() + " and accuracy: " + v.getAccuracy()));
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
