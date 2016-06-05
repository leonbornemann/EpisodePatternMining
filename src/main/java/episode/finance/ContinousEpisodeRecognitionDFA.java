package episode.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import util.Pair;

public class ContinousEpisodeRecognitionDFA {

	//TODO: incorporate d!
	private SerialEpisodePattern episode;
	private Map<Integer,Pair<AnnotatedEventType,LocalDateTime>> positions = new HashMap<>();

	public ContinousEpisodeRecognitionDFA(SerialEpisodePattern serialEpisodePattern) {
		this.episode = serialEpisodePattern;
	}
	
	/***
	 * Processes the arrival of a new Event in this automaton, representing an episode.
	 * @param e the newly arrived event
	 * @return A Pair of start- and endtime of an episode if the arrival of event e completed an episode occurance, null otherwise
	 */
	public Pair<LocalDateTime,LocalDateTime> processEvent(AnnotatedEvent e){
		if(e.getEventType().equals(episode.get(0))){
			if(episode.length()>1){
				newState(1,e.getTimestamp());
				return null;
			} else{
				return new Pair<>(e.getTimestamp(),e.getTimestamp());
			}
		} else{
			Set<Integer> waitsForThis = positions.keySet().stream().filter(i -> positions.get(i).getFirst().equals(e)).collect(Collectors.toSet()) ;//TODO!
			List<Pair<LocalDateTime,LocalDateTime>> recognizedEpisodes = new ArrayList<>();
			for(Integer i : waitsForThis){
				assert(positions.containsKey(i));
				if(i==episode.length()-1){
					positions.remove(i); //We have recognized an episode
					recognizedEpisodes.add(new Pair<>(positions.get(i).getSecond(),e.getTimestamp()));
				} else{
					Pair<AnnotatedEventType, LocalDateTime> entry = positions.remove(i);
					newState(i+1,entry.getSecond());
				}
			}
			assert(recognizedEpisodes.size()<=1);
			if(recognizedEpisodes.size()>0){
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
	
	public SerialEpisodePattern getEpsiodePattern(){
		return episode;
	}
	

}
