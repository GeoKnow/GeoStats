package org.aksw.datacube.spec;

import java.util.HashMap;
import java.util.Map;

import org.aksw.Constants;
import org.aksw.datacube.Language;
import org.aksw.datacube.Resource;
import org.aksw.datacube.property.ComponentProperty;
import org.aksw.datacube.property.MeasureProperty;

public abstract class ComponentSpecification implements Resource {

	protected String name;
	protected Map<Language,String> labels;
	protected ComponentProperty property;
	
	/**
	 * @param refPeriodDimension 
	 * 
	 */
	public ComponentSpecification(ComponentProperty property){
		this.labels = new HashMap<>();
		this.property = property;
		this.name = property.getName() + "Specification";
	}
	
	@Override
	public String getUri(){
		
		return Constants.GEOSTATS_DATA_CUBE_NS + this.name;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract ComponentProperty getComponentProperty();

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
