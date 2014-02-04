package org.aksw.datacube.observation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.datacube.Resource;
import org.aksw.datacube.property.DimensionProperty;
import org.aksw.datacube.property.MeasureProperty;
import org.aksw.datacube.spec.DimensionComponentSpecification;

public class Observation implements Resource {

	private List<DimensionProperty> dimensions;
	private Map<MeasureProperty, ObservationValue> values;
	
	/**
	 * 
	 */
	public Observation() {
		
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

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "NOT YET IMPLEMENTED";
	}
	
	/**
	 * 
	 * @param dimensionSpecifications
	 */
	public void addAllDimensionProperties(List<DimensionComponentSpecification> dimensionSpecifications) {
		for ( DimensionComponentSpecification spec : dimensionSpecifications ) this.dimensions.add(spec.getComponentProperty());
	}
}
