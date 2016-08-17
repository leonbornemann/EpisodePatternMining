package prediction.mining;

import java.util.Map;

import episode.finance.EpisodePattern;
import prediction.data.Change;
import prediction.data.stream.StreamWindow;

public class PredictiveEpisodeModel implements PredictiveModel {

	private Map<EpisodePattern, Integer> predictors;
	private Map<EpisodePattern, Integer> inversePredictors;

	public PredictiveEpisodeModel(Map<EpisodePattern, Integer> predictors,Map<EpisodePattern, Integer> inversePredictors) {
		this.predictors = predictors;
		this.inversePredictors = inversePredictors;
	}

	@Override
	public Change predict(StreamWindow currentWindow) {
		int count = 0;
		count += predictors.keySet().stream().filter(e -> currentWindow.containsPattern(e)).count();
		count -= inversePredictors.keySet().stream().filter(e -> currentWindow.containsPattern(e)).count();
		if(count==0){
			return Change.EQUAL;
		} else if(count>0){
			return Change.UP;
		} else{
			return Change.DOWN;
		}
	}

}
