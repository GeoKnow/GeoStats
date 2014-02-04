package org.aksw.datacube.spec;

import org.aksw.datacube.property.MeasureProperty;

public class MeasureComponentSpecification extends ComponentSpecification {

	/**
	 * 
	 * @param refPeriodDimension
	 */
	public MeasureComponentSpecification(MeasureProperty refPeriodDimension) {
		super(refPeriodDimension);
	}

	@Override
	public MeasureProperty getComponentProperty() {
		// TODO Auto-generated method stub
		return (MeasureProperty) this.property;
	}
}
