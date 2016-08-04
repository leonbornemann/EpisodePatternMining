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

	public double toDouble() {
		if(this==UP){
			return 0.0;
		} else if(this==DOWN){
			return 1.0;
		} else{
			return 2.0;
		}
	}

	public static Change fromDouble(double prediction) {
		if(prediction==0.0){
			return UP;
		} else if(prediction==1.0){
			return DOWN;
		} else if(prediction==2.0){
			return EQUAL;
		} else{
			assert(false);
			throw new AssertionError();
		}
	}
}
