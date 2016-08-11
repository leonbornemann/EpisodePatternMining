package prediction.mining;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import prediction.data.Change;

public class FeatureSelection {

		//TODO: test entropy calculation, use R as reference implementation?
		//just public so we can unit-test
		public static double calcEntropy(List<Change> classIds) {
			Map<Change,Integer> classFrequencies = new HashMap<>();
			for(Change classLabel : classIds){
				if(classFrequencies.containsKey(classLabel)){
					classFrequencies.put(classLabel, classFrequencies.get(classLabel)+1);
				} else{
					classFrequencies.put(classLabel, 1);
				}
			}
			double entropy = 0;
			for(Integer classFrequency : classFrequencies.values()){
				double p = (double)classFrequency/(double)classIds.size();
				entropy += -1*p*Math.log(p)/Math.log(2);
			}
			return entropy;
		}

		/***
		 * Calculates the information gain for a numeric attribute, given it's classes
		 * @param numericAttribute
		 * @param classIds
		 * @return
		 */
		public static double calcInfoGain(List<Change> classAttribute,List<Boolean> attribute) {
			assert(attribute.size() == classAttribute.size());
			//find the best splitting point
			double bestGain = 0;
			double entropy = calcEntropy(classAttribute);
			double curGain = entropy - infoAfterSplit(attribute,classAttribute);
			if(curGain>bestGain){
				bestGain = curGain;
			}
				
			return bestGain;
		}
		
		//just public so we can unit-test
		public static double infoAfterSplit(List<Boolean> booleanAttribute, List<Change> classIds) {
			List<Change> classOfTrue = new LinkedList<>();
			List<Change> classOfFalse = new LinkedList<>();
			for(int i=0;i<booleanAttribute.size();i++){
				if(booleanAttribute.get(i)){
					classOfTrue.add(classIds.get(i));
				} else{
					classOfFalse.add(classIds.get(i));
				}
			}
			return calcEntropy(classOfFalse)*classOfFalse.size()/booleanAttribute.size() + calcEntropy(classOfTrue)*classOfTrue.size()/booleanAttribute.size();
		}
}
