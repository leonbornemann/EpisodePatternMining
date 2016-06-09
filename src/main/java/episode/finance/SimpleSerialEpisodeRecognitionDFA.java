package episode.finance;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class SimpleSerialEpisodeRecognitionDFA implements SimpleEpisodeRecognitionDFA{

	private int pos;
	private SerialEpisodePattern serialEpisodePattern;
	
	public SimpleSerialEpisodeRecognitionDFA(SerialEpisodePattern serialEpisodePattern) {
		pos = 0;
		this.serialEpisodePattern = serialEpisodePattern;
	}
	
	public void reset(){
		pos=0;
	}
	
	public boolean isDone(){
		return pos >= serialEpisodePattern.length();
	}

	public boolean waitsFor(AnnotatedEventType e){
		return serialEpisodePattern.get(pos).equals(e);
	}

	public SerialEpisodePattern getEpisodePattern() {
		return serialEpisodePattern;
	}
	
	public AnnotatedEventType nextEvent(){
		if(pos<serialEpisodePattern.length()){
			return serialEpisodePattern.get(pos);
		} else{
			return null;
		}
	}

	@Override
	public void processEvent(AnnotatedEventType e) {
		if(waitsFor(e)){
			pos++;
		}
	}
}
