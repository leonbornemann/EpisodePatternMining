package episode.unstable_experimental_lossy_counting;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

public class StringToEvent implements MapFunction<Tuple2<String,Integer>, Tuple2<EventType,Integer>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Tuple2<EventType, Integer> map(Tuple2<String, Integer> e) throws Exception {
		return new Tuple2<EventType,Integer>(new EventType(((String) e.getField(0)).charAt(0)),e.getField(1));
	}
	

}
