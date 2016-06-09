package episode.finance;

import java.util.List;

public interface EpisodePatternGenerator<E extends EpisodePattern> {

	List<E> generateSize1Candidates();

	/***
	 * Generates the length k+1 candidates from the specified length k candidates
	 * @param sizeKCandidates
	 * @return
	 */
	List<E> generateNewCandidates(List<E> lengthKCandidates);

}
