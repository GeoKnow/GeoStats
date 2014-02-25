package org.aksw.geostats.datacube;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AbstractResource {
	
	protected Resource resource;

	public AbstractResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * 
	 * @return
	 */
	public String getUri() {
		
		return this.resource.getURI();
	}
}
