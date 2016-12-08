package data.events;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.IOService;

/***
 * Event type class, that consists of the company ID and the change.
 * @author Leon Bornemann
 *
 */
public class CategoricalEventType implements Comparable<CategoricalEventType>,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//static maps to translate from the internal representation:
	private static Map<String,Short> stringToId = new HashMap<>();
	private static Map<Short,String> idToString = new HashMap<>();
	private static short curId = 0;
	
	//internally we save the id as a short and the change as a byte to save memory!
	private short companyID;
	private byte change;
	
	public static Set<CategoricalEventType> loadEventAlphabet(Set<String> allCompanyIDS) throws IOException{
		Set<CategoricalEventType> eventAlphabet = new HashSet<>();
		for(String id : allCompanyIDS ){
			for(Change change : Change.values()){
				eventAlphabet.add(new CategoricalEventType(id, change));
			}
		}
		return eventAlphabet;
	}
	
	public CategoricalEventType(String companyID, Change change){
		if(stringToId.containsKey(companyID)){
			assert(idToString.containsKey(stringToId.get(companyID)));
		} else{
			stringToId.put(companyID, curId);
			idToString.put(curId, companyID);
			curId++;
		}
		this.companyID = stringToId.get(companyID);
		this.change = (byte) change.ordinal();
	}
	
	public Change getChange(){
		return Change.values()[change];
	}
	
	public String getCompanyID(){
		return idToString.get(companyID);
	}
	
	@Override
	public boolean equals(Object o){
		if(!( o instanceof CategoricalEventType)){
			return false;
		} else{
			CategoricalEventType other = (CategoricalEventType) o;
			return companyID == other.companyID && change==other.change;
		}
	}
	
	@Override
	public int hashCode(){
		return new Short(companyID).hashCode()*new Byte(change).hashCode()*7;
	}

	@Override
	public int compareTo(CategoricalEventType o) {
		if(companyID <o.companyID){
			return -1;
		} else if(companyID>o.companyID){
			return 1;
		} else{
			//same company, the enum decides the ordering
			if(change<o.change){
				return -1;
			} else if(change>o.change){
				return 1;
			} else{
				return 0;
			}
		}
	}

	public CategoricalEventType getInverseEvent() {
		return new CategoricalEventType(idToString.get(companyID), getChange().getInverse());
	}
	
	@Override
	public String toString(){
		return idToString.get(companyID) + "_" + getChange();
	}

	public static Set<CategoricalEventType> loadEventAlphabet() throws IOException {
		return loadEventAlphabet(IOService.getAllCompanyCodes());
	}
}
