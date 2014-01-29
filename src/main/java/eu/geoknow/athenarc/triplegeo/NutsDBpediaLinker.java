/**
 * 
 */
package eu.geoknow.athenarc.triplegeo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.disambiguation.UriResolver;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class NutsDBpediaLinker {

	private Model model;
	private List<String> skip = Arrays.asList("DE80I", "DE80H", "DE80G", "DE80F", "DE80E", "DE80D", "DE80C", "DE80B", "DE80A", "DE809", "DE808", "DE807", "DE806", "DE805", "DE802", "DE801");

	public NutsDBpediaLinker(Model model) {
		
		this.model = model;
		for ( int i = 0 ; i < skip.size(); i++) skip.set(i, "http://nuts.geovocab.org/id/" + skip.get(i));
	}
	
	/**
	 * select * FROM <http://geostats.aksw.org> { OPTIONAL { ?y owl:sameAs ?s . FILTER(regex(?y, "^http://nuts", "i")) } OPTIONAL { ?s owl:sameAs ?o } . OPTIONAL { ?s owl:sameAs ?x } } ORDER BY ?o
	 */
	public void linkDistricts() {
		
		System.out.println("linking districts");
		
		Map<String,String> nutsUriToDbpediaUri = new HashMap<>();
		String sparql = "SELECT ?district ?label { ?district <http://www.w3.org/2000/01/rdf-schema#label> ?label . " + 
							" ?district <http://rdfdata.eionet.europa.eu/ramon/ontology/level> \"3\" . }";
		QueryExecution qe = QueryExecutionFactory.create(sparql, this.model);
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext()) {
			
			QuerySolution qs = rs.next();
			String nutsDistrictUri		= qs.get("district").asResource().getURI();
			String nutsDistrictLabel	= qs.get("label").asLiteral().getLexicalForm();
			
			if ( skip.contains(nutsDistrictUri) ) continue;
			String dbpediaUri = UriResolver.getInstance().getUri(nutsDistrictLabel, "Landkreis " + nutsDistrictLabel);
			dbpediaUri = UriResolver.getInstance().resolveRedirect(dbpediaUri);
			
			if ( dbpediaUri == null || dbpediaUri.isEmpty() ) System.out.println(nutsDistrictLabel + " > " + dbpediaUri);
			
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DED52") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Leipzig";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE123") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Karlsruhe";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE118") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Heilbronn";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE734") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Kassel";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE80J") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Mecklenburgische_Seenplatte";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE94A") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Friesland";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE94D") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Oldenburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE943") ) dbpediaUri = "http://de.dbpedia.org/resource/Oldenburg_(Oldenburg)";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE80M") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Nordwestmecklenburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEC01") ) dbpediaUri = "http://de.dbpedia.org/resource/Regionalverband_Saarbrücken";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE261") ) dbpediaUri = "http://de.dbpedia.org/resource/Aschaffenburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE264") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Aschaffenburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE271") ) dbpediaUri = "http://de.dbpedia.org/resource/Augsburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE276") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Augsburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE241") ) dbpediaUri = "http://de.dbpedia.org/resource/Bamberg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE245") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Bamberg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE242") ) dbpediaUri = "http://de.dbpedia.org/resource/Bayreuth";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE246") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Bayreuth";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE243") ) dbpediaUri = "http://de.dbpedia.org/resource/Coburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE247") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Coburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE253") ) dbpediaUri = "http://de.dbpedia.org/resource/Fürth";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE258") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Fürth";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE244") ) dbpediaUri = "http://de.dbpedia.org/resource/Hof_(Saale)";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE249") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Hof";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEB32") ) dbpediaUri = "http://de.dbpedia.org/resource/Kaiserslautern";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEB3F") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Kaiserslautern";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE943") ) dbpediaUri = "http://de.dbpedia.org/resource/Oldenburg_(Oldenburg)";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE94D") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Oldenburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE221") ) dbpediaUri = "http://de.dbpedia.org/resource/Landshut";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE227") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Landshut";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE212") ) dbpediaUri = "http://de.dbpedia.org/resource/München";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE21H") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_München";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE944") ) dbpediaUri = "http://de.dbpedia.org/resource/Osnabrück";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE94E") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Osnabrück";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE222") ) dbpediaUri = "http://de.dbpedia.org/resource/Passau";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE228") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Passau";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE232") ) dbpediaUri = "http://de.dbpedia.org/resource/Regensburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE238") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Regensburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE213") ) dbpediaUri = "http://de.dbpedia.org/resource/Rosenheim";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE21K") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Rosenheim";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE262") ) dbpediaUri = "http://de.dbpedia.org/resource/Schweinfurt";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE26B") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Schweinfurt";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE263") ) dbpediaUri = "http://de.dbpedia.org/resource/Würzburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE26C") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Würzburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE146") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Biberach";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE946") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Ammerland";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE215") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Berchtesgadener_Land";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEB15") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Birkenfeld";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEE07") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Börde";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE235") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Cham";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE933") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Harburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEE09") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Harz";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEF06") ) dbpediaUri = "http://de.dbpedia.org/resource/Kreis_Herzogtum_Lauenburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE94C") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Leer";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE24C") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Lichtenfels";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEC03") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Neunkirchen";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE71C") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Offenbach";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE936") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Osterholz";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE928") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Schaumburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEF0D") ) dbpediaUri = "http://de.dbpedia.org/resource/Kreis_Segeberg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEF0E") ) dbpediaUri = "http://de.dbpedia.org/resource/Kreis_Steinburg";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DEF0F") ) dbpediaUri = "http://de.dbpedia.org/resource/Kreis_Stormarn";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE94G") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Wesermarsch";
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE40F") ) dbpediaUri = "http://de.dbpedia.org/resource/Landkreis_Prignitz";
			
//			System.out.println(nutsDistrictUri + " > " + dbpediaUri);
			
			nutsUriToDbpediaUri.put(nutsDistrictUri, dbpediaUri);
		}
		qe.close();
		for ( Map.Entry<String, String> entry : nutsUriToDbpediaUri.entrySet() ) {
			
			UriResolver.getInstance().queryExtra(entry.getValue(), "district", model);
			model.add(ResourceFactory.createResource(entry.getKey()), OWL.sameAs, ResourceFactory.createResource(entry.getValue()));
		}
	}
	
	public void linkAdministrativeDistricts() {
		
		System.out.println("linking adminstrative districts");
		
		Map<String,String> nutsUriToDbpediaUri = new HashMap<>();
		String sparql = "SELECT ?district ?label { ?district <http://www.w3.org/2000/01/rdf-schema#label> ?label . " + 
							" ?district <http://rdfdata.eionet.europa.eu/ramon/ontology/level> \"2\" . }";
		QueryExecution qe = QueryExecutionFactory.create(sparql, this.model);
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext()) {
			
			QuerySolution qs = rs.next();
			String nutsDistrictUri		= qs.get("district").asResource().getURI();
			String nutsDistrictLabel	= qs.get("label").asLiteral().getLexicalForm();
			
			String dbpediaUri = UriResolver.getInstance().getUri("Regierungsbezirk " + nutsDistrictLabel, nutsDistrictLabel);
			
			if ( dbpediaUri == null || dbpediaUri.isEmpty() ) System.out.println(nutsDistrictLabel + " > " + dbpediaUri);
			
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE50") ) dbpediaUri = "http://de.dbpedia.org/resource/Freie_Hansestadt_Bremen";
			
//			System.out.println(nutsDistrictUri + " " + dbpediaUri);
			
			nutsUriToDbpediaUri.put(nutsDistrictUri, dbpediaUri);
		}
		qe.close();
		for ( Map.Entry<String, String> entry : nutsUriToDbpediaUri.entrySet() ) {
			
			UriResolver.getInstance().queryExtra(entry.getValue(), "adminDistrict", model);
			model.add(ResourceFactory.createResource(entry.getKey()), OWL.sameAs, ResourceFactory.createResource(entry.getValue()));
		}
	}
	
	public void linkFederalStates() {
		
		System.out.println("linking federal states");
		
		Map<String,String> nutsUriToDbpediaUri = new HashMap<>();
		String sparql = "SELECT ?district ?label { ?district <http://www.w3.org/2000/01/rdf-schema#label> ?label . " + 
							" ?district <http://rdfdata.eionet.europa.eu/ramon/ontology/level> \"1\" . }";
		QueryExecution qe = QueryExecutionFactory.create(sparql, this.model);
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext()) {
			
			QuerySolution qs = rs.next();
			String nutsDistrictUri		= qs.get("district").asResource().getURI();
			String nutsDistrictLabel	= qs.get("label").asLiteral().getLexicalForm();
			
			String dbpediaUri = UriResolver.getInstance().getUri(nutsDistrictLabel, nutsDistrictLabel);
			if ( dbpediaUri == null || dbpediaUri.isEmpty() ) System.out.println(nutsDistrictLabel + " > " + dbpediaUri);
			
			if ( nutsDistrictUri.equals("http://nuts.geovocab.org/id/DE5") ) dbpediaUri = "http://de.dbpedia.org/resource/Freie_Hansestadt_Bremen";
			
			nutsUriToDbpediaUri.put(nutsDistrictUri, dbpediaUri);
		}
		qe.close();
		for ( Map.Entry<String, String> entry : nutsUriToDbpediaUri.entrySet() ) {
			
			UriResolver.getInstance().queryExtra(entry.getValue(), "state", model);
			model.add(ResourceFactory.createResource(entry.getKey()), OWL.sameAs, ResourceFactory.createResource(entry.getValue()));
		}
	}
}
