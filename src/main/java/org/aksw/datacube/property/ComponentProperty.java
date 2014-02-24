package org.aksw.datacube.property;

import java.util.HashMap;
import java.util.Map;

import org.aksw.Constants;
import org.aksw.datacube.AbstractResource;
import org.aksw.datacube.Language;

import com.hp.hpl.jena.rdf.model.Resource;

public class ComponentProperty extends AbstractResource {

	protected Map<Language, String> labels;
	protected String superProperty;
	protected String rdfsRange;
	protected String concept;
	protected String name;
	
	/**
	 * 
	 * @param name
	 */
	public ComponentProperty(Resource resource){
		super(resource);
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
		return this.resource.getLocalName();
	}

	/**
	 * 
	 * @param label
	 * @param language
	 */
	public void addLabel(String label, Language language) {
		this.labels.put(language, label);
	}
}
