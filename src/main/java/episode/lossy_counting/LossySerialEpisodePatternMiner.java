package episode.lossy_counting;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.flink.api.java.tuple.Tuple2;

import trie.SerialEpisodeTrie;

public class LossySerialEpisodePatternMiner {

	private double s;
	private List<Tuple2<EventType, Integer>> source;
	private Set<EventType> eventAlphabet;
	private SerialEpisodeTrie<FrequencyListElement> frequencyList;
	private double e;
	private int bucketWidth;

	public LossySerialEpisodePatternMiner(List<Tuple2<EventType, Integer>> dataSet, Set<EventType> eventAlphabet, double support,double epsilon) {
		this.source = dataSet;
		this.eventAlphabet = eventAlphabet;
		this.s = support;
		this.e = epsilon;
		this.bucketWidth = (int) Math.ceil(1.0/epsilon);
		frequencyList = new SerialEpisodeTrie<>(eventAlphabet);
	}
	
	public void executeLossyCounting(){
		int numTotalBuckets = (int) Math.floor(source.size()/(double)bucketWidth);
		int desiredBucketCountPerIteration = 40;
		assert(desiredBucketCountPerIteration<numTotalBuckets);
		int start=0;
		int end = bucketWidth*desiredBucketCountPerIteration;
		int curStartBucketNum = 1;
		int curEndBucketNum = desiredBucketCountPerIteration;
		while(true){
			lossyCountingStep(start,end,curStartBucketNum,curEndBucketNum);
			if(end==source.size()){
				break;
			}
			start = end;
			end = Math.min(start + bucketWidth*desiredBucketCountPerIteration,source.size());
			curStartBucketNum = curEndBucketNum+1;
			if(start + bucketWidth*desiredBucketCountPerIteration>source.size()){
				assert(end==source.size());
				curEndBucketNum = (int) Math.floor(end / (double)bucketWidth);
			} else{
				curEndBucketNum += desiredBucketCountPerIteration;
			}
		}
		outputFrequent();
	}
	
	private void outputFrequent() {
		Iterator<Entry<SerialEpisode,FrequencyListElement>> it = frequencyList.bfsIterator();
		it.forEachRemaining(e -> printIfFrequent(e));;
	}

	private void printIfFrequent(Entry<SerialEpisode, FrequencyListElement> entry) {
		if(isFrequent(entry.getValue())){
			System.out.println("Episode "+ entry.getKey() +" is frequent with at least" + getApproxSupport(entry.getValue()) + " support");
		}
	}

	private double getApproxSupport(FrequencyListElement value) {
		return value.getFreq() / (double)source.size();
	}

	private boolean isFrequent(FrequencyListElement value) {
		return value.getFreq() >= (s-e)*source.size();
	}

	private void lossyCountingStep(int start, int end,int curStartBucketNum, int curEndBucketNum) {
		new LossyEpisodeMiner(source.subList(start, end),curStartBucketNum,curEndBucketNum,frequencyList, eventAlphabet).mine();
	}
}
