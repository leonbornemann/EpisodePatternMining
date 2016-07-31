package spark;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.apache.spark.sql.DataFrame;
import org.junit.Test;

import scala.Tuple2;

public class SparkExperiment {

	private static Random random = new Random(13);
	
	@Test
	public void test() {
		
		JavaSparkContext jsc = new JavaSparkContext("local", "Simple App",
			      "C:/Users/LeonBornemann/Documents/Uni/Master thesis/spark-1.6.2", new String[]{"target/simple-project-1.0.jar"});
		//SparkConf conf = new SparkConf().setMaster("local").setAppName("My App");
	    //JavaSparkContext jsc = new JavaSparkContext(conf);
		
		//SparkConf conf = new SparkConf().setAppName("JavaRandomForestClassificationExample").setMaster("local");
		//JavaSparkContext jsc = new JavaSparkContext(conf);
		JavaRDD<LabeledPoint> data = buildData(jsc);
		
		// Split the data into training and test sets (30% held out for testing)
		JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[]{0.7, 0.3});
		JavaRDD<LabeledPoint> trainingData = splits[0];
		JavaRDD<LabeledPoint> testData = splits[1];

		// Train a RandomForest model.
		// Empty categoricalFeaturesInfo indicates all features are continuous.
		Integer numClasses = 2;
		HashMap<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
		Integer numTrees = 3; // Use more in practice.
		String featureSubsetStrategy = "auto"; // Let the algorithm choose.
		String impurity = "gini";
		Integer maxDepth = 5;
		Integer maxBins = 32;
		Integer seed = 12345;
		
		final RandomForestModel model = RandomForest.trainClassifier(trainingData, numClasses,
				  categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins,
				  seed);
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
		System.out.println(correct / (incorrect+correct) );
			
			
		/*	
		// Evaluate model on test instances and compute test error
		JavaPairRDD<Double, Double> predictionAndLabel =
		  testData.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {

			@Override
		    public Tuple2<Double, Double> call(LabeledPoint p) {
		      return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
		    }
		  });
		Double testErr =
		  1.0 * predictionAndLabel.filter(new Function<Tuple2<Double, Double>, Boolean>() {

			private static final long serialVersionUID = 1L;

			@Override
		    public Boolean call(Tuple2<Double, Double> pl) {
		      return !pl._1().equals(pl._2());
		    }
		  }).count() / testData.count();
		System.out.println("Test Error: " + testErr);
		System.out.println("Learned classification forest model:\n" + model.toDebugString());*/

	}

	private JavaRDD<LabeledPoint> buildData(JavaSparkContext jsc) {
		List<LabeledPoint> points = new ArrayList<>();
		for(int i=0;i<100;i++){
			points.add(createLabeledPoint());
		}
		return jsc.parallelize(points);
	}

	private LabeledPoint createLabeledPoint() {
		return new LabeledPoint(random.nextInt(2), buildFeatures());
	}

	private Vector buildFeatures() {
		Vector vec = Vectors.dense(random.nextInt(2),random.nextInt(2),random.nextInt(2),random.nextInt(2)); //new java.util.Vector<Boolean>(Arrays.asList(true,false,true,false));
		return vec;
	}

}
