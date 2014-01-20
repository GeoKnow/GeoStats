/**
 * 
 */
package org.aksw;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import eu.geoknow.athenarc.triplegeo.shape.URLConstants;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class DataRetriever {
	
	static QueryExecutionFactory lgd;
	static QueryExecutionFactory localhost;
	
	static {
		
		try {
			
			lgd = new QueryExecutionFactoryHttp("http://linkedgeodata.org/vsparql");
			lgd = new QueryExecutionFactoryRetry(lgd, 3, 1000);
			lgd = new QueryExecutionFactoryCacheEx(lgd, new CacheExImpl(CacheCoreH2.create("lgd", 150l * 60l * 60l * 1000l, false)));
			lgd = new QueryExecutionFactoryPaginated(lgd, 500);
			
			localhost = new QueryExecutionFactoryHttp("http://localhost:8890/sparql");
			localhost = new QueryExecutionFactoryRetry(localhost, 3, 1000);
			localhost = new QueryExecutionFactoryCacheEx(localhost, new CacheExImpl(CacheCoreH2.create("localhost", 150l * 60l * 60l * 1000l, false)));
			localhost = new QueryExecutionFactoryPaginated(localhost, 500);
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static int getNumberOfType(String gadmUri, String type) {
		
		String query = 
		  "PREFIX bif: <http://www.openlinksw.com/schemas/bif#> \n" + 
		  getPrefixes() + 
		  "Select ?item { " +
				"<"+gadmUri+"> 	" +
		    		"gadm-o:representedBy [ " +
	    			    "geom:geometry [ " +
	    			        "ogc:asWKT ?lvlGeo " +
	    			    "] " +
	                "]; " +
		    " . " + 
		    " ?item " + 
		    	"a meta:Node ; " + 
		    	"a lgdo:"+ type +" ; " +
		    	"geom:geometry [ " + 
		    		"ogc:asWKT ?itemGeo " + 
		    	" ] ; " + 
		    " . " + 
		    "Filter(bif:st_intersects(?lvlGeo, ?itemGeo)) " + 
		"} ";
//		"LIMIT 1000";
		
		System.out.println(query);
		
		ResultSet execSelect = lgd.createQueryExecution(QueryFactory.create(query)).execSelect();
		int counter = 0;
		while ( execSelect.hasNext() ) {
			
			execSelect.next();
			counter++;
		}
		return counter;
	}
	
	public static String getPrefixes() {
		
		return 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
				"PREFIX dcterms: <http://purl.org/dc/terms/> \n" +
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
				"PREFIX ogc: <http://www.opengis.net/ont/geosparql#> \n" +
				"PREFIX geom: <http://geovocab.org/geometry#> \n" +
				"PREFIX spatial: <http://geovocab.org/spatial#> \n" +
				"PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
				"PREFIX wgs: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n" +
				"PREFIX lgdo: <http://linkedgeodata.org/ontology/> \n" +
				"PREFIX meta: <http://linkedgeodata.org/meta/> \n" +
				"PREFIX lgd-geom: <http://linkedgeodata.org/geometry/> \n" +
				"PREFIX lgd: <http://linkedgeodata.org/triplify/> \n" +
				"PREFIX gadm-o: <http://linkedgeodata.org/ld/gadm2/ontology/> \n" +
				"PREFIX gadm-r: <http://linkedgeodata.org/ld/gadm2/resource/> \n" +
				"PREFIX meta-o: <http://linkedgeodata.org/ld/meta/ontology/> \n";
	}
	
	public static List<District> getDistricts() {
		
		String query = 
				  getPrefixes() + 
				  "SELECT ?district ?gadmUri ?label FROM <http://geostats.aksw.org> { " +
						"?district " + 
							"a dbo:District ; " +
							"gadm-o:representedBy ?gadmUri ;  " +
							"gadm-o:label ?label .  " +
			     "}";
		
		ResultSet execSelect = localhost.createQueryExecution(query).execSelect();
		
		List<District> districts = new ArrayList<>();
		while ( execSelect.hasNext() ) {
			
			QuerySolution next = execSelect.next();
			
			District d = new District();
			d.label = next.get("label").asLiteral().getLexicalForm();
			d.uri = next.get("district").asResource().getURI();
			d.gadmUri = next.get("gadmUri").asResource().getURI();
			
			districts.add(d);
		}
				
		return districts;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		List<District> districts = getDistricts();
		for ( District d : districts) {
			
			d.kindergartenCount = getNumberOfType(d.gadmUri, "Kindergarten");
			System.out.println("Trying to get infos for " + d.label + " ("+ (districts.indexOf(d) + 1) + "/"+districts.size()+") -> " + d.kindergartenCount);
		}
		
		writeStatistics(districts);
	}
	
	private static void writeStatistics(List<District> districts) throws FileNotFoundException {
		
		Model model = createDefaultModel();
		for ( District d : districts ) {
			
			model.add(ResourceFactory.createResource(d.uri), ResourceFactory.createProperty("http://geostats.aksw.org/numberOfKindergarten"), ""+ d.kindergartenCount, XSDDatatype.XSDinteger);
		}
		model.write(new FileOutputStream("/Users/gerb/Development/workspaces/data/geostats/rdf/counts.ttl"), "TURTLE");
	}

	private static Model createDefaultModel() {
		
		Model tmpModel = ModelFactory.createDefaultModel();
	    tmpModel.removeAll();
	    tmpModel.setNsPrefix("gis", "http://www.opengis.net/ont/geosparql#");
	    tmpModel.setNsPrefix("geostats", "http://geostats.aksw.org/");
	    tmpModel.setNsPrefix("geo", URLConstants.NS_GEO);
	    tmpModel.setNsPrefix("sf", URLConstants.NS_SF);
	    tmpModel.setNsPrefix("dc", URLConstants.NS_DC);
	    tmpModel.setNsPrefix("xsd", URLConstants.NS_XSD);
	    tmpModel.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
	    tmpModel.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		return tmpModel;
	}

	public static class District {

		public String label;
		public int kindergartenCount;
		public String gadmUri;
		public String uri;
	}
}
