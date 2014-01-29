package eu.geoknow.athenarc.triplegeo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
		tmpModel.setNsPrefix("gis", "http://www.opengis.net/ont/geosparql#");
		tmpModel.setNsPrefix("geostats", namespacePrefix);
		tmpModel.setNsPrefix("geo", URLConstants.NS_GEO);
		tmpModel.setNsPrefix("sf", URLConstants.NS_SF);
		tmpModel.setNsPrefix("dc", URLConstants.NS_DC);
		tmpModel.setNsPrefix("xsd", URLConstants.NS_XSD);
		tmpModel.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		tmpModel.setNsPrefix("dbr", "http://dbpedia.org/resource/");
		tmpModel.setNsPrefix("de-dbr", "http://de.dbpedia.org/resource/");
		tmpModel.setNsPrefix("gadm", "http://linkedgeodata.org/ld/gadm2/ontology/");
		tmpModel.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		tmpModel.setNsPrefix("ramon", "http://rdfdata.eionet.europa.eu/ramon/ontology/");
		tmpModel.setNsPrefix("nuts", "http://nuts.geovocab.org/id/");
		
		return tmpModel;
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
