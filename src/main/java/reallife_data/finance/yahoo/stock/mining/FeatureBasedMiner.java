package reallife_data.finance.yahoo.stock.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.model.RandomForestModel;

import episode.finance.EpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;
import util.Pair;

public class FeatureBasedMiner {
	
	private static String sparkLaptopPath = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\spark-2.0.0-bin-hadoop2.7\\";

	private int featuresToKeep = 1000; //TODO - parameter?
	private int seed = 13;
	private List<StreamWindow> positiveExamples;
	private List<StreamWindow> negativeExamples;
	private List<StreamWindow> neutralExamples;

	public FeatureBasedMiner(List<StreamWindow> positiveExamples, List<StreamWindow> negativeExamples, List<StreamWindow> neutralExamples, Set<AnnotatedEventType> eventAlphabet, int s){
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
		this.neutralExamples = neutralExamples;
		HashSet<EpisodePattern> allFrequent = mineFrequentEpisodes(eventAlphabet, s);
		//feature selection via information gain:
		List<EpisodePattern> bestEpisodes = selectBest(allFrequent);
		//use apache spark's mlib to train random forest
		
		SparkConf sparkConf = new SparkConf().setAppName("JavaRandomForestClassificationExample").setMaster("local");
		JavaSparkContext jsc = new JavaSparkContext(sparkConf);
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
		
		final RandomForestModel model = RandomForest.trainClassifier(trainingData, numClasses,
				  categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins,
				  seed);
		/*
		List<LabeledPoint> test = testData.collect();
		double correct = 0;
		double incorrect = 0;
		for(int i=0;i<test.size();i++){
			double predicted = model.predict(test.get(i).features());
			if(predicted==test.get(i).label()){
				System.out.println("zing");
				correct++;
			}else{
				System.out.println("Möp");
				incorrect++;
			}
		}
		System.out.println(correct / (incorrect+correct) );*/
	}

	/***
	 * Mines all episodes that are frequent in at least ONE of the window collections
	 * @param eventAlphabet
	 * @param s
	 * @return
	 */
	private HashSet<EpisodePattern> mineFrequentEpisodes(Set<AnnotatedEventType> eventAlphabet, int s) {
		EpisodeDiscovery discovery = new EpisodeDiscovery();
		HashSet<EpisodePattern> allFrequent = new HashSet<>();
		allFrequent.addAll(discovery.mineFrequentEpisodes(positiveExamples, eventAlphabet, s).keySet());
		allFrequent.addAll(discovery.mineFrequentEpisodes(negativeExamples, eventAlphabet, s).keySet());
		allFrequent.addAll(discovery.mineFrequentEpisodes(neutralExamples, eventAlphabet, s).keySet());
		return allFrequent;
	}

	private List<EpisodePattern> selectBest(HashSet<EpisodePattern> allFrequent) {
		List<Change> classAttribute = buildClassAttribute();
		PriorityQueue<Pair<EpisodePattern,Double>> bestEpisodes = new PriorityQueue<>( (p1,p2) -> p1.getSecond().compareTo(p2.getSecond()) );
		for(EpisodePattern pattern : allFrequent){
			double infoGain = calcInfoGain(classAttribute,pattern);
			if(bestEpisodes.size()!=featuresToKeep){
				bestEpisodes.add(new Pair<>(pattern,infoGain));
			} else if(bestEpisodes.peek().getSecond() < infoGain){
				bestEpisodes.poll();
				bestEpisodes.add(new Pair<>(pattern,infoGain));
			}
		}
		return bestEpisodes.stream().map(p -> p.getFirst()).collect(Collectors.toList());
	}
	
	private JavaRDD<LabeledPoint> buildTrainingData(JavaSparkContext jsc,List<EpisodePattern> bestEpisodes) {
		List<LabeledPoint> points = new ArrayList<>();
		positiveExamples.forEach(w -> points.add(createLabeledPoint(w,bestEpisodes,0)));
		negativeExamples.forEach(w -> points.add(createLabeledPoint(w,bestEpisodes,1)));
		neutralExamples.forEach(w -> points.add(createLabeledPoint(w,bestEpisodes,2)));
		return jsc.parallelize(points);

	}

	private LabeledPoint createLabeledPoint(StreamWindow w, List<EpisodePattern> bestEpisodes,double classLabel) {
		double[] features = new double[bestEpisodes.size()];
		for(int i=0;i<bestEpisodes.size();i++){
			if(w.containsPattern(bestEpisodes.get(i))){
				features[i] = 0;
			} else{
				features[i] = 1;
			}
		}
		return new LabeledPoint(classLabel,Vectors.dense(features));
	}

	private double calcInfoGain(List<Change> classAttribute, EpisodePattern pattern) {
		List<Boolean> attribute = new ArrayList<>(classAttribute.size());
		positiveExamples.forEach(w -> attribute.add(w.containsPattern(pattern)));
		negativeExamples.forEach(w -> attribute.add(w.containsPattern(pattern)));
		neutralExamples.forEach(w -> attribute.add(w.containsPattern(pattern)));
		assert(classAttribute.size()==attribute.size());
		return FeatureSelection.calcInfoGain(classAttribute, attribute);
		
	}

	private List<Change> buildClassAttribute() {
		List<Change> classAttribute = new ArrayList<>();
		positiveExamples.forEach(e -> classAttribute.add(Change.UP));
		negativeExamples.forEach(e -> classAttribute.add(Change.DOWN));
		neutralExamples.forEach(e -> classAttribute.add(Change.EQUAL));
		return classAttribute;
	}
}
