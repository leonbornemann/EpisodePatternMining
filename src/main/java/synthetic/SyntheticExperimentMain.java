package synthetic;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.AnnotatedEventStream;
import prediction.data.stream.PredictorPerformance;
import prediction.data.stream.StreamMonitor;
import prediction.mining.PredictiveMiner;
import semantic.SemanticKnowledgeCollector;
import synthetic.datagen.Generator;
import synthetic.datagen.NoiseKind;
import synthetic.datagen.StandardDistribution;

public class SyntheticExperimentMain {

	public static void main(String[] args) throws IOException {
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
		AnnotatedEventType A = new AnnotatedEventType("randomCompanyNotInTheOtherList",Change.UP);
		System.out.println("Starting data generator");
		Generator gen = new Generator(0.2,4,eventAlphabet,A ,10,100000,20,0.0,300,1,NoiseKind.UNIFORM,true,true,true,true,true,false,StandardDistribution.UNIFORM,StandardDistribution.UNIFORM);
		AnnotatedEventStream stream = gen.getGeneratedStream();
		//print ground truth:
		System.out.println("The following Episodes were embedded:");
		for(int i=0;i<gen.getSourceEpisodes().size();i++){
			System.out.println(gen.getSourceEpisodes().get(i) +"  with weight "+ gen.getWeights().get(i));
		}
		//DIRTY DIRTY COPY:
		int d = 1000;
		PredictiveMiner miner = new PredictiveMiner(stream,A,eventAlphabet,100,15,20,d);
		Map<EpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		Map<EpisodePattern, Integer> inversePredictors = miner.getInitialInversePreditiveEpisodes();
		//printTrustScores(predictors);
		//printTrustScores(inversePredictors);
		StreamMonitor monitor = new StreamMonitor(predictors,inversePredictors, stream, A, d,new File("resources/logs/performanceLog.txt"));
		System.out.println(monitor.getInvestmentTracker().netWorth());
		monitor.monitor();
		Map<EpisodePattern, PredictorPerformance> trustScores = monitor.getCurrentTrustScores();
		Map<EpisodePattern, PredictorPerformance> inverseTrustScores = monitor.getCurrentInverseTrustScores();
		printTrustScores(trustScores);
		System.out.println(monitor.getInvestmentTracker().netWorth());
		System.out.println(monitor.getInvestmentTracker().getPrice());
	}
	
	private static void printTrustScores(Map<EpisodePattern, PredictorPerformance> trustScores) {
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
	}

}
