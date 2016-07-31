package reallife_data.finance.yahoo.stock.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.stream.AnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class FeatureBasedMiner {
	
	private List<StreamWindow> positiveExamples;
	private List<StreamWindow> negativeExamples;
	private List<StreamWindow> neutralExamples;

	public FeatureBasedMiner(List<StreamWindow> positiveExamples, List<StreamWindow> negativeExamples, List<StreamWindow> neutralExamples, Set<AnnotatedEventType> eventAlphabet, int s){
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
		this.neutralExamples = neutralExamples;
		List<Change> classAttribute = buildClassAttribute();
		EpisodeDiscovery discovery = new EpisodeDiscovery();
		Map<EpisodePattern, List<Boolean>> positiveAttributes = discovery.mineFrequentEpisodes(positiveExamples, eventAlphabet, s);
		Map<EpisodePattern, List<Boolean>> negativeAttributes = discovery.mineFrequentEpisodes(negativeExamples, eventAlphabet, s);
		Map<EpisodePattern, List<Boolean>> neutralAttributes = discovery.mineFrequentEpisodes(neutralExamples, eventAlphabet, s);

		//TODO: selection via information gain
		
		//TODO: train random forest!
	}

	private List<Change> buildClassAttribute() {
		List<Change> classAttribute = new ArrayList<>();
		positiveExamples.forEach(e -> classAttribute.add(Change.UP));
		negativeExamples.forEach(e -> classAttribute.add(Change.DOWN));
		neutralExamples.forEach(e -> classAttribute.add(Change.EQUAL));
		return classAttribute;
	}
}
