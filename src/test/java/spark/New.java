package spark;

/*** SimpleApp.java ***/
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;

public class New {
  public static void main(String[] args) {
    String logFile = "C:/Users/LeonBornemann/Documents/Uni/Master thesis/spark-1.6.2/README.md"; // Should be some file on your system
    JavaSparkContext sc = new JavaSparkContext("local", "Simple App",
      "C:/Users/LeonBornemann/Documents/Uni/Master thesis/spark-1.6.2", new String[]{"target/simple-project-1.0.jar"});
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
