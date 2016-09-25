package episode.finance.recognition;

import data.AnnotatedEventType;
import episode.finance.SerialEpisodePattern;
import episode.finance.storage.EpisodeIdentifier;

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

	public boolean waitsFor(AnnotatedEventType e){
		return identifier.getCanonicalEpisodeRepresentation().get(pos).equals(e);
	}

	public EpisodeIdentifier<T> getEpisodePattern() {
		return identifier;
	}
	
	public AnnotatedEventType peek(){
		if(pos<identifier.getCanonicalEpisodeRepresentation().size()){
			return identifier.getCanonicalEpisodeRepresentation().get(pos);
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
