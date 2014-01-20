/**
 * 
 */
package org.aksw.disambiguation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class UriResolver {

	private static UriResolver INSTANCE = null;
	private static QueryExecutionFactory deQef = new QueryExecutionFactoryHttp("http://de.dbpedia.org/sparql");
	private static QueryExecutionFactory enQef = new QueryExecutionFactoryHttp("http://de.dbpedia.org/sparql");
	private CacheEx enCacheFrontend = null;
	private CacheEx deCacheFrontend = null;
	Map<String,String> oldToNew = new HashMap<>();
	
	
	/**
	 * select * FROM <http://geostats.aksw.org> { ?s <http://dbpedia.org/ontology/federalState> ?state . OPTIONAL { ?s <http://dbpedia.org/ontology/thumbnail> ?o } }
	 */
	private UriResolver() {
		
		oldToNew.put("Regierungsbezirk Luneburg", 			"Regierungsbezirk Lüneburg");
		oldToNew.put("Regierungsbezirk Munster", 			"Regierungsbezirk Münster");
		oldToNew.put("Regierungsbezirk Berlin", 			"Berlin");
		
		oldToNew.put("Landkreis Bitburg-Prüm", 				"Eifelkreis Bitburg-Prüm");
		oldToNew.put("Landkreis Landsberg", 				"Landkreis Landsberg am Lech");
		oldToNew.put("Landkreis Lauenburg", 				"Kreis Herzogtum Lauenburg");
		oldToNew.put("Landkreis Lindau", 					"Landkreis Lindau (Bodensee)");
		oldToNew.put("Landkreis Lippe", 					"Kreis Lippe");
		oldToNew.put("Landkreis Mettmann", 					"Kreis Mettmann");
		oldToNew.put("Landkreis Minden-Lübbecke", 			"Kreis Minden-Lübbecke");
		oldToNew.put("Landkreis Mühldorf", 					"Landkreis Mühldorf am Inn");
		oldToNew.put("Landkreis Neustadt", 					"Landkreis Neustadt an der Waldnaab");
		oldToNew.put("Landkreis Nienburg", 					"Landkreis Nienburg/Weser");
		oldToNew.put("Landkreis Nordfriesland", 			"Kreis Nordfriesland");
		oldToNew.put("Landkreis Osterode", 					"Landkreis Osterode am Harz");
		oldToNew.put("Landkreis Ostholstein",				"Kreis Ostholstein");
		oldToNew.put("Landkreis Paderborn",					"Kreis Paderborn");
		oldToNew.put("Landkreis Pfaffenhofen",				"Landkreis Pfaffenhofen an der Ilm");
		oldToNew.put("Landkreis Pinneberg",					"Kreis Pinneberg");
		oldToNew.put("Landkreis Plön",						"Kreis Plön");
		oldToNew.put("Landkreis Recklinghausen",		 	"Kreis Recklinghausen");
		oldToNew.put("Landkreis Rendsburg-Eckernförde",		"Kreis Rendsburg-Eckernförde");
		oldToNew.put("Landkreis Rotenburg",					"Landkreis Rotenburg (Wümme)");
		oldToNew.put("Landkreis Sankt Wendel",				"Landkreis St. Wendel");
		oldToNew.put("Landkreis Schleswig-Flensburg",		"Kreis Schleswig-Flensburg");
		oldToNew.put("Landkreis Segeberg",					"Kreis Segeberg");
		oldToNew.put("Landkreis Soest",						"Kreis Soest");
		oldToNew.put("Landkreis Soltau-Fallingbostel",		"Landkreis Heidekreis");
		oldToNew.put("Landkreis Steinburg",					"Kreis Steinburg");
		oldToNew.put("Landkreis Steinfurt",					"Kreis Steinfurt");
		oldToNew.put("Landkreis Stormarn",					"Kreis Stormarn");
		oldToNew.put("Landkreis Unna",						"Kreis Unna");
		oldToNew.put("Landkreis Viersen",					"Kreis Viersen");
		oldToNew.put("Landkreis Wesel",						"Kreis Wesel");
		oldToNew.put("Landkreis Wunsiedel",					"Landkreis Wunsiedel im Fichtelgebirge");
		oldToNew.put("Landkreis Dillingen", 				"Landkreis Dillingen an der Donau");
		oldToNew.put("Landkreis Dithmarschen", 				"Kreis Dithmarschen");
		oldToNew.put("Landkreis Groß-Gerau", 				"Kreis Groß-Gerau");
		oldToNew.put("Landkreis Gütersloh", 				"Kreis Gütersloh");
		oldToNew.put("Landkreis Herford", 					"Kreis Herford");
		oldToNew.put("Landkreis Höxter", 					"Kreis Höxter");
		oldToNew.put("Luneburg", 							"Lüneburg");
		oldToNew.put("Cologne", 							"Köln");
		oldToNew.put("Ludwigshafen", 						"Ludwigshafen am Rhein");
		oldToNew.put("Mülheim", 							"Mülheim an der Ruhr");
		oldToNew.put("Münster", 							"Münster (Westfalen)");
		oldToNew.put("Oldenburg", 							"Oldenburg (Oldenburg)");
		oldToNew.put("Weiden", 								"Weiden in der Oberpfalz");
		oldToNew.put("Munster", 							"Münster");
		oldToNew.put("Frankenthal", 						"Frankenthal (Pfalz)");
		oldToNew.put("Munich",	 							"München");
		oldToNew.put("Halle",	 							"Halle (Saale)");
		oldToNew.put("Hof",	 								"Hof (Saale)");
		oldToNew.put("Kempten",	 							"Kempten (Allgäu)");
		oldToNew.put("Landau",	 							"Landau in der Pfalz");
		oldToNew.put("Landkreis Aachen",	 				"Städteregion Aachen");
		oldToNew.put("Landkreis Altenkirchen",				"Landkreis Altenkirchen (Westerwald)");
		oldToNew.put("Landkreis Bergstraße",	 			"Kreis Bergstraße");
		oldToNew.put("Bitburg-Prüm",	 					"Eifelkreis Bitburg-Prüm");
		oldToNew.put("Landkreis Coesfeld",	 				"Kreis Coesfeld");
		oldToNew.put("Nuremberg", 							"Nürnberg");
		oldToNew.put("Germany", 							"Deutschland");
		oldToNew.put("Heilbronn city", 						"Heilbronn");
		oldToNew.put("Koblenz Coblenz", 					"Koblenz");
		oldToNew.put("Speyer Spires", 						"Speyer");
		oldToNew.put("Frankfurt am Oder", 					"Frankfurt (Oder)");
		oldToNew.put("Landkreis Ennepe-Ruhr", 				"Ennepe-Ruhr-Kreis");
		oldToNew.put("Olpe", 								"Kreis Olpe");
		oldToNew.put("Siegen-Wittgenstein", 				"Kreis Siegen-Wittgenstein");
		oldToNew.put("Landkreis Alb-Donau", 				"Alb-Donau-Kreis");
		oldToNew.put("Landkreis Bodensee", 					"Bodenseekreis");
		oldToNew.put("Landkreis Lörrach Lörrach", 			"Lörrach");
		oldToNew.put("Landkreis Enz", 						"Enzkreis");
		oldToNew.put("Landkreis Hohenlohe", 				"Hohenlohekreis");
		oldToNew.put("Landkreis Main-Tauber", 				"Main-Tauber-Kreis");
		oldToNew.put("Landkreis Neustadt-Bad Windsheim",	"Landkreis Neustadt an der Aisch-Bad Windsheim");
		oldToNew.put("Landkreis Berlin", 					"Berlin");
		oldToNew.put("Landkreis Hamelin-Pyrmont", 			"Landkreis Hameln-Pyrmont");
		oldToNew.put("Landkreis Hanover", 					"Hannover");
		oldToNew.put("Landkreis Cleves", 					"Kreis Kleve");
		oldToNew.put("Landkreis Rhein-Kreis Neuss", 		"Rhein-Kreis Neuss");
		oldToNew.put("Landkreis Rhein-Sieg", 				"Rhein-Sieg-Kreis");
		oldToNew.put("Landkreis Rhein-Hunsrück", 			"Rhein-Hunsrück-Kreis");
		oldToNew.put("Landkreis Rhein-Lahn", 				"Rhein-Lahn-Kreis");
		oldToNew.put("Landkreis Saarpfalz", 				"Saarpfalz-Kreis");
		oldToNew.put("Landkreis Altmarkkreis Salzwedel", 	"Altmarkkreis Salzwedel");
		oldToNew.put("Landkreis Saale-Holzland", 			"Saale-Holzland-Kreis");
		oldToNew.put("Landkreis Saale-Orla", 				"Saale-Orla-Kreis");
		oldToNew.put("Landkreis Unstrut-Hainich", 			"Unstrut-Hainich-Kreis");
		
		try {
			
			this.enCacheFrontend = new CacheExImpl(CacheCoreH2.create("en-dbpedia", 150l * 60l * 60l * 1000l, false));
			this.deCacheFrontend = new CacheExImpl(CacheCoreH2.create("de-dbpedia", 150l * 60l * 60l * 1000l, false));
			enQef = new QueryExecutionFactoryCacheEx(enQef, enCacheFrontend);
			deQef = new QueryExecutionFactoryCacheEx(deQef, deCacheFrontend);
		}
		catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
		catch (SQLException e) {

			e.printStackTrace();
		}
	}
	
	public static UriResolver getInstance(){
		
		if ( INSTANCE == null ) INSTANCE = new UriResolver();
		return INSTANCE;
	}
	
	public String getUri(String label, String backupLabel) {
		
        String query = String.format("SELECT ?s FROM <http://de.dbpedia.org> { ?s <http://www.w3.org/2000/01/rdf-schema#label> \"\"\"%s\"\"\"@de }", repairLabel(label));
        ResultSet rs = deQef.createQueryExecution(query).execSelect();
        
        while (rs.hasNext()) {
        	
        	QuerySolution result = rs.next();
        	String uri = result.getResource("s").getURI();
        	
        	if ( uri.contains("Kategorie:") ) continue;
        	
        	return uri;
        }
        
        if ( backupLabel != null ) {
        	
        	query = String.format("SELECT ?s FROM <http://de.dbpedia.org> { ?s <http://www.w3.org/2000/01/rdf-schema#label> \"\"\"%s\"\"\"@de }", repairLabel(backupLabel));
            rs = deQef.createQueryExecution(query).execSelect();
            
            while (rs.hasNext()) {
            	
            	QuerySolution result = rs.next();
            	String uri = result.getResource("s").getURI();
            	
            	if ( uri.contains("Kategorie:") ) continue;
            	
            	return uri;
            }
        }
        
        System.out.println("no URI found for label: " + label);
        return "";
	}
	
	public void queryExtra(String uri, String type, Model model){
		
		switch ( type ) {
		
			case "country": {
				
				Resource countryUri = ResourceFactory.createResource(uri);
				
				model.add(countryUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/PopulatedPlace"));
				model.add(countryUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/Country"));
				model.add(countryUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/Place"));
				model.add(countryUri, RDFS.label, "Deutschland", "de");
				model.add(countryUri, RDFS.label, "Germany", "en");
				model.add(countryUri, RDFS.comment, "Deutschland ist ein föderalistischer Staat in Mitteleuropa. Die Bundesrepublik Deutschland ist gemäß ihrer "
						+ "Verfassung eine Republik, die aus den 16 deutschen Ländern gebildet wird. Sie ist ein freiheitlich-demokratischer und sozialer Rechtsstaat "
						+ "und stellt als Bundesstaat die jüngste Ausprägung des deutschen Nationalstaates dar. Bundeshauptstadt ist Berlin.", "de");
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/thumbnail"), 
						ResourceFactory.createResource("http://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Flag_of_Germany.svg/200px-Flag_of_Germany.svg.png"));
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/areaCode"), "+49");
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/areaTotal"), "357121410000.0", XSDDatatype.XSDdouble);
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/populationDensity"), "229.000000", XSDDatatype.XSDdouble);
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/populationTotal"), "81858000", XSDDatatype.XSDdouble);
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/topLevelDomain"), ".de", XSDDatatype.XSDstring);
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/vehicleCode"), "D", XSDDatatype.XSDstring);
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/capital"), ResourceFactory.createResource("http://dbpedia.org/resource/Berlin"));
				model.add(ResourceFactory.createResource("http://dbpedia.org/resource/Berlin"), RDFS.label, "Berlin", "de");
				model.add(ResourceFactory.createResource("http://dbpedia.org/resource/Berlin"), RDFS.label, "Berlin", "en");
				
				model.add(countryUri, ResourceFactory.createProperty("http://dbpedia.org/ontology/currency"), ResourceFactory.createResource("http://dbpedia.org/resource/Euro"));
				model.add(ResourceFactory.createResource("http://dbpedia.org/resource/Euro"), RDFS.label, "Euro", "de");
				model.add(ResourceFactory.createResource("http://dbpedia.org/resource/Euro"), RDFS.label, "Euro", "en");
				
				break;
			}
			case "state": {
				
				Resource stateUri = ResourceFactory.createResource(uri);
				
				model.add(stateUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/PopulatedPlace"));
				model.add(stateUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/AdministrativeRegion"));
				model.add(stateUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/Place"));
				
				String query = 
						"PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
						"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
						"CONSTRUCT { \n" +
							"\t<" + uri + "> dbo:thumbnail ?thumbnail . \n" + 
							"\t<" + uri + "> rdfs:comment ?comment . \n" +
							"\t<" + uri + "> rdfs:label ?label . \n" +
							"\t<" + uri + "> owl:sameAs ?sameAs . \n" +
							"\t<" + uri + "> dbo:populationTotal ?population . \n" + 
						"} \n" +
						"WHERE { \n" +
							"\tOPTIONAL { <" + uri + "> dbo:thumbnail ?thumbnail } \n" +
							"\tOPTIONAL { <" + uri + "> rdfs:comment ?comment } \n" +
							"\tOPTIONAL { <" + uri + "> rdfs:label ?label } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:populationTotal ?population } \n" +
							"\tOPTIONAL { <" + uri + "> owl:sameAs ?sameAs . FILTER (regex(?sameAs, '^http://dbpedia.org/resource/', 'i')) } \n" +
						"}";
				
				model.add(deQef.createQueryExecution(query).execConstruct());
				
				break;
			}
			case "adminDistrict" : {
				
				Resource adminDistrictUri = ResourceFactory.createResource(uri);
				
				model.add(adminDistrictUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/PopulatedPlace"));
				model.add(adminDistrictUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/Settlement"));
				model.add(adminDistrictUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/Place"));
				
				String query = 
						"PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
						"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
						"CONSTRUCT { \n" +
							"\t<" + uri + "> dbo:thumbnail ?thumbnail . \n" + 
							"\t<" + uri + "> rdfs:comment ?comment . \n" +
							"\t<" + uri + "> rdfs:label ?label . \n" +
							"\t<" + uri + "> owl:sameAs ?sameAs . \n" +
							"\t<" + uri + "> dbo:areaCode ?areaCode . \n" + 
							"\t<" + uri + "> dbo:areaTotal ?areaTotal . \n" + 
							"\t<" + uri + "> dbo:elevation ?elevation . \n" + 
							"\t<" + uri + "> dbo:leaderName ?leaderName . \n" + 
							"\t?leaderName rdfs:label ?name . \n" + 
							"\t?leaderName dbo:birthDate ?date . \n" + 
							"\t?leaderName dbo:birthPlace ?place . \n" + 
							"\t?leaderName dbo:abstract ?abstract . \n" + 
							"\t<" + uri + "> dbo:leaderParty ?leaderParty . \n" + 
							"\t?leaderParty rdfs:label ?party . \n" + 
							"\t<" + uri + "> dbo:municipalityCode ?municipalityCode . \n" + 
							"\t<" + uri + "> dbo:postalCode ?postalCode . \n" + 
							"\t<" + uri + "> dbo:vehicleCode ?vehicleCode . \n" + 
							"\t<" + uri + "> dbo:populationTotal ?population . \n" +
						"} \n" +
						"WHERE { \n" +
							"\tOPTIONAL { <" + uri + "> dbo:thumbnail ?thumbnail } \n" +
							"\tOPTIONAL { <" + uri + "> rdfs:comment ?comment } \n" +
							"\tOPTIONAL { <" + uri + "> rdfs:label ?label } \n" +
							"\tOPTIONAL { <" + uri + "> owl:sameAs ?sameAs . FILTER (regex(?sameAs, '^http://dbpedia.org/resource/', 'i')) } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:areaCode ?areaCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:areaTotal ?areaTotal } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:elevation ?elevation } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:leaderName ?leaderName } \n" +
							"\tOPTIONAL { ?leaderName rdfs:label ?name } \n" +
							"\tOPTIONAL { ?leaderName dbo:birthDate ?date } \n" +
							"\tOPTIONAL { ?leaderName dbo:birthPlace ?place } \n" +
							"\tOPTIONAL { ?leaderName dbo:abstract ?abstract } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:leaderParty ?leaderParty } \n" +
							"\tOPTIONAL { ?leaderParty rdfs:label ?party } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:municipalityCode ?municipalityCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:postalCode ?postalCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:vehicleCode ?vehicleCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:populationTotal ?population } \n" +
						"}";
				
				model.add(deQef.createQueryExecution(query).execConstruct());
				
				break;
			}
			case "district" : {
				
				Resource adminDistrictUri = ResourceFactory.createResource(uri);
				
				model.add(adminDistrictUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/PopulatedPlace"));
				model.add(adminDistrictUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/District"));
				model.add(adminDistrictUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/Place"));
				
				String query = 
						"PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
						"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
						"CONSTRUCT { \n" +
							"\t<" + uri + "> dbo:thumbnail ?thumbnail . \n" + 
							"\t<" + uri + "> rdfs:comment ?comment . \n" +
							"\t<" + uri + "> rdfs:label ?label . \n" +
							"\t<" + uri + "> owl:sameAs ?sameAs . \n" +
							"\t<" + uri + "> dbo:areaCode ?areaCode . \n" + 
							"\t<" + uri + "> dbo:areaTotal ?areaTotal . \n" + 
							"\t<" + uri + "> dbo:elevation ?elevation . \n" + 
							"\t<" + uri + "> dbo:leaderName ?leaderName . \n" + 
							"\t?leaderName rdfs:label ?name . \n" + 
							"\t?leaderName dbo:birthDate ?date . \n" + 
							"\t?leaderName dbo:birthPlace ?place . \n" + 
							"\t?leaderName dbo:abstract ?abstract . \n" + 
							"\t<" + uri + "> dbo:leaderParty ?leaderParty . \n" + 
							"\t?leaderParty rdfs:label ?party . \n" + 
							"\t<" + uri + "> dbo:municipalityCode ?municipalityCode . \n" + 
							"\t<" + uri + "> dbo:postalCode ?postalCode . \n" + 
							"\t<" + uri + "> dbo:vehicleCode ?vehicleCode . \n" + 
							"\t<" + uri + "> dbo:populationTotal ?population . \n" +
						"} \n" +
						"WHERE { \n" +
							"\tOPTIONAL { <" + uri + "> dbo:thumbnail ?thumbnail } \n" +
							"\tOPTIONAL { <" + uri + "> rdfs:comment ?comment } \n" +
							"\tOPTIONAL { <" + uri + "> rdfs:label ?label } \n" +
							"\tOPTIONAL { <" + uri + "> owl:sameAs ?sameAs . FILTER (regex(?sameAs, '^http://dbpedia.org/resource/', 'i')) } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:areaCode ?areaCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:areaTotal ?areaTotal } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:elevation ?elevation } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:leaderName ?leaderName } \n" +
							"\tOPTIONAL { ?leaderName rdfs:label ?name } \n" +
							"\tOPTIONAL { ?leaderName dbo:birthDate ?date } \n" +
							"\tOPTIONAL { ?leaderName dbo:birthPlace ?place } \n" +
							"\tOPTIONAL { ?leaderName dbo:abstract ?abstract } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:leaderParty ?leaderParty } \n" +
							"\tOPTIONAL { ?leaderParty rdfs:label ?party } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:municipalityCode ?municipalityCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:postalCode ?postalCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:vehicleCode ?vehicleCode } \n" +
							"\tOPTIONAL { <" + uri + "> dbo:populationTotal ?population } \n" +
						"}";
				
				model.add(deQef.createQueryExecution(query).execConstruct());
				
				break;
			}
			default: System.out.println("Unknown type specified: " + type); break;
		}
	}
	
	private String repairLabel(String label) {
		
		if ( oldToNew.containsKey(label.replace("\n", "").replace("\r", "")) ) return oldToNew.get(label.replace("\n", "").replace("\r", ""));
		else return label;
	}

	public static void main(String[] args) {
		
		String uri = UriResolver.getInstance().getUri("München", null);
		System.out.println(uri);
	}
}
