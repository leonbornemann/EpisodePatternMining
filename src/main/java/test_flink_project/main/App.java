package test_flink_project.main;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) throws Exception {
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

	    DataSet<String> text = env.fromElements(
            "Who's there?",
            "I think I hear them. Stand, ho! Who's there?");

        DataSet<Tuple2<String, Integer>> wordCounts = text.flatMap(new LineSplitter()).groupBy(0).sum(1);
        wordCounts.print();
        env.execute("Word Count Example");
    }

    public static class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
		public void flatMap(String line, org.apache.flink.util.Collector<Tuple2<String, Integer>> out)throws Exception {
			for (String word : line.split(" ")) {
                out.collect(new Tuple2<String, Integer>(word, 1));
            }
		}
    }
}
