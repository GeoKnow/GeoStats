package org.aksw.datacube.spec;

import org.aksw.datacube.property.MeasureProperty;

import com.hp.hpl.jena.rdf.model.Resource;

public class MeasureComponentSpecification extends ComponentSpecification {

	/**
	 * 
	 * @param refPeriodDimension
	 */
	public MeasureComponentSpecification(Resource resource) {
		super(resource);
	}

	@Override
	public MeasureProperty getComponentProperty() {
		// TODO Auto-generated method stub
		return (MeasureProperty) this.property;
	}
}
