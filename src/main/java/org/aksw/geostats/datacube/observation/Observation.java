package org.aksw.geostats.datacube.observation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.geostats.datacube.AbstractResource;
import org.aksw.geostats.datacube.property.DimensionProperty;
import org.aksw.geostats.datacube.property.MeasureProperty;
import org.aksw.geostats.datacube.spec.DimensionComponentSpecification;

import com.hp.hpl.jena.rdf.model.Resource;

public class Observation extends AbstractResource {

	private List<DimensionProperty> dimensions;
	private Map<MeasureProperty, ObservationValue> values;
	
	/**
	 * 
	 */
	public Observation(Resource resource) {
		super(resource);
		this.dimensions = new ArrayList<>();
		this.values = new HashMap<>();
	}
	
	/**
	 * @return the dimensions
	 */
	public List<DimensionProperty> getDimensions() {
		return dimensions;
	}
	/**
	 * @param dimensions the dimensions to set
	 */
	public void setDimensions(List<DimensionProperty> dimensions) {
		this.dimensions = dimensions;
	}
	/**
	 * @return the values
	 */
	public Map<MeasureProperty, ObservationValue> getValues() {
		return values;
	}
	/**
	 * @param values the values to set
	 */
	public void setValues(Map<MeasureProperty, ObservationValue> values) {
		this.values = values;
	}
	
	/**
	 * 
	 * @param property
	 */
	public void addDimensionProperty(DimensionProperty property) {
		this.dimensions.add(property);
	}
	
	/**
	 * 
	 * @param property
	 * @param value
	 */
	public void addObservationValue(MeasureProperty property, ObservationValue value) {
		this.values.put(property, value);
	}

	/**
	 * 
	 * @param dimensionSpecifications
	 */
	public void addAllDimensionProperties(List<DimensionComponentSpecification> dimensionSpecifications) {
		for ( DimensionComponentSpecification spec : dimensionSpecifications ) this.dimensions.add(spec.getComponentProperty());
	}
}
