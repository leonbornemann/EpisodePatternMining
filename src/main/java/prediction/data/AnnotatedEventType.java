package prediction.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import prediction.util.IOService;

public class AnnotatedEventType implements Comparable<AnnotatedEventType>,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Set<AnnotatedEventType> loadEventAlphabet(Set<String> allCompanyIDS) throws IOException{
		Set<AnnotatedEventType> eventAlphabet = new HashSet<>();
		for(String id : allCompanyIDS ){
			for(Change change : Change.values()){
				eventAlphabet.add(new AnnotatedEventType(id, change));
			}
		}
		return eventAlphabet;
	}
	
	private String companyID;
	private Change change;

	public AnnotatedEventType(String companyID, Change change){
		this.companyID = companyID;
		this.change = change;
	}
	
	public Change getChange(){
		return change;
	}
	
	public String getCompanyID(){
		return companyID;
	}
	
	@Override
	public boolean equals(Object o){
		if(!( o instanceof AnnotatedEventType)){
			return false;
		} else{
			AnnotatedEventType other = (AnnotatedEventType) o;
			return companyID.equals(other.companyID) && change==other.change;
		}
	}
	
	@Override
	public int hashCode(){
		return companyID.hashCode()*change.hashCode()*7;
	}

	@Override
	public int compareTo(AnnotatedEventType o) {
		if(companyID.compareTo(o.companyID)<0){
			return -1;
		} else if(companyID.compareTo(o.companyID)>0){
			return 1;
		} else{
			//same company, the enum decides the ordering
			if(change.compareTo(o.change)<0){
				return -1;
			} else if(change.compareTo(o.change)>0){
				return 1;
			} else{
				return 0;
			}
		}
	}

	public AnnotatedEventType getInverseEvent() {
		return new AnnotatedEventType(companyID, change.getInverse());
	}
	
	@Override
	public String toString(){
		return companyID + "_" + change;
	}

	public static Set<AnnotatedEventType> loadEventAlphabet() throws IOException {
		return loadEventAlphabet(IOService.getAllCompanyCodes());
	}
}
