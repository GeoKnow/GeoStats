/**
 * 
 */
package org.aksw.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
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
			"SELECT ?s ?wkt FROM <http://geostats.aksw.org> { " +
			"	?s gis:hasGeometry ?geo . " +
			"   ?s rdf:type <RDF:TYPE> . " +
			"   ?geo gis:asWKT ?wkt " +
			"}";
	
	
	static {
		
		try {
			
			QueryExecutionFactory sparql = new QueryExecutionFactoryHttp("http://localhost:8890/sparql");
			CacheEx cache = new CacheExImpl(CacheCoreH2.create("localhost1", 150l * 60l * 60l * 1000l, false));
			qef = new QueryExecutionFactoryCacheEx(sparql, cache);
		}
		catch ( Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, JSONException, ParseException, IOException {
		
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

	private static void getFederalStates(JSONObject json) throws ParseException, JSONException {
		
		ResultSet rs = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/FederalState")).execSelect();
		
        while (rs.hasNext()) {
        	
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("s").asResource().getURI();
        	String label		 = getLabel(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
//        	String kinderGarten	 = getKindergartenCount(uri);
        	
        	// simplify geometry
        	Geometry geo = new WKTReader().read(result.get("wkt").asLiteral().getLexicalForm());
        	JSONArray points = new JSONArray();
        	for ( Coordinate p : TopologyPreservingSimplifier.simplify(geo, 0.01).getCoordinates()) {
        		
        		JSONObject point = new JSONObject();
        		point.put("lat", String.format("%.4f", p.y).replace(",", "."));
        		point.put("lon", String.format("%.4f", p.x).replace(",", "."));
        		points.put(point);
        	}
        	
        	// some uris appear more then once (if they consist of multipolygons)
        	JSONObject area = (JSONObject) (!json.has(uri) ? new JSONObject() : json.get(uri));
        	JSONArray multiplePolyongs = area.has("sgeo") ? area.getJSONArray("sgeo") : new JSONArray();
        	multiplePolyongs.put(points);
        	
        	area.put("uri", uri);
        	area.put("sgeo", multiplePolyongs);
        	area.put("label", label);
        	area.put("comment", comment);
        	area.put("img", img);
//        	area.put("kinderGarten", kinderGarten);
        	
        	if ( points.length() > 0 ) json.getJSONArray("federalState").put(area);
        }
	}

	private static void getAdminstrativeDistricts(JSONObject json) throws ParseException, JSONException {
		
		ResultSet rs = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/AdminstrativeDistrict")).execSelect();
		
        while (rs.hasNext()) {
        	
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("s").asResource().getURI();
        	String label		 = getLabel(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
//        	String kinderGarten	 = getKindergartenCount(uri);
        	
        	// simplify geometry
        	Geometry geo = new WKTReader().read(result.get("wkt").asLiteral().getLexicalForm());
        	JSONArray points = new JSONArray();
        	for ( Coordinate p : TopologyPreservingSimplifier.simplify(geo, 0.01).getCoordinates()) {
        		
        		JSONObject point = new JSONObject();
        		point.put("lat", String.format("%.4f", p.y).replace(",", "."));
        		point.put("lon", String.format("%.4f", p.x).replace(",", "."));
        		points.put(point);
        	}
        	
        	// some uris appear more then once (if they consist of multipolygons)
        	JSONObject area = (JSONObject) (!json.has(uri) ? new JSONObject() : json.get(uri));
        	JSONArray multiplePolyongs = area.has("sgeo") ? area.getJSONArray("sgeo") : new JSONArray();
        	multiplePolyongs.put(points);
        	
        	area.put("uri", uri);
        	area.put("sgeo", multiplePolyongs);
        	area.put("label", label);
        	area.put("comment", comment);
        	area.put("img", img);
//        	area.put("kinderGarten", kinderGarten);
        	
        	if ( points.length() > 0 ) json.getJSONArray("adminstrativeDistricts").put(area);
        }
	}

	private static void getDistricts(JSONObject json) throws ParseException, JSONException {
		
		ResultSet rs = qef.createQueryExecution(query.replace("RDF:TYPE", "http://dbpedia.org/ontology/District")).execSelect();
		
        while (rs.hasNext()) {
        	
        	QuerySolution result = rs.next();
        	
        	// get string values
        	String uri			 = result.get("s").asResource().getURI();
        	String label		 = getLabel(uri);
        	String comment		 = getComment(uri);
        	String img		 	 = getImage(uri);
        	String kinderGarten	 = getKindergartenCount(uri);
        	
        	// simplify geometry
        	Geometry geo = new WKTReader().read(result.get("wkt").asLiteral().getLexicalForm());
        	JSONArray points = new JSONArray();
        	for ( Coordinate p : TopologyPreservingSimplifier.simplify(geo, 0.01).getCoordinates()) {
        		
        		JSONObject point = new JSONObject();
        		point.put("lat", String.format("%.4f", p.y).replace(",", "."));
        		point.put("lon", String.format("%.4f", p.x).replace(",", "."));
        		points.put(point);
        	}
        	
        	// some uris appear more then once (if they consist of multipolygons)
        	JSONObject area = (JSONObject) (!json.has(uri) ? new JSONObject() : json.get(uri));
        	JSONArray multiplePolyongs = area.has("sgeo") ? area.getJSONArray("sgeo") : new JSONArray();
        	multiplePolyongs.put(points);
        	
        	area.put("uri", uri);
        	area.put("sgeo", multiplePolyongs);
        	area.put("label", label);
        	area.put("comment", comment);
        	area.put("img", img);
        	area.put("kinderGarten", kinderGarten);
        	
        	if ( points.length() > 0 ) json.getJSONArray("districts").put(area);
        }
	}

	private static String getKindergartenCount(String uri) {
		
		Integer maxCount = Integer.MIN_VALUE;
		Map<String,Integer> maxCounts = new HashMap<>();
		if ( !maxCounts.containsKey("numberOfKindergarten") ) {
			
			ResultSet rs = qef.createQueryExecution("SELECT MAX(?kindergartenCount) FROM <http://geostats.aksw.org> { ?s <http://geostats.aksw.org/numberOfKindergarten> ?kindergartenCount }").execSelect();
			while ( rs.hasNext()) maxCount = rs.next().get("callret-0").asLiteral().getInt();
			maxCounts.put("numberOfKindergarten", maxCount);
		}
		maxCount = maxCounts.get("numberOfKindergarten");
		
		ResultSet rs = qef.createQueryExecution(String.format("SELECT ?kindergartenCount FROM <http://geostats.aksw.org> { <%s> <http://geostats.aksw.org/numberOfKindergarten> ?kindergartenCount }", uri)).execSelect();
		while ( rs.hasNext()) return String.format("%.4f", rs.next().get("kindergartenCount").asLiteral().getInt() / (double) maxCount).replace(",", ".");
		
		System.out.println("No kindergarten count for uri: " + uri);
		
		return "0.0";
	}

	private static String getImage(String uri) {
		ResultSet rs = qef.createQueryExecution(String.format("SELECT ?image FROM <http://geostats.aksw.org> { <%s> <http://dbpedia.org/ontology/thumbnail> ?image }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("image").asResource().getURI();
		
		return "no image";
	}

	private static String getComment(String uri) {
		
		ResultSet rs = qef.createQueryExecution(String.format("SELECT ?comment FROM <http://geostats.aksw.org> { <%s> rdfs:comment ?comment }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("comment").asLiteral().getLexicalForm();
		
		return "no comment";
	}

	private static String getLabel(String uri) {
		
		ResultSet rs = qef.createQueryExecution(String.format("SELECT ?label FROM <http://geostats.aksw.org> { <%s> rdfs:label ?label }", uri)).execSelect();
		while ( rs.hasNext()) return rs.next().get("label").asLiteral().getLexicalForm();
		
		return "no label";
	}
}
