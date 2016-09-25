package prediction.mining.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import data.AnnotatedEventType;
import data.Change;
import episode.finance.EpisodePattern;
import episode.finance.ParallelEpisodePattern;
import episode.finance.SerialEpisodePattern;
import prediction.mining.Main;
import prediction.util.IOService;

public class EpisodeMapSerializationTest {

	private static AnnotatedEventType A = new AnnotatedEventType("foo", Change.UP);
	private static AnnotatedEventType B = new AnnotatedEventType("bar", Change.EQUAL);
	private static AnnotatedEventType C = new AnnotatedEventType("mystic", Change.DOWN);
	private static AnnotatedEventType D = new AnnotatedEventType("magic", Change.UP);
	
	
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
