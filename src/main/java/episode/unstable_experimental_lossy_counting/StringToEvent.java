package episode.unstable_experimental_lossy_counting;

import util.Pair;

public class StringToEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Pair<EventType, Integer> map(Pair<String, Integer> e) throws Exception {
		return new Pair<EventType,Integer>(new EventType(((String) e.getFirst()).charAt(0)),e.getSecond());
	}
	

}
