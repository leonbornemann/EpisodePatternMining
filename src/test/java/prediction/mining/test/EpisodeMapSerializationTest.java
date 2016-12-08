package prediction.mining.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import data.events.CategoricalEventType;
import data.events.Change;
import episode.pattern.EpisodePattern;
import episode.pattern.ParallelEpisodePattern;
import episode.pattern.SerialEpisodePattern;
import util.IOService;

public class EpisodeMapSerializationTest {

	private static CategoricalEventType A = new CategoricalEventType("foo", Change.UP);
	private static CategoricalEventType B = new CategoricalEventType("bar", Change.EQUAL);
	private static CategoricalEventType C = new CategoricalEventType("mystic", Change.DOWN);
	private static CategoricalEventType D = new CategoricalEventType("magic", Change.UP);
	
	
	@Test
	public void test() throws FileNotFoundException, IOException, ClassNotFoundException {
		Map<EpisodePattern,Double> map = new HashMap<>();
		map.put(new SerialEpisodePattern(A,A,B,C), 1337.0);
		map.put(new ParallelEpisodePattern(A,A,B,B,C,D), 42.0);
		map.put(new ParallelEpisodePattern(A,A,B,C), 133742.0);
		File file = new File("resources/testdata/serialized Episodes/test.map");
		IOService.serializeEpisodeMap(map, file);
		Map<EpisodePattern, Double> newMap = IOService.loadEpisodeMap(file);
		assertEquals(map,newMap);
	}

}
