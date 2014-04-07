/**
 * 
 */
package org.aksw.geostats.disambiguation;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.aksw.geostats.rdf.RdfExport;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class UriResolver {

	private static UriResolver INSTANCE = null;
	private static QueryExecutionFactory deQef = new QueryExecutionFactoryHttp("http://de.dbpedia.org/sparql");
	private static QueryExecutionFactory enQef = new QueryExecutionFactoryHttp("http://lod.openlinksw.com/sparql");
	private CacheFrontend enCacheFrontend = null;
	private CacheFrontend deCacheFrontend = null;
	Map<String,String> oldToNew = new HashMap<>();
	
	
	/**
	 * select * FROM <http://geostats.aksw.org> { ?s <http://dbpedia.org/ontology/federalState> ?state . OPTIONAL { ?s <http://dbpedia.org/ontology/thumbnail> ?o } }
	 */
	private UriResolver() {
		
		oldToNew.put("Regierungsbezirk Luneburg", 			"Regierungsbezirk Lüneburg");
		oldToNew.put("Regierungsbezirk Munster", 			"Regierungsbezirk Münster");
		oldToNew.put("Regierungsbezirk Berlin", 			"Berlin");
		oldToNew.put("Regierungsbezirk Magdeburg", 			"Bezirk Magdeburg");
		
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
		oldToNew.put("Burgenland (D)", 						"Burgenlandkreis");
		oldToNew.put("Friesland (D)", 						"Landkreis Friesland");
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
		oldToNew.put("Salzland", 							"Salzlandkreis");
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
			
			this.enCacheFrontend = new CacheFrontendImpl(CacheCoreH2.create("en-dbpedia", 150l * 60l * 60l * 1000l, false));
			this.deCacheFrontend = new CacheFrontendImpl(CacheCoreH2.create("de-dbpedia", 150l * 60l * 60l * 1000l, false));
			enQef = new QueryExecutionFactoryCacheEx(enQef, enCacheFrontend);
			enQef = new QueryExecutionFactoryRetry(enQef, 5, 3000);
			deQef = new QueryExecutionFactoryCacheEx(deQef, deCacheFrontend);
			deQef = new QueryExecutionFactoryRetry(deQef, 5, 3000);
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
	
	public String resolveRedirect(String dbpediaUri) {
		
		String query = String.format("SELECT ?redirect FROM <http://de.dbpedia.org> { <%s> <http://dbpedia.org/ontology/wikiPageRedirects> ?redirect }", dbpediaUri);
        ResultSet rs = deQef.createQueryExecution(query).execSelect();
        
        while (rs.hasNext()) {
        	
        	QuerySolution result = rs.next();
        	String uri = result.getResource("redirect").getURI();
        	
//        	System.out.println(dbpediaUri + " " + uri);
        	
        	return uri;
        }
        
        return dbpediaUri;
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
        
//        System.out.println("no URI found for label: " + label);
        return "";
	}
	
	public void queryExtra(String uri, String type, Model globalModel){
		
		Model model = ModelFactory.createDefaultModel();
		
		System.out.println(uri);
		
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
				globalModel.add(model);
				
				break;
			}
			case "state": {
				
				Resource stateUri = ResourceFactory.createResource(uri);
				addTypes(stateUri, model, "PopulatedPlace", "FederalState", "Place");
				
				model.add(deQef.createQueryExecution(getQuery1(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery11(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery2(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery3(uri,uri)).execConstruct());
				
				RdfExport.write(model, "data/tmp/"+URLEncoder.encode(uri.replace("http://de.dbpedia.org/resource/", ""))+".ttl");
				globalModel.add(model);
				
				break;
			}
			case "adminDistrict" : {
				
				Resource adminDistrictUri = ResourceFactory.createResource(uri);
				addTypes(adminDistrictUri, model, "PopulatedPlace", "AdministrativeDistrict", "Place", "Settlement");
				model.add(deQef.createQueryExecution(getQuery1(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery11(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery2(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery3(uri,uri)).execConstruct());
				
				RdfExport.write(model, "data/tmp/"+URLEncoder.encode(uri.replace("http://de.dbpedia.org/resource/", ""))+".ttl");
				globalModel.add(model);
				
				break;
			}
			case "district" : {
				
				Resource districtUri = ResourceFactory.createResource(uri);
				addTypes(districtUri, model, "PopulatedPlace", "District", "Place");
				
				model.add(deQef.createQueryExecution(getQuery1(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery11(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery2(uri,uri)).execConstruct());
				model.add(deQef.createQueryExecution(getQuery3(uri,uri)).execConstruct());
				
				if ( uri.equals("http://de.dbpedia.org/resource/Städteregion_Aachen") ) 
					model.add(ResourceFactory.createResource(uri), OWL.sameAs, ResourceFactory.createResource("http://dbpedia.org/resource/Aachen_(district)"));
				
				NodeIterator listObjectsOfProperty = model.listObjectsOfProperty(OWL.sameAs);
				while (listObjectsOfProperty.hasNext()) {
					
					String enUri = listObjectsOfProperty.next().asResource().getURI();
					String deUri = districtUri.getURI();
					model.add(enQef.createQueryExecution(getQuery1(deUri, enUri)).execConstruct());
					model.add(enQef.createQueryExecution(getQuery11(deUri, enUri)).execConstruct());
					model.add(enQef.createQueryExecution(getQuery2(deUri, enUri)).execConstruct());
					model.add(enQef.createQueryExecution(getQuery3(deUri, enUri)).execConstruct());
				}
				
				RdfExport.write(model, "data/tmp/"+URLEncoder.encode(uri.replace("http://de.dbpedia.org/resource/", ""))+".ttl");
						
				globalModel.add(model);
				
				break;
			}
			default: System.out.println("Unknown type specified: " + type); break;
		}
	}
	
	/**
	 * 
	 * @param adminDistrictUri
	 * @param model
	 * @param types
	 */
	private void addTypes(Resource adminDistrictUri, Model model, String ... types) {
		
		for (String type : types) model.add(adminDistrictUri, RDF.type, ResourceFactory.createResource("http://dbpedia.org/ontology/"+type));
	}

	private String getQuery1(String newUri, String oldUri) {
		return  "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
				"CONSTRUCT { \n" +
					"\t<" + newUri + "> dbo:thumbnail ?thumbnail . \n" + 
//					"\t<" + newUri + "> rdfs:comment ?comment . \n" +
					"\t<" + newUri + "> dbo:abstract  ?abstract . \n" +
					"\t<" + newUri + "> rdfs:label ?label . \n" +
					"\t<" + newUri + "> owl:sameAs ?sameAs . \n" +
//					"\t<" + newUri + "> dbo:areaCode ?areaCode . \n" + 
//					"\t<" + newUri + "> dbo:areaTotal ?areaTotal . \n" + 
//					"\t<" + newUri + "> dbo:elevation ?elevation . \n" +
//					"\t<" + newUri + "> dbo:municipalityCode ?municipalityCode . \n" + 
//					"\t<" + newUri + "> dbo:postalCode ?postalCode . \n" + 
//					"\t<" + newUri + "> dbo:vehicleCode ?vehicleCode . \n" + 
//					"\t<" + newUri + "> dbo:populationTotal ?population . \n" +
//					"\t<" + newUri + "> foaf:homepage ?homepage . \n" +
//					"\t<" + newUri + "> dbo:capital ?capital . \n" +
//					"\t<" + newUri + "> rdfs:label ?capitalLabel . \n" +
				"} \n" +
				"WHERE { \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:thumbnail ?thumbnail  } \n" +
//					"\tOPTIONAL { <" + oldUri + "> rdfs:comment ?comment . FILTER(LANGMATCHES(lang(?comment), 'de') || LANG(?comment) = '') } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:abstract ?abstract . FILTER(LANGMATCHES(lang(?abstract), 'de') || LANG(?abstract) = '')}  \n" +
					"\tOPTIONAL { <" + oldUri + "> rdfs:label ?label . FILTER(LANGMATCHES(lang(?label), 'de')  || LANG(?label) = '')  } \n" +
					"\tOPTIONAL { <" + oldUri + "> owl:sameAs ?sameAs . FILTER (regex(?sameAs, '^http://dbpedia.org/resource/', 'i')) } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:areaCode ?areaCode } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:areaTotal ?areaTotal } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:elevation ?elevation } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:municipalityCode ?municipalityCode } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:postalCode ?postalCode } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:vehicleCode ?vehicleCode } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:populationTotal ?population } \n" +
//					"\tOPTIONAL { <" + oldUri + "> foaf:homepage ?homepage } \n" +
//					"\tOPTIONAL { <" + oldUri + "> dbo:capital ?capital . ?capital rdfs:label ?capitalLabel . FILTER(LANGMATCHES(lang(?capitalLabel), 'de') || LANG(?capitalLabel) = '') } \n" +
				"}";
	}
	
	private String getQuery11(String newUri, String oldUri) {
		return  "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
				"CONSTRUCT { \n" +
					"\t<" + newUri + "> dbo:areaCode ?areaCode . \n" + 
					"\t<" + newUri + "> dbo:areaTotal ?areaTotal . \n" + 
					"\t<" + newUri + "> dbo:elevation ?elevation . \n" +
					"\t<" + newUri + "> dbo:municipalityCode ?municipalityCode . \n" + 
					"\t<" + newUri + "> dbo:postalCode ?postalCode . \n" + 
					"\t<" + newUri + "> dbo:vehicleCode ?vehicleCode . \n" + 
					"\t<" + newUri + "> dbo:populationTotal ?population . \n" +
					"\t<" + newUri + "> foaf:homepage ?homepage . \n" +
					"\t<" + newUri + "> dbo:capital ?capital . \n" +
					"\t<" + newUri + "> rdfs:label ?capitalLabel . \n" +
				"} \n" +
				"WHERE { \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:areaCode ?areaCode } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:areaTotal ?areaTotal } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:elevation ?elevation } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:municipalityCode ?municipalityCode } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:postalCode ?postalCode } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:vehicleCode ?vehicleCode } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:populationTotal ?population } \n" +
					"\tOPTIONAL { <" + oldUri + "> foaf:homepage ?homepage } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:capital ?capital . ?capital rdfs:label ?capitalLabel . FILTER(LANGMATCHES(lang(?capitalLabel), 'de') || LANG(?capitalLabel) = '') } \n" +
				"}";
	}
	
	private String getQuery2(String newUri, String oldUri) {
		return  "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
				"CONSTRUCT { \n" +
					"\t<" + newUri + "> dbo:leader ?leader . \n" +
					"\t?leader a foaf:Person . \n" +
					"\t?leader rdfs:label ?leaderLabel . \n" + 
					"\t?leader dbo:birthDate ?date . \n" + 
					"\t?leader dbo:birthPlace ?place . \n" + 
					"\t?place rdfs:label ?placeLabel . \n" +
					"\t?leader dbo:abstract ?abstract . \n" + 
					"\t?leader dbo:thumbnail ?leaderThumb . \n" + 
					"\t?leader dbo:party ?leaderParty . \n" + 
					"\t?leaderParty rdfs:label ?partyLabel . \n" + 
					"\t?leaderParty dbo:thumbnail ?partyThumb . \n" +
				"} \n" +
				"WHERE { \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leader ?leader . ?leader rdfs:label ?leaderLabel . } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leader ?leader . ?leader dbo:birthDate ?date } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leader ?leader . ?leader dbo:thumbnail ?leaderThumb } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leader ?leader . ?leader dbo:birthPlace ?place . ?place rdfs:label ?placeLabel FILTER(LANGMATCHES(lang(?placeLabel), 'de') || LANG(?placeLabel) = '') } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leader ?leader . ?leader dbo:abstract ?abstract } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leader ?leader . ?leader dbo:party ?leaderParty . ?leaderParty rdfs:label ?partyLabel . } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leader ?leader . ?leader dbo:party ?leaderParty . ?leaderParty dbo:thumbnail ?partyThumb } \n" +
				"}";
	}
	
	private String getQuery3(String newUri, String oldUri) {
		return  "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
				"CONSTRUCT { \n" +
					"\t<" + newUri + "> dbo:leader ?leader1 . \n" + 
					"\t?leader1 a foaf:Person . \n" +
					"\t?leader1 rdfs:label ?leaderLabel1 . \n" + 
					"\t?leader1 dbo:birthDate ?date1 . \n" + 
					"\t?leader1 dbo:birthPlace ?place1 . \n" + 
					"\t?place1 rdfs:label ?placeLabel1 . \n" + 
					"\t?leader1 dbo:abstract ?abstract1 . \n" + 
					"\t?leader1 dbo:thumbnail ?leaderThumb . \n" +
					"\t?leader1 dbo:party ?leaderParty1 . \n" + 
					"\t?leaderParty1 rdfs:label ?partyLabel1 . \n" + 
					"\t?leaderParty1 dbo:thumbnail ?partyThumb1 . \n" +
				"} \n" +
				"WHERE { \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leaderName ?leader1 . ?leader1 rdfs:label ?leaderLabel1 . } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leaderName ?leader1 . ?leader1 dbo:birthDate ?date1 } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leaderName ?leader1 . ?leader1 dbo:birthPlace ?place1 . ?place1 rdfs:label ?placeLabel1 FILTER(LANGMATCHES(lang(?placeLabel1), 'de') || LANG(?placeLabel1) = '')  } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leaderName ?leader1 . ?leader1 dbo:thumbnail ?leaderThumb } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leaderName ?leader1 . ?leader1 dbo:abstract ?abstract1 } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leaderName ?leader1 . ?leader1 dbo:party ?leaderParty1 . ?leaderParty1 rdfs:label ?partyLabel1 . } \n" +
					"\tOPTIONAL { <" + oldUri + "> dbo:leaderName ?leader1 . ?leader1 dbo:party ?leaderParty1 . ?leaderParty1 dbo:thumbnail ?partyThumb1 } \n" +
				"}";
	}

	private String repairLabel(String label) {
		
		if ( oldToNew.containsKey(label.replace("\n", "").replace("\r", "")) ) return oldToNew.get(label.replace("\n", "").replace("\r", ""));
		else return label;
	}
}
