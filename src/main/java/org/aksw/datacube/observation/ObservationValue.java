package org.aksw.datacube.observation;

public class ObservationValue {
	
	private final String type;
	private final String value;
	
	/**
	 * 
	 * @param value
	 * @param typeUri
	 */
	public ObservationValue(String value, String typeUri){
		this.value = value;
		this.type = typeUri;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}
