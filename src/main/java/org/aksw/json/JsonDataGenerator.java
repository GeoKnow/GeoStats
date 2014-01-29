/**
 * 
 */
package org.aksw.json;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
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
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.FileManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class JsonDataGenerator {
	
	static QueryExecutionFactory qef;
	static String query = 
			"PREFIX gis: <http://www.opengis.net/ont/geosparql#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"SELECT ?s ?dbpedia { " +
//			"	?s gis:hasGeometry ?geo . " +
			"   ?s <http://www.w3.org/2002/07/owl#sameAs> ?dbpedia . " +
			"   ?dbpedia rdf:type <RDF:TYPE> . " +
//			"   ?geo gis:asWKT ?wkt " +
			"}";
	
	static {
		
		try {
			
			Model data = FileManager.get().loadModel("data/geostats.ttl", "TURTLE");
//		    Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//		    infmodel = ModelFactory.createInfModel(reasoner, data);
			
			QueryExecutionFactory sparql = new QueryExecutionFactoryModel(data);
			CacheEx cache = new CacheExImpl(CacheCoreH2.create("localhost", 150l * 60l * 60l * 1000l, false));
			qef = new QueryExecutionFactoryCacheEx(sparql, cache);
		}
		catch ( Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
		
		JsonDataGenerator.generate();
	}
	
	private static void getFederalStates(JSONObject json) throws ParseException, JSONException {
		
		QueryExecution qExec = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/FederalState"));
		ResultSet rs = qExec.execSelect();
		Set<String> done = new HashSet<>();
		
//		ResultSetFormatter.out(rs);
		
		int i = 1;
        while (rs.hasNext()) {
        	
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("dbpedia").asResource().getURI();
        	
        	if ( done.contains(uri) ) continue;
        	done.add(uri);
        	
        	String label		 = getLabel(uri);
        	String url		 	 = getUrl(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
        	List<Geometry> geos  = getGeometries(result.get("s").asResource().getURI());	
//        	String kinderGarten	 = getKindergartenCount(uri);
        	
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
        	area.put("url", url);
//        	area.put("kinderGarten", kinderGarten);
        	
        	json.getJSONArray("federalState").put(area);
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
		
		Set<String> done = new HashSet<>();
		int i = 1;
        while (rs.hasNext()) {
        	
        	System.out.println("AD " + i++);
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("dbpedia").asResource().getURI();
        	if ( done.contains(uri) ) continue;
        	done.add(uri);
        	String label		 = getLabel(uri);
        	String url		 	 = getUrl(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
        	List<Geometry> geos  = getGeometries(result.get("s").asResource().getURI());		
//        	String kinderGarten	 = getKindergartenCount(uri);
        	
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
        	area.put("url", url);
//        	area.put("kinderGarten", kinderGarten);
        	
        	json.getJSONArray("adminstrativeDistricts").put(area);
        }
	}

	private static void getDistricts(JSONObject json) throws ParseException, JSONException {
		
		QueryExecution qExec = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/District")); 
		ResultSet rs = qExec.execSelect();
		Set<String> done = new HashSet<>();
		
		int i = 1;
        while (rs.hasNext()) {
        	
        	System.out.println("DIS: " + i++);
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("dbpedia").asResource().getURI();
        	if ( done.contains(uri) ) continue;
        	if ( !uri.contains("Nordwestmecklenburg"))continue;
        	done.add(uri);
        	String label		 = getLabel(uri);
        	String url		 	 = getUrl(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
        	List<Geometry> geos  = getGeometries(result.get("s").asResource().getURI());	
//        	String kinderGarten	 = getKindergartenCount(uri);
        	
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
        	
        	System.out.println(multipolygon.toString());
        	
        	// some uris appear more then once (if they consist of multipolygons)
        	JSONObject area = new JSONObject();
        	area.put("uri", uri);
        	area.put("url", url);
        	area.put("sgeo", multipolygon);
        	area.put("label", label);
        	area.put("comment", comment);
        	area.put("img", img);
//        	area.put("kinderGarten", kinderGarten);
        	
        	json.getJSONArray("districts").put(area);
        }
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	private static String getKindergartenCount(String uri) {
		
		Integer maxCount = Integer.MIN_VALUE;
		Map<String,Integer> maxCounts = new HashMap<>();
		if ( !maxCounts.containsKey("numberOfKindergarten") ) {
			
			ResultSet rs = qef.createQueryExecution(QueryFactory.create("SELECT MAX(?kindergartenCount) { ?s <http://geostats.aksw.org/numberOfKindergarten> ?kindergartenCount }", Syntax.syntaxARQ)).execSelect();
			while ( rs.hasNext()) maxCount = rs.next().get("callret-0").asLiteral().getInt();
			maxCounts.put("numberOfKindergarten", maxCount);
		}
		maxCount = maxCounts.get("numberOfKindergarten");
		
		ResultSet rs = qef.createQueryExecution(String.format("SELECT ?kindergartenCount { <%s> <http://geostats.aksw.org/numberOfKindergarten> ?kindergartenCount }", uri)).execSelect();
		while ( rs.hasNext()) return String.format("%.4f", rs.next().get("kindergartenCount").asLiteral().getInt() / (double) maxCount).replace(",", ".");
		
		System.out.println("No kindergarten count for uri: " + uri);
		
		return "0.0";
	}

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
		
		ResultSet rs = qef.createQueryExecution(String.format("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?comment { <%s> rdfs:comment ?comment }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("comment").asLiteral().getLexicalForm();
		
		return "no comment";
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 */
	private static String getLabel(String uri) {
		
		ResultSet rs = qef.createQueryExecution(String.format("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?label { <%s> rdfs:label ?label }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("label").asLiteral().getLexicalForm();
		
		return "no label";
	}

	public static void generate() throws JSONException, ParseException, IOException {
		
		JSONObject json = new JSONObject();
		json.put("districts", new JSONArray());
		json.put("adminstrativeDistricts", new JSONArray());
		json.put("federalState", new JSONArray());
		
		// getting all districts
		getDistricts(json);
		getAdminstrativeDistricts(json);
		getFederalStates(json);
		
        String output = json.toString(); 
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement je = new JsonParser().parse(json.toString());
		output = gson.toJson(je);
        
        FileUtils.write(new File("/Users/gerb/Development/workspaces/java/geostats/gui/data/geometries.json"), output , "UTF-8");
	}
}
