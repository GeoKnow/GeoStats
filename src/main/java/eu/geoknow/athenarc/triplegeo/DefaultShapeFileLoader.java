package eu.geoknow.athenarc.triplegeo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.Property;

import com.hp.hpl.jena.rdf.model.Model;

import eu.geoknow.athenarc.triplegeo.shape.ShpFileLoader;
import eu.geoknow.athenarc.triplegeo.utils.UtilsConstants;

public abstract class DefaultShapeFileLoader implements ShpFileLoader {
	
	protected static final CharSequence STRING_TO_REPLACE = null;
	protected static final CharSequence REPLACEMENT = null;
	protected String featureAttribute;
	protected String featureName;
	protected String featureClass;
	protected String type = "resource";
	protected String language = "de";
	protected String path;
	protected String fileName;
	protected Model model;
	protected FeatureCollection featureCollection;
	protected String areaType;

	/**
	   * Loads the shape file from the configuration path and returns the
	   * feature collection associated according to the configuration.
	   *
	   * @param shapePath with the path to the shapefile.
	   * @param featureString with the featureString to filter.
	   *
	   * @return FeatureCollection with the collection of features filtered.
	   */
	  public FeatureCollection getShapeFileFeatureCollection() {
		  
	    File file = new File(this.path);
	    
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
	  	      	FeatureSource featureSource = dataStore.getFeatureSource(this.fileName);
	  	      	this.featureCollection = featureSource.getFeatures();
	  	      	
//	  	      	for ( Property i : ((SimpleFeatureImpl) featureCollection.features().next()).getProperties()) {
//	  	      		
//	  	      		System.out.println(i);
//	  	      	}
	    	}
	    	catch ( Exception e) { e.printStackTrace(); }
	    }
	    return null;
	  }
	  
	  /**
	   * 
	   * @param model
	   * @param feature
	   * @param uri
	   * @param originalName
	   * @throws UnsupportedEncodingException
	   * @throws FileNotFoundException
	   */
	  public void handleNonGeometricAttributes(Model model, SimpleFeatureImpl feature, String uri, String originalName) throws UnsupportedEncodingException, FileNotFoundException {

	        try {

	            // Feature id
	            if (feature.getAttribute(this.featureAttribute) != null) featureAttribute = feature.getAttribute(this.featureAttribute).toString();
	            // Feature name
	            if (feature.getAttribute(this.featureName) != null) featureName = feature.getAttribute(this.featureName).toString();
	            // Feature classification
	            if (feature.getAttribute(this.featureClass) != null) featureClass = feature.getAttribute(this.featureClass).toString();

	            RdfExport.insertResourceNameLiteral(model, uri, "http://linkedgeodata.org/ld/gadm2/ontology/label", originalName, this.language );
	        } catch (Exception e) {

	            e.printStackTrace();
	        }
	   }
}
