package episode.basic;


public class EventType implements Comparable<EventType>{

	private final char eventChar;

	public EventType(char c){
		this.eventChar = c;
	}
	
	@Override
	public String toString(){
		return ""+eventChar;
	}
	
	@Override
	public boolean equals(Object o){
		if(!( o instanceof EventType)){
			return false;
		} else{
			return eventChar == ((EventType) o).getEventChar();
		}
	}
	
	@Override
	public int hashCode(){
		return new Character(eventChar).hashCode();
	}

	private char getEventChar() {
		return eventChar;
	}

	@Override
	public int compareTo(EventType o) {
		if(eventChar<o.getEventChar()){
			return -1;
		} else if(eventChar== o.getEventChar()){
			return 0;
		} else{
			return 1;
		}
	}
}
