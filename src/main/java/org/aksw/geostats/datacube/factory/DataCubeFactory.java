/**
 * 
 */
package org.aksw.geostats.datacube.factory;

import java.util.UUID;

import org.aksw.geostats.Constants;
import org.aksw.geostats.datacube.DataSet;
import org.aksw.geostats.datacube.DataStructureDefinition;
import org.aksw.geostats.datacube.observation.Observation;
import org.aksw.geostats.datacube.property.DimensionProperty;
import org.aksw.geostats.datacube.property.MeasureProperty;
import org.aksw.geostats.datacube.spec.DimensionComponentSpecification;
import org.aksw.geostats.datacube.spec.MeasureComponentSpecification;

import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class DataCubeFactory {

	private static DataCubeFactory INSTANCE;
	
	private DataCubeFactory(){}
	
	/**
	 * 
	 * @return
	 */
	public static DataCubeFactory getInstance(){
		
		if ( DataCubeFactory.INSTANCE == null ) {
			
			DataCubeFactory.INSTANCE = new DataCubeFactory();
		}
		
		return DataCubeFactory.INSTANCE;
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 */
	public DataSet createDataSet(String uri) {
		
		return new DataSet(ResourceFactory.createResource(uri));
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public DataStructureDefinition createDataStructureDefinition(String uri) {
		
		return new DataStructureDefinition(ResourceFactory.createResource(fixUri(uri)));
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public Observation createObservation(String uri) {
		
		return new Observation(ResourceFactory.createResource(fixUri(uri)));
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public MeasureProperty createMeasureProperty(String uri) {
		
		return new MeasureProperty(ResourceFactory.createResource(fixUri(uri)));
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public MeasureComponentSpecification createMeasureComponentSpecification(String uri) {
		
		return new MeasureComponentSpecification(ResourceFactory.createResource(fixUri(uri)));
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public DimensionProperty createDimensionProperty(String uri) {

		return new DimensionProperty(ResourceFactory.createResource(fixUri(uri)));
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public DimensionComponentSpecification createDimensionComponentSpecification(String uri) {
		
		return new DimensionComponentSpecification(ResourceFactory.createResource(fixUri(uri)));
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 */
	private String fixUri(String uri) {
		
		if ( uri == null || uri.isEmpty() || !uri.startsWith("http://") ) {
			
			uri = Constants.GEOSTATS_DATA_CUBE_NS + UUID.randomUUID(); 
		}
		return uri;
	}
}
