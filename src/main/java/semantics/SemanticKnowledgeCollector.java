package semantics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;

import util.IOService;
import util.Pair;

/***
 * In theory this class uses the SPARQL-endpoint to connect to the dbpedia and retrieve the ids of those companies that have an entry in the dbpedia.
 * However to speed things up and allow execution without internet connections, the 40 companies that were returned in the 21st of august are hardcoded here and simply returned.
 * @author Leon Bornemann
 *
 */
public class SemanticKnowledgeCollector {

	private Set<String> allCompanyCodes;
	private List<Pair<String, String>> filteredResults;
	
	public SemanticKnowledgeCollector() throws IOException {
		allCompanyCodes = IOService.getAllCompanyCodes();
		String service = "http://dbpedia.org/sparql";
		//long and lat:
		String sparqlQuery = linkedDataQuery();
		QueryExecution qe = QueryExecutionFactory.sparqlService(service,sparqlQuery);
		ResultSet resultSet = qe.execSelect();
		List<Pair<String,String>> results = new ArrayList<>();
		resultSet.forEachRemaining(e -> results.add(new Pair<>(e.get("comp").toString(),e.get("sym").toString())));
		filteredResults = results.stream().filter( e -> containsCompanySymbol(e)).map(e -> new Pair<>(e.getFirst(),getSymbol(e.getSecond()))).collect(Collectors.toList());
	}
	
	public Set<String> getAnnotatedCompanyCodes(){
		//return filteredResults.stream().map(e -> e.getSecond()).collect(Collectors.toSet());
		//no need to do the query all the time, since we know the result:
		Set<String> codes = new HashSet<>();
		codes.addAll(Arrays.asList("CERN","AAPL","EYES","CSCO","ELNK","OPTT","SNDK","YHOO","TXN","HEAR","MSFT","CGEN","QCOM","STX",
				"ISSC","CTSH","KE","Z","ACOR","EA","GNCMA","ALSK","SP","OHGI","ON","GOOG","IBKR","VOD","VA","AUDC","TECH","CMPR","EBAY","INTC","FCS","LE","NLST","NK","CSTE","SODA"));
		return codes;
	}
	
	public Set<String> getSectorCodes(){
		Set<String> codes = new HashSet<>();
		codes.addAll(Arrays.asList("Miscellaneous","Finance","Transportation","Consumer Services","Capital Goods","Public Utilities",
				"Basic Industries","Health Care","Energy","Consumer Durables","Technology","Consumer Non-Durables"));
		return codes;
	}

	private String getSymbol(String longCode) {
		for(String code : allCompanyCodes){
			if(longCode.toUpperCase().contains(code.toUpperCase())){
				return code;
			}
		}
		assert(false);
		return null;
	}

	private boolean containsCompanySymbol(Pair<String, String> e) {
		for(String code : allCompanyCodes){
			if(e.getSecond().toUpperCase().contains(code.toUpperCase())){
				return true;
			}
		}
		return false;
	}

	private String linkedDataQuery() {
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\r\n"
				+ "select * where {"
				+ "?comp <http://dbpedia.org/property/symbol> ?sym ."
				+ "?comp rdf:type <http://dbpedia.org/ontology/Company> ."
			+ "}";
	}

}
