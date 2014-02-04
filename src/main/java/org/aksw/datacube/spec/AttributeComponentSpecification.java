package org.aksw.datacube.spec;

import org.aksw.datacube.property.AttributeProperty;
import org.aksw.datacube.property.ComponentProperty;

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
	public AttributeComponentSpecification(AttributeProperty property) {
		super(property);
	}

	@Override
	public AttributeProperty getComponentProperty() {
		// TODO Auto-generated method stub
		return (AttributeProperty) this.property;
	}
}
