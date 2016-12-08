package data.synthetic;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import data.events.CategoricalEvent;
import data.events.CategoricalEventType;
import data.stream.CategoricalEventStream;
import data.stream.InMemoryCategoricalEventStream;
import episode.pattern.EpisodePattern;
import episode.pattern.SerialEpisodePattern;
import episode.pattern.recognition.SimpleSerialEpisodeIdentifierRecognitionDFA;
import util.StandardDateTimeFormatter;

/***
 * Incomplete implementation of the episode pattern generator suggested in the paper "Generating diverse realistic data sets for episode mining" by Zimmerman et. al.
 * @author Leon Bornemann
 *
 */
public class Generator {

	//numerical parameters
	private double p;
	private int N;
	private Set<CategoricalEventType> eventAlphabet;
	private int n;
	private int T;
	private int g;
	private double o;
	private double G;
	private int m;
	//nominal parameters
	private NoiseKind P;
	private boolean r;
	private boolean s;
	private boolean i;
	private boolean W;
	private boolean h;
	private boolean S;
	private StandardDistribution d;
	private StandardDistribution D;
	
	Random random = new Random(13);
	private ArrayList<Double> weights;
	private ArrayList<SerialEpisodePattern> sourceEpisodes;
	private ArrayList<CategoricalEvent> stream;
	private Map<SerialEpisodePattern,SimpleSerialEpisodeIdentifierRecognitionDFA> embedStatus = new HashMap<>();
	private Map<SerialEpisodePattern, LocalDateTime> lastTimeStamps = new HashMap<>();
	private CategoricalEventType toPredict;
	
	public Generator(double p, int N, Set<CategoricalEventType> eventAlphabet,CategoricalEventType toPredict , int n, int T, int g, double o, double G,int m, NoiseKind P, boolean r, boolean s, boolean i, boolean W, boolean h, boolean S,StandardDistribution d, StandardDistribution D) {
		super();
		this.p = p;
		this.N = N;
		this.eventAlphabet = eventAlphabet;
		this.toPredict = toPredict;
		this.n = n;
		this.T = T;
		this.g = g;
		this.o = o;
		this.G = G;
		this.m = m;
		this.P = P;
		this.r = r;
		this.s = s;
		this.i = i;
		this.W = W;
		this.h = h;
		this.S = S;
		this.d = d;
		this.D = D;
		init();
	}

	private void init() {
		generateSourceEpisodes();
		generateStream();
	}

	private void generateStream() {
		stream = new ArrayList<>();
		sourceEpisodes.forEach(e -> embedStatus.put(e, (SimpleSerialEpisodeIdentifierRecognitionDFA) e.getSimpleRecognitionDFA()));
		LocalDateTime timestamp = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		while(stream.size() < T){
			if(noiseEvent()){
				stream.add(getNoiseEvent(timestamp));
				timestamp = incrementTimestamp(timestamp);
			} else{
				if(S){
					assert(false);
					//should never be the case!
				} else{
					if(!i){
						//TODO: not interleaved
						assert(false);
						EpisodePattern chosen = weightedRandom();
						
					} else{
						//interleaved
						SerialEpisodePattern chosen = weightedRandom();
						embedNextEvent(timestamp, chosen);
						long delta = randomTimeSpan();
						timestamp = timestamp.plus(delta, ChronoUnit.SECONDS);
						if(h){
							for(SerialEpisodePattern episode : lastTimeStamps.keySet()){
								LocalDateTime lastTimeStamp = lastTimeStamps.get(episode);
								if( differenceInSeconds(timestamp.plus(delta, ChronoUnit.SECONDS),lastTimeStamp)> g){
									embedNextEvent(lastTimeStamp.plus(g, ChronoUnit.SECONDS), chosen);
								}
							}
						}
					}
				}
			}
		}
		stream.sort( (a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));
	}

	private long differenceInSeconds(LocalDateTime a, LocalDateTime b) {
		return ChronoUnit.SECONDS.between(a, b);
	}

	private void embedNextEvent(LocalDateTime timestamp, SerialEpisodePattern chosen) {
		SimpleSerialEpisodeIdentifierRecognitionDFA dfa = embedStatus.get(chosen);
		CategoricalEventType nextEvent = dfa.peek();
		dfa.processEvent(nextEvent);
		if(dfa.isDone()){
			dfa.reset();
		}
		stream.add(new CategoricalEvent(nextEvent,timestamp));
		lastTimeStamps.put(chosen,timestamp);
	}

	private SerialEpisodePattern weightedRandom() {
		double p = random.nextDouble();
		double cumulativeProbability = 0.0;
		for (int i=0;i<weights.size();i++) {
		    cumulativeProbability += weights.get(i);
		    if (p <= cumulativeProbability) {
		        return sourceEpisodes.get(i);
		    }
		}
		assert(false);
		return null;
	}

	private LocalDateTime incrementTimestamp(LocalDateTime timestamp) {
		return timestamp.plus(randomTimeSpan(), ChronoUnit.SECONDS);
	}

	private long randomTimeSpan() {
		return random.nextInt(g) +1; //TODO: consider the other parameters!
	}

	private CategoricalEvent getNoiseEvent(LocalDateTime timestamp) {
		if(P==NoiseKind.UNIFORM){
			return new CategoricalEvent(sampleUniformly(eventAlphabet),timestamp);
		} else{
			assert(false); //not yet implemented
			return null;
		}
	}

	private boolean noiseEvent() {
		return p < random.nextDouble(); //TODO: is this correct?
	}

	private void generateSourceEpisodes() {
		weights = new ArrayList<>();
		sourceEpisodes = new ArrayList<>();
		int toPredictIndex = 1;
		int inverseToPRedictIndex = 2;
		for(int i=1;i<=n;i++){
			boolean repeated = false;
			boolean shared = false;
			List<CategoricalEventType> selected = new ArrayList<>();
			for(int j=1;j<=N;j++){
				boolean accepted = false;
				CategoricalEventType E = null;
				while(!accepted){
					E = sampleUniformly(eventAlphabet);
					accepted = true;
					if(!r && selected.contains(E)){
						//reject E
						accepted = false;
					} else if(r && selected.contains(E)){
						accepted = true;
						repeated = true;
					} else if(r && !repeated && j==N){
						E = sampleUniformly(selected);
						accepted = true;
					}
					if(!s && getUnionOfContainedTypes(sourceEpisodes).contains(E)){
						accepted = false;
					} else if(s && getUnionOfContainedTypes(sourceEpisodes).contains(E)){
						accepted = true;
						shared = true;
					} else if(s && !shared && j==N){
						if(sourceEpisodes.isEmpty()){
							E = sampleUniformly(eventAlphabet);
						} else{
							E = sampleUniformly(getUnionOfContainedTypes(sourceEpisodes));
						}
						accepted = true;
					}
				}
				assert(E!=null);
				selected.add(E);
			}
			if(i==toPredictIndex){
				selected.add(toPredict);
				System.out.println("chose toPredict");
			} else if(i==inverseToPRedictIndex){
				System.out.println("chose inverse");
				selected.add(toPredict.getInverseEvent());
			}
			sourceEpisodes.add(new SerialEpisodePattern(selected));
			double weight = Double.MIN_VALUE + (1.0 - Double.MIN_VALUE) * random.nextDouble();
			weights.add(weight);
		}
		double weightSum = weights.stream().mapToDouble(Double::doubleValue).sum();
		for(int i=0;i<n;i++){
			weights.set(i,weights.get(i) / weightSum);
		}		
	}

	private CategoricalEventType sampleUniformly(Collection<CategoricalEventType> types) {
		ArrayList<CategoricalEventType> typesAsList = new ArrayList<>(types);
		Collections.sort(typesAsList);
		return typesAsList.get(random.nextInt(types.size()));
	}

	private Set<CategoricalEventType> getUnionOfContainedTypes(ArrayList<? extends EpisodePattern> sourceEpisodes) {
		Set<CategoricalEventType> allTypes = new HashSet<>();
		sourceEpisodes.forEach(e -> allTypes.addAll(e.getAllContainedTypes()));
		return allTypes;
	}

	public CategoricalEventStream getGeneratedStream() {
		return new InMemoryCategoricalEventStream(stream);
	}

	public List<SerialEpisodePattern> getSourceEpisodes() {
		return sourceEpisodes;
	}

	public List<Double> getWeights() {
		return weights;
	}
	
	
	
	
}
