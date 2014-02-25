package org.aksw.geostats.datacube;

import java.util.ArrayList;
import java.util.List;

import org.aksw.geostats.datacube.spec.AttributeComponentSpecification;
import org.aksw.geostats.datacube.spec.ComponentSpecification;
import org.aksw.geostats.datacube.spec.DimensionComponentSpecification;
import org.aksw.geostats.datacube.spec.MeasureComponentSpecification;

import com.hp.hpl.jena.rdf.model.Resource;

public class DataStructureDefinition extends AbstractResource {
	
	private List<DimensionComponentSpecification> dimensionSpecifications;
	private List<AttributeComponentSpecification> attributeSpecifications;
	private List<MeasureComponentSpecification> measureSpecifications;
	
	/**
	 * 
	 */
	public DataStructureDefinition(Resource resource) {
		super(resource);
		
		this.dimensionSpecifications = new ArrayList<>();
		this.attributeSpecifications = new ArrayList<>();
		this.measureSpecifications = new ArrayList<>();
	}
	
	/**
	 * 
	 * @param dimensionSpec
	 */
	public void addDimensionSpecification(DimensionComponentSpecification dimensionSpec) {
		this.dimensionSpecifications.add(dimensionSpec);
	}
	
	/**
	 * 
	 * @param attributeSpec
	 */
	public void addAttributeSpecification(AttributeComponentSpecification attributeSpec) {
		this.attributeSpecifications.add(attributeSpec);
	}
	
	/**
	 * 
	 * @param measureSpec
	 */
	public void addMeasureSpecification(MeasureComponentSpecification measureSpec) {
		this.measureSpecifications.add(measureSpec);
	}
	
	/**
	 * 
	 * @param dimensionSpec
	 */
	public void addAllDimensionSpecification(List<DimensionComponentSpecification> dimensionSpecs) {
		this.dimensionSpecifications.addAll(dimensionSpecs);
	}
	
	/**
	 * 
	 * @param attributeSpec
	 */
	public void addAllAttributeSpecification(List<AttributeComponentSpecification> attributeSpecs) {
		this.attributeSpecifications.addAll(attributeSpecs);
	}
	
	/**
	 * 
	 * @param measureSpec
	 */
	public void addAllMeasureSpecification(List<MeasureComponentSpecification> measureSpecs) {
		this.measureSpecifications.addAll(measureSpecs);
	}

	/**
	 * @return the dimensionSpecifications
	 */
	public List<DimensionComponentSpecification> getDimensionSpecifications() {
		return dimensionSpecifications;
	}

	/**
	 * @param dimensionSpecifications the dimensionSpecifications to set
	 */
	public void setDimensionSpecifications(List<DimensionComponentSpecification> dimensionSpecifications) {
		this.dimensionSpecifications = dimensionSpecifications;
	}

	/**
	 * @return the attributeSpecifications
	 */
	public List<AttributeComponentSpecification> getAttributeSpecifications() {
		return attributeSpecifications;
	}

	/**
	 * @param attributeSpecifications the attributeSpecifications to set
	 */
	public void setAttributeSpecifications(List<AttributeComponentSpecification> attributeSpecifications) {
		this.attributeSpecifications = attributeSpecifications;
	}

	/**
	 * @return the measureSpecifications
	 */
	public List<MeasureComponentSpecification> getMeasureSpecifications() {
		return measureSpecifications;
	}

	/**
	 * @param measureSpecifications the measureSpecifications to set
	 */
	public void setMeasureSpecifications(List<MeasureComponentSpecification> measureSpecifications) {
		this.measureSpecifications = measureSpecifications;
	}

	@Override
	public String getUri() {
		
		return "NOT YET IMPLEMENTED";
	}
}
