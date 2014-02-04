package org.aksw.datacube.property;

import java.util.HashMap;
import java.util.Map;

import org.aksw.Constants;
import org.aksw.datacube.Language;
import org.aksw.datacube.Resource;

public class ComponentProperty implements Resource {

	protected Map<Language, String> labels;
	protected String superProperty;
	protected String rdfsRange;
	protected String concept;
	protected String name;
	
	/**
	 * 
	 * @param name
	 */
	public ComponentProperty(String name){
		this.name = name;
		this.labels = new HashMap<>();
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

	/**
	 * @return the superProperty
	 */
	public String getSuperProperty() {
		return superProperty;
	}

	/**
	 * @param superProperty the superProperty to set
	 */
	public void setSuperPropertyUri(String superProperty) {
		this.superProperty = superProperty;
	}

	/**
	 * @return the rdfsRange
	 */
	public String getRdfsRange() {
		return rdfsRange;
	}

	/**
	 * @param rdfsRange the rdfsRange to set
	 */
	public void setRdfsRangeUri(String rdfsRange) {
		this.rdfsRange = rdfsRange;
	}

	/**
	 * @return the concept
	 */
	public String getConcept() {
		return concept;
	}

	/**
	 * @param concept the concept to set
	 */
	public void setConceptUri(String concept) {
		this.concept = concept;
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
	 * 
	 * @param label
	 * @param language
	 */
	public void addLabel(String label, Language language) {
		this.labels.put(language, label);
	}

	@Override
	public String getUri() {
		
		return Constants.GEOSTATS_DATA_CUBE_NS + this.name;
	}
}
