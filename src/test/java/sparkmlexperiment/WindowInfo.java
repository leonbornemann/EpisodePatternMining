package sparkmlexperiment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WindowInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<Boolean> occurrences;
	Classlabel classlabel;
	
	public WindowInfo(int n, Random random){
		occurrences = new ArrayList<>(n);
		for(int i=0;i<n;i++){
			occurrences.add(random.nextBoolean());
		}
		classlabel = Classlabel.values()[random.nextInt(Classlabel.values().length)];
	}

	public String getClassLabel() {
		return classlabel.toString();
	}

	public Boolean[] getFeatureArray() {
		return occurrences.toArray(new Boolean[occurrences.size()]);
	}
}
