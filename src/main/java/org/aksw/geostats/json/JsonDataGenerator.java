/**
 * 
 */
package org.aksw.geostats.json;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.geostats.datacube.DataSet;
import org.aksw.geostats.datacube.Language;
import org.aksw.geostats.datacube.observation.Observation;
import org.aksw.geostats.datacube.observation.ObservationValue;
import org.aksw.geostats.datacube.property.MeasureProperty;
import org.aksw.geostats.datacube.rdf.RdfToDataCube;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class JsonDataGenerator {
	
	static QueryExecutionFactory qef;
	static Map<String,DataSet> datasets;
	static String query = 
			"PREFIX gis: <http://www.opengis.net/ont/geosparql#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
			"PREFIX geostats: <http://geostats.aksw.org/> " +
			"SELECT ?s ?dbpedia ?regionalStatistik { " +
			"   ?s <http://www.w3.org/2002/07/owl#sameAs> ?dbpedia . " +
			"   ?dbpedia rdf:type <RDF:TYPE> . " +
//			"   FILTER NOT EXISTS { ?dbpedia geostats:regionalStatistikId ?regionalStatistik } " +
			"   ?dbpedia geostats:regionalStatistikId ?regionalStatistik . " +
			"} " +
			"ORDER BY ?dbpedia";
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
		
		Model model = FileManager.get().loadModel("data/sparqlify/insolvenzen/insolvenzen-kreisebene-325-31-4.ttl", "TURTLE");
		model.add(FileManager.get().loadModel("data/sparqlify/bevoelkerung/bevoelkerung-kreisebene-173-01-4.ttl", "TURTLE"));
		datasets = RdfToDataCube.read(RdfToDataCube.createQueryExecutionFactory(model));
		
		JsonDataGenerator.generate();
		
//		for ( String  s : JsonDataGenerator.values) System.out.println(s);
	}
	
	public static void generate() throws JSONException, ParseException, IOException, ClassNotFoundException, SQLException {
		
		Model data = FileManager.get().loadModel("data/geostats.ttl", "TURTLE");
		QueryExecutionFactory sparql = new QueryExecutionFactoryModel(data);
		CacheFrontend cache = new CacheFrontendImpl(CacheCoreH2.create("localhost", 150l * 60l * 60l * 1000l, false));
		qef = new QueryExecutionFactoryCacheEx(sparql, cache);
		
		JSONObject json = new JSONObject();
		json.put("districts", new JSONArray());
		json.put("administrativeDistricts", new JSONArray());
		json.put("federalStates", new JSONArray());
		
		// getting all districts
		getDistricts(json);
		getAdminstrativeDistricts(json);
		getFederalStates(json);
		
        String output = json.toString(); 
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		JsonElement je = new JsonParser().parse(json.toString());
//		output = gson.toJson(je);
//        
        FileUtils.write(new File("geostats-angular/app/data/geometries.json"), output , "UTF-8");
	}
	
	private static void getFederalStates(JSONObject json) throws ParseException, JSONException {
		
		QueryExecution qExec = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/FederalState"));
		
		ResultSet rs = qExec.execSelect();
		Set<String> done = new HashSet<>();
		
//		ResultSetFormatter.out(rs);
//		System.exit(0);
		
		int i = 1;
        while (rs.hasNext()) {
        	
        	System.out.println("FS: " + i++);
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("dbpedia").asResource().getURI();
        	String regionID	 	 = result.get("regionalStatistik").asLiteral().getLexicalForm();
        	
        	if ( done.contains(uri) ) continue;
        	done.add(uri);
        	
        	String label		 = getLabel(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
        	List<Geometry> geos  = getGeometries(result.get("s").asResource().getURI());	
        	
        	JSONArray multipolygon = new JSONArray();
        	for ( Geometry geo : geos ) {
        		
        		JSONArray points = new JSONArray();
        		for ( Coordinate p : geo.getCoordinates()) {
            		
            		JSONObject point = new JSONObject();
            		point.put("lat", String.format("%.4f", p.y).replace(",", "."));
            		point.put("lon", String.format("%.4f", p.x).replace(",", "."));
            		points.put(point);
            	}
        		multipolygon.put(points);
        	}
        	
        	// some uris appear more then once (if they consist of multipolygons)
        	JSONObject area = new JSONObject();
        	area.put("uri", uri);
        	area.put("sgeo", multipolygon);
        	area.put("label", label);
        	area.put("comment", comment);
        	area.put("img", img);
        	
        	json.getJSONArray("federalStates").put(area);
        }
	}

	private static List<Geometry> getGeometries(String string) throws ParseException {
		
		String query = 
				"PREFIX gis: <http://www.opengis.net/ont/geosparql#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"SELECT ?wkt { " +
				"	<"+string+"> gis:hasGeometry ?geo . " +
				"   ?geo gis:asWKT ?wkt " +
				"}";
		
		List<Geometry> geos = new ArrayList<>();
		ResultSet rs = qef.createQueryExecution(query).execSelect();
		while ( rs.hasNext()) {
			
			geos.add(new WKTReader().read(rs.next().get("wkt").asLiteral().getLexicalForm()));
		}
		
		return geos;
	}

	private static void getAdminstrativeDistricts(JSONObject json) throws ParseException, JSONException {
		
		QueryExecution qExec = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/AdministrativeDistrict"));
		ResultSet rs = qExec.execSelect();
		
//		ResultSetFormatter.out(rs);
//		System.exit(0);
		
		Set<String> done = new HashSet<>();
		int i = 1;
        while (rs.hasNext()) {
        	
        	System.out.println("AD " + i++);
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("dbpedia").asResource().getURI();
        	String regionID	 	 = result.get("regionalStatistik").asLiteral().getLexicalForm();
        	
        	if ( done.contains(uri) ) continue;
        	done.add(uri);
        	
        	String label		 = getLabel(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
        	List<Geometry> geos  = getGeometries(result.get("s").asResource().getURI());		
        	
        	JSONArray multipolygon = new JSONArray();
        	for ( Geometry geo : geos ) {
        		
        		JSONArray points = new JSONArray();
        		for ( Coordinate p : geo.getCoordinates()) {
            		
            		JSONObject point = new JSONObject();
            		point.put("lat", String.format("%.4f", p.y).replace(",", "."));
            		point.put("lon", String.format("%.4f", p.x).replace(",", "."));
            		points.put(point);
            	}
        		multipolygon.put(points);
        	}
        	
        	// some uris appear more then once (if they consist of multipolygons)
        	JSONObject area = new JSONObject();
        	area.put("uri", uri);
        	area.put("sgeo", multipolygon);
        	area.put("label", label);
        	area.put("comment", comment);
        	area.put("img", img);
        	
        	json.getJSONArray("administrativeDistricts").put(area);
        }
	}

	private static void getDistricts(JSONObject json) throws ParseException, JSONException {
		
		QueryExecution qExec = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/District"));
		System.out.println(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/District"));
		ResultSet rs = qExec.execSelect();
		Set<String> done = new HashSet<>();
		
//		ResultSetFormatter.out(rs);
//		System.exit(0);
		
		int i = 1;
        while (rs.hasNext()) {
        	
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("dbpedia").asResource().getURI();
        	String regionID	 	 = result.get("regionalStatistik").asLiteral().getLexicalForm();
        	
        	// avoid having dupliacte items like berlin (district, admin districit and federal state)
        	if ( done.contains(uri) ) continue;
        	done.add(uri);
        	
        	System.out.println("DIS: " + i++ + "  -> " + uri);
        	String label		 = getLabel(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
        	
        	JSONArray multipolygon = new JSONArray();
        	for ( Geometry geo : getGeometries(result.get("s").asResource().getURI()) ) {
        		
        		JSONArray points = new JSONArray();
        		for ( Coordinate p : geo.getCoordinates()) {
            		
            		JSONObject point = new JSONObject();
            		point.put("lat", String.format("%.4f", p.y).replace(",", "."));
            		point.put("lon", String.format("%.4f", p.x).replace(",", "."));
            		points.put(point);
            	}
        		multipolygon.put(points);
        	}
        	
        	// some uris appear more then once (if they consist of multipolygons)
        	JSONObject area = new JSONObject();
        	area.put("uri", uri);
        	area.put("sgeo", multipolygon);
        	area.put("label", label);
        	area.put("comment", comment);
        	area.put("img", img);
        	
        	json.getJSONArray("districts").put(area);
        }
	}

//	private static JSONObject getDataCubes(String id) throws JSONException {
//		
//		String uri = "http://geostats.aksw.org/qb/observation/2012_" + id;
//		JSONObject datasetsJson = new JSONObject();
//		
//		for ( DataSet set : datasets.values() ) {
//			
//			JSONObject dataset = new JSONObject();
//			dataset.put("name", set.getLabels().get(Language.de));
//			dataset.put("uri", set.getUri());
//			
//			JSONObject values = new JSONObject();
//			for ( Observation obs : set.getObservations(uri) ) {
//				for( Entry<MeasureProperty, ObservationValue> entry : obs.getValues().entrySet() ) {
//					values.put(entry.getKey().getUri(), entry.getValue().getValue());
//					
//					JsonDataGenerator.values.add(entry.getValue().getValue());
//				}
//			}
//			dataset.put("values", values);
//			datasetsJson.put(set.getUri(), dataset);
//		}
//		
//		return datasetsJson;
//	}
//
//	/**
//	 * 
//	 * @param uri
//	 * @return
//	 */
//	private static String getKindergartenCount(String uri) {
//		
//		Integer maxCount = Integer.MIN_VALUE;
//		Map<String,Integer> maxCounts = new HashMap<>();
//		if ( !maxCounts.containsKey("numberOfKindergarten") ) {
//			
//			ResultSet rs = qef.createQueryExecution(QueryFactory.create("SELECT MAX(?kindergartenCount) { ?s <http://geostats.aksw.org/numberOfKindergarten> ?kindergartenCount }", Syntax.syntaxARQ)).execSelect();
//			while ( rs.hasNext()) maxCount = rs.next().get("callret-0").asLiteral().getInt();
//			maxCounts.put("numberOfKindergarten", maxCount);
//		}
//		maxCount = maxCounts.get("numberOfKindergarten");
//		
//		ResultSet rs = qef.createQueryExecution(String.format("SELECT ?kindergartenCount { <%s> <http://geostats.aksw.org/numberOfKindergarten> ?kindergartenCount }", uri)).execSelect();
//		while ( rs.hasNext()) return String.format("%.4f", rs.next().get("kindergartenCount").asLiteral().getInt() / (double) maxCount).replace(",", ".");
//		
//		System.out.println("No kindergarten count for uri: " + uri);
//		
//		return "0.0";
//	}
//
	/**
	 * 
	 * @param uri
	 * @return
	 */
	private static String getImage(String uri) {
		ResultSet rs = qef.createQueryExecution(String.format("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?image { <%s> <http://dbpedia.org/ontology/thumbnail> ?image }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("image").asResource().getURI();
		
		return "no image";
	}
	

	private static String getUrl(String uri) {
		
		ResultSet rs = qef.createQueryExecution(String.format("PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?homepage { <%s> foaf:homepage ?homepage }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("homepage").asResource().getURI();
		
		return "no homepage";
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	private static String getComment(String uri) {
		
		ResultSet rs = qef.createQueryExecution(String.format("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?comment { <%s> <http://dbpedia.org/ontology/abstract> ?comment . FILTER(lang(?comment) = 'de') }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("comment").asLiteral().getLexicalForm();
		
		return "no comment";
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 */
	private static String getLabel(String uri) {
		
		ResultSet rs = qef.createQueryExecution(String.format("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?label { <%s> rdfs:label ?label . FILTER(lang(?label) = 'de') }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("label").asLiteral().getLexicalForm();
		
		return "no label";
	}
}
