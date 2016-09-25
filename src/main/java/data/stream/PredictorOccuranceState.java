package data.stream;

import java.time.LocalDateTime;

import episode.finance.recognition.ContinousEpisodeRecognitionDFA;

public class PredictorOccuranceState {

	private LocalDateTime expiryTime;
	private boolean hadOccurance = false;
	private ContinousEpisodeRecognitionDFA dfa;

	public PredictorOccuranceState(ContinousEpisodeRecognitionDFA automaton, LocalDateTime expiryTime) {
		this.dfa = automaton;
		this.expiryTime = expiryTime;
	}

	public LocalDateTime getExpiryTime() {
		return expiryTime;
	}

	public boolean hadOccurance() {
		return hadOccurance;
	}

	public ContinousEpisodeRecognitionDFA getDFA() {
		return dfa;
	}

	public void setOccurance(boolean hadOccurance) {
		this.hadOccurance = hadOccurance;
	}

}
