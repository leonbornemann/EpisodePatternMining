package episode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.CsvReader;
import org.apache.flink.api.java.tuple.Tuple2;

import synthetic_data_gen.EventStreamGenerator;

public class Main {

	public static void main(String[] args) throws Exception {
		//data generation
		String filePath = "resources/testdata/test.csv";
		List<Tuple2<String, Integer>> typeToWeight = initTypeToWeight();
		EventStreamGenerator gen = new EventStreamGenerator(1, 5, typeToWeight , new Random(13));
		gen.generateEventStream(new File(filePath), 100000);
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		CsvReader reader = env.readCsvFile(filePath );
		DataSet<Tuple2<String,Integer>> dataSet = reader.types(String.class,Integer.class);
		DataSet<Tuple2<EventType,Integer>> newDataSet = dataSet.map(new StringToEvent());
		EpisodePatternMiner miner = new EpisodePatternMiner(newDataSet.collect(), 0.03, getEventAlphabet(typeToWeight));
		miner.execute();
	}
	
	private static Set<EventType> getEventAlphabet(List<Tuple2<String, Integer>> typeToWeight) {
		Set<EventType> set = new HashSet<>();
		for(Tuple2<String, Integer> a : typeToWeight){
			set.add(new EventType(((String) a.getField(0)).charAt(0)));
		}
		return set;
	}

	private static List<Tuple2<String, Integer>> initTypeToWeight() {
		List<Tuple2<String, Integer>> typeToWeight = new ArrayList<>();
		typeToWeight.add(new Tuple2<String,Integer>("A",99));
		typeToWeight.add(new Tuple2<String,Integer>("B",99));
		typeToWeight.add(new Tuple2<String,Integer>("C",250));
		typeToWeight.add(new Tuple2<String,Integer>("D",275));
		typeToWeight.add(new Tuple2<String,Integer>("E",50));
		typeToWeight.add(new Tuple2<String,Integer>("F",50));
		typeToWeight.add(new Tuple2<String,Integer>("G",100));
		typeToWeight.add(new Tuple2<String,Integer>("H",25));
		typeToWeight.add(new Tuple2<String,Integer>("I",25));
		typeToWeight.add(new Tuple2<String,Integer>("J",25));
		typeToWeight.add(new Tuple2<String,Integer>("K",2));
		return typeToWeight;
	}

}
