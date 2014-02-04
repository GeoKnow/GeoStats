package org.aksw.datacube.spec;

import org.aksw.datacube.property.DimensionProperty;

public class DimensionComponentSpecification extends ComponentSpecification {
	
	private int order;
	
	/**
	 * 
	 * @param property
	 */
	public DimensionComponentSpecification(DimensionProperty property) {
		super(property);
	}
	
	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public DimensionProperty getComponentProperty() {
		// TODO Auto-generated method stub
		return (DimensionProperty) this.property;
	}
}
