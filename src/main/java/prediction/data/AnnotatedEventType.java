package prediction.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import prediction.util.IOService;

public class AnnotatedEventType implements Comparable<AnnotatedEventType>,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Map<String,Short> stringToId = new HashMap<>();
	private static Map<Short,String> idToString = new HashMap<>();
	private static short curId = 0;
	
	/*static {
		try {
			Set<String> codes = IOService.getAllCompanyCodes();
			idToString = new HashMap<>();
			stringToId = new HashMap<>();
			List<String> orderedCodes = codes.stream().sorted().collect(Collectors.toList());
			for(short i = 0;i<orderedCodes.size();i++ ){
				idToString.put(i, orderedCodes.get(i));
				stringToId.put(orderedCodes.get(i), i);
			}
		} catch (IOException e) {
			assert(false);
			throw new AssertionError("Error when initializing static variables of class "+AnnotatedEventType.class,e);
		}
	}*/
	
	//internally we save the id as a short and the change as a byte to save memory!
	private short companyID;
	private byte change;
	
	public static Set<AnnotatedEventType> loadEventAlphabet(Set<String> allCompanyIDS) throws IOException{
		Set<AnnotatedEventType> eventAlphabet = new HashSet<>();
		for(String id : allCompanyIDS ){
			for(Change change : Change.values()){
				eventAlphabet.add(new AnnotatedEventType(id, change));
			}
		}
		return eventAlphabet;
	}
	
	public AnnotatedEventType(String companyID, Change change){
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
		if(!( o instanceof AnnotatedEventType)){
			return false;
		} else{
			AnnotatedEventType other = (AnnotatedEventType) o;
			return companyID == other.companyID && change==other.change;
		}
	}
	
	@Override
	public int hashCode(){
		return new Short(companyID).hashCode()*new Byte(change).hashCode()*7;
	}

	@Override
	public int compareTo(AnnotatedEventType o) {
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

	public AnnotatedEventType getInverseEvent() {
		return new AnnotatedEventType(idToString.get(companyID), getChange().getInverse());
	}
	
	@Override
	public String toString(){
		return idToString.get(companyID) + "_" + getChange();
	}

	public static Set<AnnotatedEventType> loadEventAlphabet() throws IOException {
		return loadEventAlphabet(IOService.getAllCompanyCodes());
	}
}
