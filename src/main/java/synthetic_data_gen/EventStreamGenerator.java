package synthetic_data_gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.flink.api.java.tuple.Tuple2;

public class EventStreamGenerator {

	private int minDeltaT;
	private int maxDeltaT;
	private List<Tuple2<String, Integer>> typeToWeight;
	private int weightSum;
	private Random random;
	
	public EventStreamGenerator(int minDeltaT, int maxDeltaT, List<Tuple2<String, Integer>> typeToProbability,Random random) {
		super();
		this.minDeltaT = minDeltaT;
		this.maxDeltaT = maxDeltaT;
		this.typeToWeight = typeToProbability;
		this.random = random;
		weightSum = typeToProbability.stream().mapToInt(a -> a.getField(1)).reduce((a,b) ->a+b).getAsInt();
	}
	
	public void generateEventStream(File target,int numEvents) throws IOException{
		PrintWriter out = new PrintWriter(new FileWriter(target));
		long currentTimeStamp = 0;
		for(int i=0;i<numEvents;i++){
			currentTimeStamp = getNextTimeStamp(currentTimeStamp);
			if(i!=numEvents-1){
				out.println(getNextEvent() + "," + currentTimeStamp);
			} else{
				out.print(getNextEvent() + "," + currentTimeStamp);
			}
		}
		out.close();
	}

	private long getNextTimeStamp(long currentTimeStamp) {
		return currentTimeStamp + random.nextInt((maxDeltaT - minDeltaT) + 1) + minDeltaT;
	}

	private String getNextEvent() {
		int index = random.nextInt(weightSum);
		int sum = 0;
	    int i=0;
	    while(sum < index ) {
	    	Integer curRelativeProb = typeToWeight.get(i).getField(1);
	    	sum = sum + curRelativeProb;
	    	i++;
    	}
	    return typeToWeight.get(Math.max(0,i-1)).getField(0);
	}
}
