package synthetic;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import data.AnnotatedEventType;
import data.Change;
import data.stream.AnnotatedEventStream;
import data.stream.PredictorPerformance;
import episode.finance.EpisodePattern;
import prediction.mining.PERMSTrainer;
import prediction.mining.WindowMiner;
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
		WindowMiner winMiner = new WindowMiner(stream, A, 100, d);
		PERMSTrainer miner = new PERMSTrainer(winMiner, eventAlphabet,15,20);
		Map<EpisodePattern, Double> predictors = miner.getInitialPreditiveEpisodes();
		Map<EpisodePattern, Double> inversePredictors = miner.getInitialInversePreditiveEpisodes();
		//printTrustScores(predictors);
		//printTrustScores(inversePredictors);
		//TODO: rework this, removed stream monitor here
	}
	
	private static void printTrustScores(Map<EpisodePattern, PredictorPerformance> trustScores) {
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
	}

}
