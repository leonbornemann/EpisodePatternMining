package prediction.mining;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.model.RandomForestModel;

import data.AnnotatedEventType;
import data.Change;
import data.stream.FixedStreamWindow;
import data.stream.StreamWindow;
import episode.finance.EpisodePattern;
import episode.finance.ParallelEpisodePattern;
import episode.finance.SerialEpisodePattern;
import episode.finance.storage.EpisodeIdentifier;
import episode.finance.storage.EpisodeTrie;
import episode.lossy_counting.SerialEpisode;
import util.Pair;

public class FBSWCModel implements PredictiveModel {
	
	private static String sparkLaptopPath = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\spark-2.0.0-bin-hadoop2.7\\";
	private static JavaSparkContext jsc;
	
	private int featuresToKeep = 1000; //TODO - parameter?
	private int seed = 13;
	private List<FixedStreamWindow> upExamples;
	private List<FixedStreamWindow> downExamples;
	private List<FixedStreamWindow> neutralExamples;

	private RandomForestModel model;

	private List<EpisodePattern> bestEpisodes;

	public FBSWCModel(List<FixedStreamWindow> upExamples, List<FixedStreamWindow> downExamples, List<FixedStreamWindow> neutralExamples, Set<AnnotatedEventType> eventAlphabet, double sSerial,double sParallel){
		this.upExamples = upExamples;
		this.downExamples = downExamples;
		this.neutralExamples = neutralExamples;
		Pair<EpisodeTrie<List<Boolean>>,EpisodeTrie<List<Boolean>>> allFrequent = mineFrequentEpisodes(eventAlphabet, sSerial,sParallel);
		//feature selection via information gain:
		bestEpisodes = selectBest(allFrequent);
		//use apache spark's mlib to train random forest
		buildModel();
	}
	
	public void saveModel(File file) throws FileNotFoundException, IOException{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(bestEpisodes);
		out.writeObject(model);
		out.close();
	}
	
	/***
	 * loads the model from a file
	 * @param file
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public FBSWCModel(File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		bestEpisodes =  (List<EpisodePattern>) in.readObject();
		model = (RandomForestModel) in.readObject();
		in.close();
	}

	private void buildModel() {
		SparkConf sparkConf = new SparkConf().setAppName("JavaRandomForestClassificationExample").setMaster("local");
		if(jsc ==null){
			jsc = new JavaSparkContext(sparkConf);
		}
		JavaRDD<LabeledPoint> trainingData = buildTrainingData(jsc,bestEpisodes);
		// Train a RandomForest model.
		// Empty categoricalFeaturesInfo indicates all features are continuous.
		Integer numClasses = 3;
		//all our features are boolean, meaning categorical with 2 categories
		HashMap<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
		for(int i=0;i<bestEpisodes.size();i++){
			categoricalFeaturesInfo.put(i, 2); //TODO: let's hope they also stat counting the features at index 0!
		}
		
		//TODO: tune parameters
		Integer numTrees = 500;
		String featureSubsetStrategy = "sqrt";
		String impurity = "gini";
		Integer maxDepth = 5;
		Integer maxBins = 32; //does not matter
		
		model = RandomForest.trainClassifier(trainingData, numClasses,
				  categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins,
				  seed);
	}
	
	public Change predict(StreamWindow window){
		Vector features = buildFeatures(window,bestEpisodes);
		double prediction = model.predict(features);
		return Change.fromDouble(prediction);
	}

	/***
	 * Mines all episodes that are frequent in at least ONE of the window collections
	 * @param eventAlphabet
	 * @param s
	 * @return
	 */
	private Pair<EpisodeTrie<List<Boolean>>,EpisodeTrie<List<Boolean>>> mineFrequentEpisodes(Set<AnnotatedEventType> eventAlphabet,double sSerial,double sParallel) {
		EpisodeDiscovery discovery = new EpisodeDiscovery();
		Pair<EpisodeTrie<List<Boolean>>,EpisodeTrie<List<Boolean>>> allFrequent = discovery.mineFrequentEpisodes(upExamples, eventAlphabet, sSerial, sParallel);
		Pair<EpisodeTrie<List<Boolean>>, EpisodeTrie<List<Boolean>>> fromDown = discovery.mineFrequentEpisodes(downExamples, eventAlphabet, sSerial, sParallel);
		allFrequent.getFirst().addAllNew(fromDown.getFirst());
		allFrequent.getSecond().addAllNew(fromDown.getSecond());
		Pair<EpisodeTrie<List<Boolean>>, EpisodeTrie<List<Boolean>>> fromNeutral = discovery.mineFrequentEpisodes(neutralExamples, eventAlphabet, sSerial, sParallel);
		allFrequent.getFirst().addAllNew(fromNeutral.getFirst());
		allFrequent.getSecond().addAllNew(fromNeutral.getSecond());
		return allFrequent;
	}

	private List<EpisodePattern> selectBest(Pair<EpisodeTrie<List<Boolean>>,EpisodeTrie<List<Boolean>>> allFrequent) {
		List<Change> classAttribute = buildClassAttribute();
		PriorityQueue<Pair<EpisodePattern,Double>> bestEpisodes = new PriorityQueue<>( (p1,p2) -> p1.getSecond().compareTo(p2.getSecond()) );
		Iterator<EpisodeIdentifier<List<Boolean>>> serialIterator = allFrequent.getFirst().bfsIterator();
		while (serialIterator.hasNext()) {
			EpisodeIdentifier<List<Boolean>> id = serialIterator.next();
			EpisodePattern pattern = new SerialEpisodePattern(id.getCanonicalEpisodeRepresentation());
			insertIfBetterInfoGain(classAttribute, bestEpisodes, pattern);
		}
		Iterator<EpisodeIdentifier<List<Boolean>>> parallelIterator = allFrequent.getSecond().bfsIterator();
		while (parallelIterator.hasNext()) {
			EpisodeIdentifier<List<Boolean>> id = parallelIterator.next();
			EpisodePattern pattern = new ParallelEpisodePattern(id.getCanonicalEpisodeRepresentation());
			insertIfBetterInfoGain(classAttribute, bestEpisodes, pattern);
		}
		return bestEpisodes.stream().map(p -> p.getFirst()).collect(Collectors.toList());
	}

	private void insertIfBetterInfoGain(List<Change> classAttribute,
			PriorityQueue<Pair<EpisodePattern, Double>> bestEpisodes, EpisodePattern pattern) {
		double infoGain = calcInfoGain(classAttribute,pattern);
		if(bestEpisodes.size()!=featuresToKeep){
			bestEpisodes.add(new Pair<>(pattern,infoGain));
		} else if(bestEpisodes.peek().getSecond() < infoGain){
			bestEpisodes.poll();
			bestEpisodes.add(new Pair<>(pattern,infoGain));
		}
	}
	
	private JavaRDD<LabeledPoint> buildTrainingData(JavaSparkContext jsc,List<EpisodePattern> bestEpisodes) {
		List<LabeledPoint> points = new ArrayList<>();
		upExamples.forEach(w -> points.add(createLabeledPoint(w,bestEpisodes,Change.UP.toDouble())));
		downExamples.forEach(w -> points.add(createLabeledPoint(w,bestEpisodes,Change.DOWN.toDouble())));
		neutralExamples.forEach(w -> points.add(createLabeledPoint(w,bestEpisodes,Change.EQUAL.toDouble())));
		return jsc.parallelize(points);

	}

	private LabeledPoint createLabeledPoint(StreamWindow w, List<EpisodePattern> bestEpisodes,double classLabel) {
		Vector features = buildFeatures(w, bestEpisodes);
		return new LabeledPoint(classLabel,features);
	}

	private Vector buildFeatures(StreamWindow w, List<EpisodePattern> bestEpisodes) {
		double[] featuresArray = new double[bestEpisodes.size()];
		for(int i=0;i<bestEpisodes.size();i++){
			if(w.containsPattern(bestEpisodes.get(i))){
				featuresArray[i] = 0;
			} else{
				featuresArray[i] = 1;
			}
		}
		Vector features = Vectors.dense(featuresArray);
		return features;
	}

	private double calcInfoGain(List<Change> classAttribute, EpisodePattern pattern) {
		List<Boolean> attribute = new ArrayList<>(classAttribute.size());
		upExamples.forEach(w -> attribute.add(w.containsPattern(pattern)));
		downExamples.forEach(w -> attribute.add(w.containsPattern(pattern)));
		neutralExamples.forEach(w -> attribute.add(w.containsPattern(pattern)));
		assert(classAttribute.size()==attribute.size());
		return FeatureSelection.calcInfoGain(classAttribute, attribute);
		
	}

	private List<Change> buildClassAttribute() {
		List<Change> classAttribute = new ArrayList<>();
		upExamples.forEach(e -> classAttribute.add(Change.UP));
		downExamples.forEach(e -> classAttribute.add(Change.DOWN));
		neutralExamples.forEach(e -> classAttribute.add(Change.EQUAL));
		return classAttribute;
	}
}
