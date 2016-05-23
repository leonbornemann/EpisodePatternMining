package semantic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;

import reallife_data.finance.yahoo.stock.util.IOService;
import util.Pair;

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
		return filteredResults.stream().map(e -> e.getSecond()).collect(Collectors.toSet());
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
