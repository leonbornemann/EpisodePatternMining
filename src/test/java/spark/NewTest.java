package spark;

import static org.junit.Assert.*;

import org.junit.Test;
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;


public class NewTest {

		@Test
	  public void main() {
	    String logFile = "resources/temp/test.txt"; // Should be some file on your system
	    SparkConf conf = new SparkConf().setMaster("local").setAppName("My App");
	    JavaSparkContext sc = new JavaSparkContext(conf);
	    JavaRDD<String> logData = sc.textFile(logFile).cache();

	    long numAs = logData.filter(new Function<String, Boolean>() {
	      public Boolean call(String s) { return s.contains("a"); }
	    }).count();

	    long numBs = logData.filter(new Function<String, Boolean>() {
	      public Boolean call(String s) { return s.contains("b"); }
	    }).count();

	    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
	  }
}
