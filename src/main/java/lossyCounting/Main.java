package lossyCounting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.CsvReader;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;

import lossyCounting.algorithms.Algorithm;
import lossyCounting.algorithms.impl.LossyCounting;
import synthetic_data_gen.EventStreamGenerator;

public class Main {

	public static void main(String[] args) throws Exception{
		//data generation
		String filePath = "resources/testdata/test.csv";
		List<Tuple2<String, Integer>> typeToWeight = initTypeToWeight();
		EventStreamGenerator gen = new EventStreamGenerator(1, 5, typeToWeight , new Random(13));
		gen.generateEventStream(new File(filePath), 100000);
		//algorithm execution
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		CsvReader reader = env.readCsvFile(filePath );
		DataSet<Tuple2<String,Integer>> dataSet = reader.types(String.class,Integer.class);
		Algorithm lossyCountingAlg = new LossyCounting<String>(dataSet.collect(),0.1,0.01);
		lossyCountingAlg.execute();
	}

	private static List<Tuple2<String, Integer>> initTypeToWeight() {
		List<Tuple2<String, Integer>> typeToWeight = new ArrayList<>();
		typeToWeight.add(new Tuple2<String,Integer>("A",249));
		typeToWeight.add(new Tuple2<String,Integer>("B",249));
		typeToWeight.add(new Tuple2<String,Integer>("C",500));
		typeToWeight.add(new Tuple2<String,Integer>("D",2));
		return typeToWeight;
	}
}
