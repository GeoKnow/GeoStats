package org.aksw.geostats.datacube.spec;

import org.aksw.geostats.datacube.property.AttributeProperty;
import org.aksw.geostats.datacube.property.ComponentProperty;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * 
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class AttributeComponentSpecification extends ComponentSpecification {
	
	/**
	 * 
	 * @param property
	 */
	public AttributeComponentSpecification(Resource resource) {
		super(resource);
	}

	@Override
	public AttributeProperty getComponentProperty() {
		// TODO Auto-generated method stub
		return (AttributeProperty) this.property;
	}
}
