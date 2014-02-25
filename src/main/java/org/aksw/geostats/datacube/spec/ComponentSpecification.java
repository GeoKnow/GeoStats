package org.aksw.geostats.datacube.spec;

import java.util.HashMap;
import java.util.Map;

import org.aksw.geostats.Constants;
import org.aksw.geostats.datacube.AbstractResource;
import org.aksw.geostats.datacube.Language;
import org.aksw.geostats.datacube.property.ComponentProperty;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class ComponentSpecification extends AbstractResource {

	protected String name;
	protected Map<Language,String> labels;
	protected ComponentProperty property;
	
	/**
	 * @param refPeriodDimension 
	 * 
	 */
	public ComponentSpecification(Resource resource){
		super(resource);
		
		this.labels = new HashMap<>();
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract ComponentProperty getComponentProperty();
	
	/**
	 * 
	 * @return
	 */
	public void setComponentProperty(ComponentProperty componentProperty) {
		this.property = componentProperty;
	}

	/**
	 * 
	 * @param label
	 * @param language
	 */
	public void addLabel(String label, Language language){
		this.labels.put(language, label);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the labels
	 */
	public Map<Language, String> getLabels() {
		return labels;
	}

	/**
	 * @param labels the labels to set
	 */
	public void setLabels(Map<Language, String> labels) {
		this.labels = labels;
	}
}
