package eu.geoknow.athenarc.triplegeo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.aksw.disambiguation.UriResolver;
import org.geotools.feature.simple.SimpleFeatureImpl;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vividsolutions.jts.geom.Geometry;

import eu.geoknow.athenarc.triplegeo.shape.URLConstants;
import eu.geoknow.athenarc.triplegeo.utils.UtilsConstants;

public class RdfExport {

	private static final String SEPARATOR = "/";
	public static final String NS_URI = "http://geostats.aksw.org/";

	/**
	 * Returns a Jena RDF model populated with the params from the
	 * configuration.
	 * 
	 * @param configuration
	 *            with all the configuration parameters.
	 * 
	 * @return a Jena RDF model populated with the params from the
	 *         configuration.
	 */
	public static Model getModelFromConfiguration(String namespacePrefix) {
		Model tmpModel = ModelFactory.createDefaultModel();
		tmpModel.removeAll();
		tmpModel.setNsPrefixes(getNamespaceMapping());
		return tmpModel;
	}
	
	public static Map<String,String> getNamespaceMapping() {
		
		Map<String,String> prefixes = new HashMap<>();
		prefixes.put("gis", "http://www.opengis.net/ont/geosparql#");
		prefixes.put("geostats", "http://geostats.aksw.org/");
		prefixes.put("geo", URLConstants.NS_GEO);
		prefixes.put("sf", URLConstants.NS_SF);
		prefixes.put("dc", URLConstants.NS_DC);
		prefixes.put("xsd", URLConstants.NS_XSD);
		prefixes.put("dbo", "http://dbpedia.org/ontology/");
		prefixes.put("dbr", "http://dbpedia.org/resource/");
		prefixes.put("de-dbr", "http://de.dbpedia.org/resource/");
		prefixes.put("gadm", "http://linkedgeodata.org/ld/gadm2/ontology/");
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("ramon", "http://rdfdata.eionet.europa.eu/ramon/ontology/");
		prefixes.put("nuts", "http://nuts.geovocab.org/id/");
		prefixes.put("qb",              "http://purl.org/linked-data/cube#");
		prefixes.put("gs-qb",           "http://geostats.aksw.org/qb/");
		prefixes.put("rdf",             "http://www.w3.org/1999/02/22-rdf-syntax-ns#"); 
		prefixes.put("owl",             "http://www.w3.org/2002/07/owl#");
		prefixes.put("xsd",             "http://www.w3.org/2001/XMLSchema#");
		prefixes.put("dct",             "http://purl.org/dc/terms/");
		prefixes.put("sdmx-concept",    "http://purl.org/linked-data/sdmx/2009/concept#");
		prefixes.put("sdmx-code",       "http://purl.org/linked-data/sdmx/2009/code#");
		prefixes.put("sdmx-dimension",  "http://purl.org/linked-data/sdmx/2009/dimension#");
		prefixes.put("sdmx-attribute",  "http://purl.org/linked-data/sdmx/2009/attribute#");
		prefixes.put("sdmx-measure",    "http://purl.org/linked-data/sdmx/2009/measure#");
		prefixes.put("admingeo",        "http://data.ordnancesurvey.co.uk/ontology/admingeo/"); 
		prefixes.put("sdmx-subject",    "http://purl.org/linked-data/sdmx/2009/subject#"); 
		prefixes.put("interval",        "http://reference.data.gov.uk/def/intervals/"); 
		prefixes.put("genesis",        "http://www.regionalstatistik.de/genesis/resource/");
		return prefixes;
	}
	
	public static String getNamespaceMappingForQuery() {
		
		String prefixes = "";
		for ( Map.Entry<String, String> entry : getNamespaceMapping().entrySet()){
			prefixes += "PREFIX " + entry.getKey() + ": <" + entry.getValue() + "> \n";
		}
		
		return prefixes;
	}
	
	public static void write(Model model, String filePathAndName) {
		
		// Export model to a suitable format
		FileOutputStream out;
		try {
			out = new FileOutputStream(filePathAndName);
			model.write(out, "TURTLE");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 * Handle string literals for 'name' attribute
	 */
	public static void insertResourceNameLiteral(Model model, String s, String p, String o, String lang) {

		Resource resource = model.createResource(s);
		Property property = model.createProperty(p);
		if (lang != null) {
			Literal literal = model.createLiteral(o, lang);
			resource.addLiteral(property, literal);
		} else {
			resource.addProperty(property, o);
		}
	}

	/**
	 * 
	 * Handle resource triples
	 */
	public static void insertResourceTriple(Model model, String s, String p, String o) {

		Resource resourceGeometry = model.createResource(s);
		Property property = model.createProperty(p);
		Resource resourceGeometry2 = model.createResource(o);
		resourceGeometry.addProperty(property, resourceGeometry2);
	}

	/**
	 * 
	 * Handle label triples
	 */
	public static void insertLabelResource(Model model, String resource, String label, String lang) {
		Resource resource1 = model.createResource(resource);
		model.add(resource1, RDFS.label, model.createLiteral(label, lang));
	}

	public static void insertGadmTriples(Model model, String uri, String countryID, String stateID, String adminID, String districtID) {

		String gadmUri = "http://linkedgeodata.org/ld/gadm2/resource/level";
		if (countryID != null &&  !countryID.isEmpty())
			gadmUri += "_" + countryID;
		if (stateID != null &&  !stateID.isEmpty())
			gadmUri += "_" + stateID;
		if (adminID != null &&  !adminID.isEmpty())
			gadmUri += "_" + adminID;
		if (districtID != null &&  !districtID.isEmpty())
			gadmUri += "_" + districtID;

		model.add(model.createResource(uri), ResourceFactory.createProperty("http://linkedgeodata.org/ld/gadm2/ontology/representedBy"), ResourceFactory.createResource(gadmUri));
	}

	/**
	 * changes dg
	 * 
	 * @param string
	 */
	public static void insertResourceHierachie(Model model, String uri, Object admDistrict, Object state, Object country) {
		
		if ( admDistrict != null ) {
			
			String districtUri = UriResolver.getInstance().getUri(admDistrict.toString(), null);
			UriResolver.getInstance().queryExtra(districtUri, "adminDistrict", model);
			model.add(model.createResource(uri), ResourceFactory.createProperty("http://dbpedia.org/ontology/adminstrativeDistrict"), ResourceFactory.createResource(districtUri));
		}
		
		if ( state != null ) {
			
			String stateUri = UriResolver.getInstance().getUri(state.toString(), null);
			UriResolver.getInstance().queryExtra(stateUri, "state", model);
			model.add(model.createResource(uri), ResourceFactory.createProperty("http://dbpedia.org/ontology/federalState"), ResourceFactory.createResource(stateUri));
		}
		
		if ( country != null ) {
			
			String countryUri = UriResolver.getInstance().getUri(country.toString(), null);
			UriResolver.getInstance().queryExtra(countryUri, "country", model);
			model.add(model.createResource(uri), ResourceFactory.createProperty("http://dbpedia.org/ontology/country"), ResourceFactory.createResource(countryUri));
		}
	}

	//
	/**
	 * Handle Polyline geometry according to GeoSPARQL standard
	 * 
	 */
	public static void insertLineString(Model model, String resource, Geometry geo) {

		insertResourceTriple(model, NS_URI + resource, URLConstants.NS_GEO + "hasGeometry", NS_URI + UtilsConstants.FEAT + resource);

		insertResourceTypeResource(model, NS_URI + UtilsConstants.FEAT + resource, URLConstants.NS_SF + Constants.LINE_STRING);

		insertLiteralTriplet(model, NS_URI + UtilsConstants.FEAT + resource, URLConstants.NS_GEO + Constants.WKT, geo.toText(), URLConstants.NS_GEO + Constants.WKTLiteral);
	}

	/**
	 * 
	 * Handle Polygon geometry according to GeoSPARQL standard
	 */
	public static void insertPolygon(Model model, String uri, Geometry geo) throws UnsupportedEncodingException {

		String geometryUri = uri + SEPARATOR + UtilsConstants.FEAT;
		insertResourceTriple(model, uri, URLConstants.NS_GEO + "hasGeometry", geometryUri);
		insertResourceTypeResource(model, geometryUri, URLConstants.NS_SF + Constants.POLYGON);
		insertLiteralTriplet(model, geometryUri, URLConstants.NS_GEO + Constants.WKT, geo.toText(), URLConstants.NS_GEO + Constants.WKTLiteral);

//		String simplifiedGeometryUri = uri + SEPARATOR + UtilsConstants.FEAT + "/simple";
//		insertResourceTriple(uri, URLConstants.NS_GEO + "hasSimplifiedGeometry", simplifiedGeometryUri);
//		insertResourceTypeResource(simplifiedGeometryUri, URLConstants.NS_SF + Constants.POLYGON);
//		insertLiteralTriplet(simplifiedGeometryUri, URLConstants.NS_GEO + Constants.WKT, DouglasPeuckerSimplifier.simplify(geo, 0.001).toText(), URLConstants.NS_GEO + Constants.WKTLiteral);
	}

	/**
	 * 
	 * Handle resource type
	 */
	public static void insertResourceTypeResource(Model model, String r1, String r2) {

		model.add(model.createResource(r1), RDF.type, model.createResource(r2));
	}

	/**
	 * 
	 * Handle triples for string literals
	 */
	public static void insertLiteralTriplet(Model model, String s, String p, String o, String x) {

		Resource resourceGeometry = model.createResource(s);
		Property property = model.createProperty(p);

		if (x != null) {

			resourceGeometry.addLiteral(property, model.createTypedLiteral(o, x));
		} else {
			resourceGeometry.addProperty(property, o);
		}
	}
	
	/**
	 * 
	 * Handling non-spatial attributes only
	 * 
	 * @param uri
	 **/
	public static void handleNonGeometricAttributes(Model model, SimpleFeatureImpl feature, String uri, String originalName, String language) throws UnsupportedEncodingException, FileNotFoundException {

		RdfExport.insertResourceNameLiteral(model, uri, "http://linkedgeodata.org/ld/gadm2/ontology/label", originalName, language);
	}

	/**
	 * 
	 * Point geometry according to GeoSPARQL standard
	 */
	public static void insertPoint(Model model, String resource, Geometry geo) {
		
		insertResourceTriple(model, NS_URI + resource, URLConstants.NS_GEO + "hasGeometry", NS_URI + UtilsConstants.FEAT + resource);
		insertResourceTypeResource(model, NS_URI + UtilsConstants.FEAT + resource, URLConstants.NS_SF + Constants.POINT);
		insertLiteralTriplet(model, NS_URI + UtilsConstants.FEAT + resource, URLConstants.NS_GEO + Constants.WKT, geo.toText(), URLConstants.NS_GEO + Constants.WKTLiteral);
	}
}
