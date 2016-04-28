package lossyCounting.algorithms.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;

import lossyCounting.algorithms.Algorithm;
import trie.SerialEpisodeTrie;
import trie.SerialEpisodeTrieNode;

public class LossyCounting<T> implements Algorithm {

	private double e;
	private double s;
	private List<Tuple2<T, Integer>> source;
	private int bucketWidth;
	SerialEpisodeTrie<FrequencyListElement> frequencyList;
	private int highestBucketNum;

	public LossyCounting(List<Tuple2<T, Integer>> subDataSet, double support, double epsilon,SerialEpisodeTrie<FrequencyListElement> frequencyList,int highestBucketNum) {
		this.e = epsilon;
		this.s = support;
		this.source = subDataSet;
		this.bucketWidth = (int) Math.ceil(1.0/epsilon);
		this.highestBucketNum = highestBucketNum;
	}

	public void execute() {
		source.forEach( t -> updateList(t));
		pruneList(highestBucketNum);
		outputFrequent();
	}

	private void outputFrequent() {
		frequencyList.entrySet().stream().filter( e -> isFrequent(e.getValue())).forEach( e -> output(e));
	}

	private void output(Entry<String, FrequencyListElement> elem) {
		System.out.println(elem.getKey() + " is frequent with approx " + elem.getValue().getFreq()/(double)n +" support");
	}

	private boolean isFrequent(FrequencyListElement listElem) {
		return listElem.getFreq() >= (s-e)*n;
	}

	private Integer updateList(Tuple2<T, Integer> t) {
		int curBucketId = getCurBucketId();
		if(frequencyList.containsKey(t.getField(0))){
			frequencyList.get(t.getField(0)).incFreq();
		} else{
			frequencyList.put(t.getField(0),new FrequencyListElement(1, curBucketId-1));
		}
		//pruning of D:
		if((n % bucketWidth) ==0){
			pruneList(curBucketId);
		}
		return 0;
	}

	private void pruneList(int curBucketId) {
		frequencyList = frequencyList.entrySet().stream().filter( e -> hasEnough(e,curBucketId)).collect(Collectors.toMap(p -> p.getKey(),p -> p.getValue()));
	}

	private boolean hasEnough(Entry<String, FrequencyListElement> e, int curBucketId) {
		boolean hasEnough = e.getValue().getFreq()+e.getValue().getDelta() > curBucketId;
		if(!hasEnough){
			System.out.println("pruning " + e.getKey() + " from frequency List");
		}
		return hasEnough;
	}

	private int getCurBucketId() {
		return (int)(Math.ceil((double) n/ (double)bucketWidth));
	}

}
