/**
 * 
 */
package org.aksw.datacube;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.datacube.observation.Observation;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 *
 */
public class DataSet extends AbstractResource {
	
	private DataStructureDefinition structure;
	private Map<Language, String> labels;
	private Map<Language, String> comments;
	private String publisher;
	private String publishDate;
	private List<String> subjects;
	private List<Observation> observations;
	
	/**
	 * 
	 * @param resource
	 */
	public DataSet(Resource resource) {
		super(resource);
		
		this.labels = new HashMap<>();
		this.comments = new HashMap<>();
		this.subjects = new ArrayList<>();
		this.observations = new ArrayList<>();
	}

	/**
	 * @return the structure
	 */
	public DataStructureDefinition getStructure() {
		return structure;
	}
	/**
	 * @param structure the structure to set
	 */
	public void setStructure(DataStructureDefinition structure) {
		this.structure = structure;
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
	 * @return the comments
	 */
	public Map<Language, String> getComments() {
		return comments;
	}
	/**
	 * @param comments the comments to set
	 */
	public void setComments(Map<Language, String> comments) {
		this.comments = comments;
	}
	/**
	 * @return the publisher
	 */
	public String getPublisher() {
		return publisher;
	}
	/**
	 * @param publisher the publisher to set
	 */
	public void setPublisherUri(String publisher) {
		this.publisher = publisher;
	}
	/**
	 * @return the publisheDate
	 */
	public String getPublisheDate() {
		return publishDate;
	}
	/**
	 * @param publisheDate the publisheDate to set
	 */
	public void setPublishDate(String publisheDate) {
		this.publishDate = publisheDate;
	}
	/**
	 * @return the subjects
	 */
	public List<String> getSubjects() {
		return subjects;
	}
	/**
	 * @param subjects the subjects to set
	 */
	public void setSubjects(List<String> subjects) {
		this.subjects = subjects;
	}
	
	public List<Observation> getObservations(String uri) {
		
		List<Observation> obs = new ArrayList<>();

		for ( Observation ob : this.observations ) if ( ob.getUri().equals(uri) ) obs.add(ob);
		
		return obs;
	}
	
	/**
	 * @return the observations
	 */
	public List<Observation> getObservations() {
		return observations;
	}
	/**
	 * @param observations the observations to set
	 */
	public void setObservations(List<Observation> observations) {
		this.observations = observations;
	}
	
	/**
	 * 
	 * @param label
	 * @param language
	 */
	public void addComment(String comment, Language language) {
		this.comments.put(language, comment);
	}
	
	/**
	 * 
	 * @param label
	 * @param language
	 */
	public void addLabel(String label, Language language) {
		this.labels.put(language, label);
	}

	/**
	 * 
	 * @param subjectUri
	 */
	public void addSubjectUri(String subjectUri) {
		this.subjects.add(subjectUri);
	}
	
	/**
	 * 
	 * @param obs
	 */
	public void addObservation(Observation obs) {
		this.observations.add(obs);
	}

	/**
	 * 
	 * @param observations
	 */
	public void addAllObservations(List<Observation> observations) {
		this.observations.addAll(observations);
	}
}
