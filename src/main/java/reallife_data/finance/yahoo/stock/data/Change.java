package reallife_data.finance.yahoo.stock.data;

public enum Change {

	DOWN,EQUAL,UP;

	public Change getInverse() {
		if(this==DOWN){
			return UP;
		} else if(this==UP){
			return DOWN;
		} else{
			assert(false);
			return EQUAL;
		}
	}
}
