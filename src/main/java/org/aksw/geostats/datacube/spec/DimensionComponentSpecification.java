package org.aksw.geostats.datacube.spec;

import org.aksw.geostats.datacube.property.DimensionProperty;

import com.hp.hpl.jena.rdf.model.Resource;

public class DimensionComponentSpecification extends ComponentSpecification {
	
	private int order;
	
	/**
	 * 
	 * @param property
	 */
	public DimensionComponentSpecification(Resource resource) {
		super(resource);
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
