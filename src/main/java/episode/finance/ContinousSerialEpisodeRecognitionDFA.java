package episode.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import util.Pair;

public class ContinousSerialEpisodeRecognitionDFA implements ContinousEpisodeRecognitionDFA{

	//TODO: incorporate d!
	private SerialEpisodePattern episode;
	private Map<Integer,Pair<AnnotatedEventType,LocalDateTime>> positions = new HashMap<>();
	private int ocurranceCount = 0;

	public ContinousSerialEpisodeRecognitionDFA(SerialEpisodePattern serialEpisodePattern) {
		this.episode = serialEpisodePattern;
	}
	
	/***
	 * Processes the arrival of a new Event in this automaton, representing an episode.
	 * @param e the newly arrived event
	 * @return A Pair of start- and endtime of an episode if the arrival of event e completed an episode occurance, null otherwise
	 */
	public Pair<LocalDateTime,LocalDateTime> processEvent(AnnotatedEvent e){
		if(e.getEventType().equals(episode.get(0)) && episode.length()==1){
			ocurranceCount++;
			return new Pair<>(e.getTimestamp(),e.getTimestamp());
		} else{
			Set<Integer> waitsForThis = positions.keySet().stream().filter(i -> positions.get(i).getFirst().equals(e.getEventType())).collect(Collectors.toSet()) ;//TODO!
			List<Pair<LocalDateTime,LocalDateTime>> recognizedEpisodes = new ArrayList<>();
			for(Integer i : waitsForThis){
				assert(positions.containsKey(i));
				if(i==episode.length()-1){
					LocalDateTime startTime = positions.remove(i).getSecond(); //We have recognized an episode
					recognizedEpisodes.add(new Pair<>(startTime,e.getTimestamp()));
				} else{
					Pair<AnnotatedEventType, LocalDateTime> entry = positions.remove(i);
					newState(i+1,entry.getSecond());
				}
			}
			//special case: we always wait for the first event of the episode, but this is not registered in positions.
			if(e.getEventType().equals(episode.get(0))){
				newState(1,e.getTimestamp());
			}
			assert(recognizedEpisodes.size()<=1);
			if(recognizedEpisodes.size()>0){
				ocurranceCount++;
				return recognizedEpisodes.get(0);
			} else{
				return null;
			}	
		}
	}

	private void newState(int i, LocalDateTime timestamp) {
		AnnotatedEventType nextEvent = episode.get(i);
		positions.put(i, new Pair<>(nextEvent,timestamp));
	}
	
	public EpisodePattern getEpsiodePattern(){
		return episode;
	}

	@Override
	public int getOccuranceCount() {
		return ocurranceCount;
	}
	

}
