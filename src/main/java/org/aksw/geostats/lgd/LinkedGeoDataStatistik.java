/**
 * 
 */
package org.aksw.geostats.lgd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aksw.geostats.rdf.RdfExport;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.commons.collections15.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.postgis.MultiPolygon;
import org.postgis.Polygon;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;


/**
 * @author gerb
 *
 */
public class LinkedGeoDataStatistik {
	
	public static final String GS_QB_NS = "http://geostats.aksw.org/qb/";
	public static final String QB_NS    = "http://purl.org/linked-data/cube#";
	public static Map<String,String> translations = new HashMap<>();
	public static Integer ENTITIES = 1;
	public static Integer TOTAL_ENTITIES = 1;
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException, ExecutionException {
		
		LinkedGeoDataStatistik.run();
	}
	
	public static void run() throws ClassNotFoundException, SQLException, InterruptedException, ExecutionException {
		
		Model model = createDataCube();
		
		Class.forName("org.postgresql.Driver");
		
		List<Callable<Job>> jobs = new ArrayList<>();
		for ( Entity entity : getEntities() ) {
			jobs.add(new LinkedGeoDataCallable(new Job(entity, getAmenities(model))));
		}
		for ( Future<Job> future : executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(10), jobs) ) {
			
			Job job = future.get();
			for ( Map.Entry<String, Integer> entry : job.statistics.entrySet()) {
				
				String regioId = job.entity.uri.replace("http://www.regionalstatistik.de/genesis/resource/", "");
				Resource observation = ResourceFactory.createResource("http://geostats.aksw.org/qb/observation/openstreetmaps/2014_"+regioId);
				model.add(observation, RDF.type, ResourceFactory.createResource(QB_NS+"Observation"));
				model.add(observation, ResourceFactory.createProperty(QB_NS+"dataSet"), ResourceFactory.createResource(GS_QB_NS + "GeoStatsOpenStreetMaps"));
				model.add(observation, ResourceFactory.createProperty(GS_QB_NS+"refArea"), ResourceFactory.createResource(job.entity.uri));
				model.add(observation, ResourceFactory.createProperty(GS_QB_NS+"refPeriod"), ResourceFactory.createResource("http://dbpedia.org/resource/2014"));
				model.add(observation, ResourceFactory.createProperty(GS_QB_NS+"refPeriod"), ResourceFactory.createResource("http://dbpedia.org/resource/2014"));
				
				String propertyName = StringUtils.uncapitalise(StringUtils.capitaliseAllWords(entry.getKey().replace("_", " ")).replace(" ", "")) + "Measure";
				model.add(observation, ResourceFactory.createProperty(GS_QB_NS+propertyName), entry.getValue() + "", XSDDatatype.XSDint);
				
				System.out.println(regioId + entry.getValue());
			}
			
			RdfExport.write(model, "data/osm.ttl");
		}
	}

	private static Model createDataCube() {
		
		Model model = RdfExport.getModel();
		Resource dataset = ResourceFactory.createResource(GS_QB_NS + "GeoStatsOpenStreetMaps");
		model.add(dataset, RDF.type, ResourceFactory.createResource(QB_NS + "DataSet"));
		model.add(dataset, RDFS.label, "OpenStreetMaps");
		model.add(dataset, DCTerms.publisher, ResourceFactory.createResource("http://semanticweb.org/id/AKSW"));
		model.add(dataset, DCTerms.issued, new SimpleDateFormat("yyyy-M-dd").format(new Date()), XSDDatatype.XSDdate);
		model.add(dataset, DCTerms.subject, ResourceFactory.createResource("http://purl.org/linked-data/sdmx/2009/subject#3.2"));
		
		translations.put("school", "Schulen");
		translations.put("restaurant", "Restaurants");
		translations.put("place_of_worship", "Gotteshäusern");
		translations.put("fuel", "Tankstellen");
		translations.put("bank", "Banken");
		translations.put("fast_food", "Fast Food");
		translations.put("cafe", "Cafés");
		translations.put("kindergarten", "Kindergärten");
		translations.put("hospital", "Krankenhäusern");
		translations.put("pharmacy", "Apotheken");
		
		return model;
	}

	private static List<Entity> getEntities() throws ClassNotFoundException, SQLException {
		
		List<Entity> entities =  new ArrayList<>();
		String query =  "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
						"select * WHERE { " +
						 "?s a <http://dbpedia.org/ontology/Place> . " + 
						 "?nuts owl:sameAs ?s . "  +
						 "?regio owl:sameAs ?s . " +
						 "FILTER( regex(str(?nuts), '^http://nuts.geovocab.org/id/', 'i')) . " + 
						 "FILTER( regex(str(?regio), '^http://www.regionalstatistik.de/genesis/resource/', 'i')) . " + 
						 "?nuts <http://www.opengis.net/ont/geosparql#hasGeometry> ?geo . "  +
						 "?geo <http://www.opengis.net/ont/geosparql#asWKT> ?wkt " +
						 "} ORDER BY ?nuts ";
		
		QueryExecutionFactory sparql = new QueryExecutionFactoryModel(FileManager.get().loadModel("data/geostats.ttl", "TURTLE"));
		CacheFrontend cache = new CacheFrontendImpl(CacheCoreH2.create("localhost", 150l * 60l * 60l * 1000l, false));
		sparql = new QueryExecutionFactoryCacheEx(sparql, cache);
		
		com.hp.hpl.jena.query.ResultSet resultSet = sparql.createQueryExecution(query).execSelect();
		Map<String,Entity> urisToEntities = new HashMap<>();
		
		while ( resultSet.hasNext() ) {
			
			QuerySolution next = resultSet.next();
			String uri = next.get("regio").asResource().getURI();
			String wkt = next.get("wkt").asLiteral().getLexicalForm();
			
			if ( urisToEntities.containsKey(uri) ) {
				
				Entity entity = urisToEntities.get(uri);
				entity.polygons.add(new Polygon(wkt));
			}
			else {
				
				urisToEntities.put(uri, new Entity(uri, wkt));
			}
		}
		entities = new ArrayList<>(urisToEntities.values());
		TOTAL_ENTITIES = entities.size();
		Collections.shuffle(entities, new Random(100));
		
		return entities;
	}
	
	public static class Job {
		
		public Job(Entity entity, List<String> list) {
			this.entity = entity;
			this.amenity = list;
			this.statistics = new HashedMap<>();
		}
		public Entity entity;
		public List<String> amenity;
		public Map<String,Integer> statistics;
	}
	
	public static class Entity {
		
		public Entity(String uri, String wkt) throws SQLException {
			this.uri = uri;
			this.polygons.add(new Polygon(wkt));
		}
		
		public String uri;
		public List<Polygon> polygons = new ArrayList<>();
		
		public String toString(){
			
			return uri + " Polygons: " + polygons.size();
		}
	}
	
	/**
     * 
     * @param executor
     * @param callables
     * @return
     */
    private static <T> List<Future<T>> executeAndWaitAndShutdownCallables(ExecutorService executor, List<? extends Callable<T>> callables) {
    	
    	List<Future<T>> results = null;
    	
    	try {
            
            results = executor.invokeAll(callables);
            executor.shutdownNow();
        }
        catch (InterruptedException e) {

            e.printStackTrace();
        }
    	
    	return results;
    }

	private static List<String> getAmenities(Model model) {
		
		Resource structure = ResourceFactory.createResource(GS_QB_NS + "GeoStatsOpenStreetMapsStructure");
		model.add(ResourceFactory.createResource(GS_QB_NS + "GeoStatsOpenStreetMaps"), ResourceFactory.createProperty(QB_NS + "structure"), structure);
		model.add(structure, RDF.type, ResourceFactory.createResource(QB_NS+ "DataStructureDefinition"));
		
		List<String> amenities =  new ArrayList<>(Arrays.asList("school","place_of_worship","restaurant","fuel","bank","fast_food","cafe","kindergarten","hospital","pharmacy"));
		
		for ( String amenity : amenities) {
			
			String name = StringUtils.uncapitalise(StringUtils.capitaliseAllWords(amenity.replace("_", " ")).replace(" ", ""));
			
			Resource amenitySpec = ResourceFactory.createResource(GS_QB_NS + name + "Spec");
			Resource amenityMeasure = ResourceFactory.createResource(GS_QB_NS + name + "Measure");
			model.add(amenitySpec, RDF.type, ResourceFactory.createResource(QB_NS+"ComponentSpecification"));
			model.add(amenitySpec, ResourceFactory.createProperty(QB_NS+"measure"), amenityMeasure);
			model.add(amenityMeasure, RDF.type, RDF.Property);
			model.add(amenityMeasure, RDF.type, ResourceFactory.createResource(QB_NS+"MeasureProperty"));
			model.add(amenityMeasure, RDFS.label, "Anzahl an "+ translations.get(amenity), "de");
			model.add(amenityMeasure, RDFS.range, ResourceFactory.createResource("http://www.w3.org/2001/XMLSchema#int"));
			model.add(structure, ResourceFactory.createProperty(QB_NS+"component"), amenitySpec);
		}
		
		addDimensions(model, structure);
		
		return amenities;
	}

	private static void addDimensions(Model model, Resource structure) {
		
		Resource refAreaSpec = ResourceFactory.createResource(GS_QB_NS + "refAreaSpec");
		model.add(refAreaSpec, RDF.type, ResourceFactory.createResource(QB_NS+"ComponentSpecification"));
		
		Resource refArea = ResourceFactory.createResource(GS_QB_NS + "DimensionProperty");
		model.add(refAreaSpec, ResourceFactory.createProperty(QB_NS+"dimension"), refArea);
		model.add(refArea, RDF.type, RDF.Property);
		model.add(refArea, RDF.type, ResourceFactory.createResource(QB_NS+"DimensionProperty"));
		model.add(refArea, RDFS.label, "Ort", "de");
		model.add(refArea, RDFS.range, ResourceFactory.createResource("http://data.ordnancesurvey.co.uk/ontology/admingeo/UnitaryAuthority"));
		model.add(refArea, ResourceFactory.createProperty(QB_NS+"concept"), ResourceFactory.createResource("http://purl.org/linked-data/sdmx/2009/concept#refArea"));
		model.add(structure, ResourceFactory.createProperty(QB_NS+"component"), refAreaSpec);
		
		Resource refPeriodSpec = ResourceFactory.createResource(GS_QB_NS + "refAreaSpec");
		model.add(refPeriodSpec, RDF.type, ResourceFactory.createResource(QB_NS+"ComponentSpecification"));
		
		Resource refPeriod = ResourceFactory.createResource(GS_QB_NS + "DimensionProperty");
		model.add(refPeriodSpec, ResourceFactory.createProperty(QB_NS+"dimension"), refPeriod);
		model.add(refPeriod, RDF.type, RDF.Property);
		model.add(refPeriod, RDF.type, ResourceFactory.createResource(QB_NS+"DimensionProperty"));
		model.add(refPeriod, RDFS.label, "Zeit", "de");
		model.add(refPeriod, RDFS.range, ResourceFactory.createResource("http://reference.data.gov.uk/def/intervals/Interval"));
		model.add(refPeriod, ResourceFactory.createProperty(QB_NS+"concept"), ResourceFactory.createResource("http://purl.org/linked-data/sdmx/2009/concept#refPeriod"));
		model.add(structure, ResourceFactory.createProperty(QB_NS+"component"), refPeriodSpec);
	}
}
