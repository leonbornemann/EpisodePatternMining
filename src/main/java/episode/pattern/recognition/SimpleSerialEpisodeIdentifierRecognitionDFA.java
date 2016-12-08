package episode.pattern.recognition;

import data.events.CategoricalEventType;
import episode.pattern.storage.EpisodeIdentifier;

public class SimpleSerialEpisodeIdentifierRecognitionDFA<T> implements SimpleEpisodeRecognitionDFA<T>{

	private int pos;
	private EpisodeIdentifier<T> identifier;
	
	public SimpleSerialEpisodeIdentifierRecognitionDFA(EpisodeIdentifier<T> identifier) {
		pos = 0;
		this.identifier = identifier;
	}
	
	public void reset(){
		pos=0;
	}
	
	public boolean isDone(){
		return pos >= identifier.getCanonicalEpisodeRepresentation().size();
	}

	public boolean waitsFor(CategoricalEventType e){
		return identifier.getCanonicalEpisodeRepresentation().get(pos).equals(e);
	}

	public EpisodeIdentifier<T> getEpisodePattern() {
		return identifier;
	}
	
	public CategoricalEventType peek(){
		if(pos<identifier.getCanonicalEpisodeRepresentation().size()){
			return identifier.getCanonicalEpisodeRepresentation().get(pos);
		} else{
			return null;
		}
	}

	@Override
	public void processEvent(CategoricalEventType e) {
		if(waitsFor(e)){
			pos++;
		}
	}
}
