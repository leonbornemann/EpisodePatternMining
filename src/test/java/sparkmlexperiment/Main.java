package sparkmlexperiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.mllib.linalg.VectorUDT;
import org.apache.spark.mllib.regression.LabeledPoint;
// Import factory methods provided by DataTypes.
import org.apache.spark.sql.types.DataTypes;
// Import StructType and StructField
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.DataFrame;
// Import Row.
import org.apache.spark.sql.Row;
// Import RowFactory.
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;

public class Main {

	public static Random random = new Random(13);
	
	public static void main(String[] args) {
		JavaSparkContext sc = new JavaSparkContext("local", "Simple App",
			      "C:/Users/LeonBornemann/Documents/Uni/Master thesis/spark-1.6.2", new String[]{"target/simple-project-1.0.jar"});
		SQLContext sqlContext = new org.apache.spark.sql.SQLContext(sc);

		int n=1000;
		// Load a text file and convert each line to a JavaBean.
		JavaRDD<WindowInfo> windows = createWindowInfos(n,sc);

		// Generate the schema based on the string of schema
		StructType schema = createSchema(n);
		
		
		
		
		
		
		// Convert records of the RDD (people) to Rows.
		JavaRDD<Row> rowRDD = windows.map(
		  new Function<WindowInfo, Row>() {
		    public Row call(WindowInfo window) throws Exception {
		      return RowFactory.create(window.getClassLabel(),window.getFeatureArray());
		    }
		  });

		// Apply the schema to the RDD.
		DataFrame data = sqlContext.createDataFrame(rowRDD, schema);
		System.out.println(data);
		
				// Split the data into training and test sets (30% held out for testing)
				DataFrame[] splits = data.randomSplit(new double[] {0.7, 0.3});
				DataFrame trainingData = splits[0];
				DataFrame testData = splits[1];

				// Train a RandomForest model.
				RandomForestClassifier rf = new RandomForestClassifier();
				  //.setLabelCol("classLabel");
				StringIndexerModel labelIndexer = new StringIndexer()
						  .setInputCol("classLabel")
						  .setOutputCol("indexedLabel")
						  .fit(data);
				/*VectorIndexerModel featureIndexer = new VectorIndexer()
						  .setInputCol("features")
						  .setOutputCol("indexedFeatures")
						  .setMaxCategories(4)
						  .fit(data);*/

				// Chain indexers and forest in a Pipeline
				Pipeline pipeline = new Pipeline()
				  .setStages(new PipelineStage[] {labelIndexer,rf});

				// Train model. This also runs the indexers.
				PipelineModel model = pipeline.fit(trainingData);

				// Make predictions.
				DataFrame predictions = model.transform(testData);
				System.out.println(predictions);
				// Select example rows to display.
				/*predictions.select("predictedLabel", "label", "features").show(5);

				// Select (prediction, true label) and compute test error
				MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
				  .setLabelCol("classLabel")
				  .setPredictionCol("prediction")
				  .setMetricName("precision");
				double accuracy = evaluator.evaluate(predictions);
				System.out.println("Test Error = " + (1.0 - accuracy));*/
	}

	private static StructType createSchema(int n) {
		List<StructField> fields = new ArrayList<StructField>();
		fields.add(DataTypes.createStructField("classLabel", DataTypes.StringType, false));
		for (int i=0;i<n;i++) {
		  fields.add(DataTypes.createStructField("field_"+i, DataTypes.BooleanType, false));
		}
		StructType schema = DataTypes.createStructType(fields);
		
		return schema;
	}

	private static JavaRDD<WindowInfo> createWindowInfos(int n, JavaSparkContext jsc) {
		List<WindowInfo> points = new ArrayList<>();
		for(int i=0;i<100;i++){
			points.add(new WindowInfo(n,random));
		}
		return jsc.parallelize(points);
	}

}
