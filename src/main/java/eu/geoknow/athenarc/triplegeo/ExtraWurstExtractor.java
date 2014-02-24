/**
 * 
 */
package eu.geoknow.athenarc.triplegeo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.Feature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelCon;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import eu.geoknow.athenarc.triplegeo.shape.ShpFileLoader;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class ExtraWurstExtractor {

	static Property CODE = ResourceFactory.createProperty("http://rdfdata.eionet.europa.eu/ramon/ontology/code");
      static Property LEVEL = ResourceFactory.createProperty("http://rdfdata.eionet.europa.eu/ramon/ontology/level");
      
	
	/**
	 * @param args
	 */
	public static void extract(Model model) {
		
		File file = new File("data/geodatenzentrum/");
	    
	    // Create the map with the file URL to be passed to DataStore.
	    Map map = new HashMap();
	    try {
	      map.put("url", file.toURL());
	    } catch (MalformedURLException ex) {
	      Logger.getLogger(ShpFileLoader.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    if (map.size() > 0) {
	    	try {
	    		DataStore dataStore = DataStoreFinder.getDataStore(map);
	  	      	FeatureSource featureSource = dataStore.getFeatureSource("vg250_krs_latlng");
	  	      	FeatureCollection features = featureSource.getFeatures();
	  	      	FeatureIterator iterator = features.features();
	  	      	while ( iterator.hasNext() ) {
	  	      		
		  	      	SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
					Geometry geometry = (Geometry) feature.getDefaultGeometry();
					
					String name = feature.getAttribute("GEN").toString();
					String type = feature.getAttribute("DES").toString();
						
					if ( name.equals("Rostock") ) insert(model, name, "DE803", "3", (MultiPolygon) geometry);
					if ( name.equals("Schwerin") ) insert(model, name, "DE804", "3", (MultiPolygon) geometry);
					if ( name.equals("Mecklenburgische Seenplatte") ) insert(model, name, "DE80J", "3", (MultiPolygon) geometry);
					if ( name.equals("Landkreis Rostock") ) insert(model, name, "DE80K", "3", (MultiPolygon) geometry);
					if ( name.equals("Vorpommern-Rügen") ) insert(model, name, "DE80L", "3", (MultiPolygon) geometry);
					if ( name.equals("Nordwestmecklenburg") ) insert(model, name, "DE80M", "3", (MultiPolygon) geometry);
					if ( name.equals("Vorpommern-Greifswald") ) insert(model, name, "DE80N", "3", (MultiPolygon) geometry);
					if ( name.equals("Ludwigslust-Parchim") ) insert(model, name, "DE80O", "3", (MultiPolygon) geometry);
					if ( name.equals("Heidekreis") ) insert(model, name, "DE938", "3", (MultiPolygon) geometry);
					if ( name.equals("Vulkaneifel") ) insert(model, name, "DEB24", "3", (MultiPolygon) geometry);
					if ( name.equals("Regionalverband Saarbrücken") ) insert(model, name, "DEC01", "3", (MultiPolygon) geometry);
					if ( name.equals("Ansbach") && feature.getAttribute("RS").toString().equals("09571"))  insert(model, "Landkreis "+ name, "DE256", "3", (MultiPolygon) geometry);
	  	      	}
	    	}
	    	catch ( Exception e) { e.printStackTrace(); }
	    }
	}
	
	private static void insert(Model model, String name, String code, String level, MultiPolygon geometry) throws UnsupportedEncodingException, MismatchedDimensionException, TransformException{
		
		Resource r = ResourceFactory.createResource("http://nuts.geovocab.org/id/"+ code );
		model.add(r, RDFS.label, name);
		model.add(r, CODE, code);
		model.add(r, LEVEL, level);
		
		MultiPolygon multiPolygon = (MultiPolygon) geometry;
		for (int i = 0; i < multiPolygon.getNumGeometries(); ++i)
			RdfExport.insertPolygon(model, r.getURI(), multiPolygon.getGeometryN(i));
	}
}


