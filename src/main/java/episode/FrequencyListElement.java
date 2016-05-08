package episode;

public class FrequencyListElement {

	private int freq;
	private int delta;
	
	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

	public FrequencyListElement(int freq, int delta) {
		super();
		this.freq = freq;
		this.delta = delta;
	}

	public void incFreq() {
		this.freq++;
		
	}
	
	
}
