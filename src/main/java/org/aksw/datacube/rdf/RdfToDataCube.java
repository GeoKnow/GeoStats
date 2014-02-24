package org.aksw.datacube.rdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.Constants;
import org.aksw.datacube.DataSet;
import org.aksw.datacube.DataStructureDefinition;
import org.aksw.datacube.Language;
import org.aksw.datacube.factory.DataCubeFactory;
import org.aksw.datacube.observation.Observation;
import org.aksw.datacube.observation.ObservationValue;
import org.aksw.datacube.property.DimensionProperty;
import org.aksw.datacube.property.MeasureProperty;
import org.aksw.datacube.spec.AttributeComponentSpecification;
import org.aksw.datacube.spec.DimensionComponentSpecification;
import org.aksw.datacube.spec.MeasureComponentSpecification;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.commons.lang3.StringUtils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

import eu.geoknow.athenarc.triplegeo.RdfExport;

public class RdfToDataCube {
	
	/**
	 * 
	 * @param createDefaultModel
	 * @return
	 */
	public static Map<String,DataSet> read(QueryExecutionFactory qef) {
		
		QueryExecution qe = qef.createQueryExecution(
				RdfExport.getNamespaceMappingForQuery() +
				"SELECT ?dataSet ?label ?date ?publisher ?subject ?structure ?comment { " +
					" ?dataSet a qb:DataSet . " + 
					" ?dataSet qb:structure ?structure . " +
					" OPTIONAL { ?dataSet rdfs:label ?label } " +
					" OPTIONAL { ?dataSet rdfs:comment ?comment } " +
					" OPTIONAL { ?dataSet dct:issued ?date } " +
					" OPTIONAL { ?dataSet dct:publisher ?publisher } " +
					" OPTIONAL { ?dataSet dct:subject ?subject } " + 
				"}");
		
		ResultSet rs = qe.execSelect();
		Map<String,DataSet> dataSets = new HashMap<>();
		
		while ( rs.hasNext() ) {
			
			QuerySolution next = rs.next();
			
			String dataSetUri   = next.get("dataSet").asResource().getURI();
			String structureUri = next.get("structure").asResource().getURI();
			
			DataStructureDefinition structure = DataCubeFactory.getInstance().createDataStructureDefinition(structureUri);
			structure.addAllDimensionSpecification(getDimensionComponentSpecifications(qef, structureUri));
			structure.addAllMeasureSpecification(getMeasureComponentSpecifications(qef, structureUri));
			structure.addAllAttributeSpecification(getAttributeComponentSpecifications(qef, structureUri));
			
			DataSet dataSet = DataCubeFactory.getInstance().createDataSet(dataSetUri);
			dataSet.setStructure(structure);
			if ( next.contains("date") ) dataSet.setPublishDate(next.get("date").asLiteral().getLexicalForm());
			if ( next.contains("publisher") ) dataSet.setPublisherUri(next.get("publisher").asResource().getURI());
			if ( next.contains("label") ) dataSet.addLabel(next.get("label").asLiteral().getLexicalForm(), Language.valueOf(next.get("label").asLiteral().getLanguage()));
			if ( next.contains("comment") ) dataSet.addComment(next.get("comment").asLiteral().getLexicalForm(), Language.valueOf(next.get("comment").asLiteral().getLanguage()));
			if ( next.contains("subject") ) dataSet.setSubjects(new ArrayList<>(Arrays.asList(next.get("subject").asResource().getURI())));
			
			dataSet.addAllObservations(getObservations(qef, dataSet));
			
			dataSets.put(dataSet.getUri(), dataSet);
		}
		
		qe.close();
		
		return dataSets;
	}
	
	/**
	 * 
	 * @param sparqlEndpoint
	 * @param defaultGraph
	 * @return
	 */
	public static QueryExecutionFactory createQueryExecutionFactory(String sparqlEndpoint, String defaultGraph) {
		
		if ( defaultGraph.isEmpty() ) return new QueryExecutionFactoryHttp(sparqlEndpoint);
		else return new QueryExecutionFactoryHttp(sparqlEndpoint, defaultGraph);
	}
	
	/**
	 * 
	 * @param model
	 * @return
	 */
	public static QueryExecutionFactory createQueryExecutionFactory(Model model) {
		return new QueryExecutionFactoryModel(model);
	}

	/**
	 * 
	 * @param qef
	 * @param dataset
	 * @param dataSetUri
	 * @return
	 */
	private static List<Observation> getObservations(QueryExecutionFactory qef, DataSet dataset) {
		
		List<String> variables = new ArrayList<>();
		String dimensionsAndMeasuresPart = "";
		for ( DimensionComponentSpecification spec : dataset.getStructure().getDimensionSpecifications() ) {
			
			dimensionsAndMeasuresPart += String.format(" ?observation <%s> ?%s .\n", spec.getComponentProperty().getUri(), spec.getComponentProperty().getName());
			variables.add("?" + spec.getComponentProperty().getName());
		}
		for ( MeasureComponentSpecification spec : dataset.getStructure().getMeasureSpecifications() ) {
			
			dimensionsAndMeasuresPart += String.format(" ?observation <%s> ?%s .\n", spec.getComponentProperty().getUri(), spec.getComponentProperty().getName());
			variables.add("?" + spec.getComponentProperty().getName());
		}
		
		String query = RdfExport.getNamespaceMappingForQuery() +
			"SELECT ?observation "+ StringUtils.join(variables, " ")+" { " +
				" ?observation a qb:Observation . " +
				" ?observation qb:dataSet <"+dataset.getUri()+"> . " +
				dimensionsAndMeasuresPart +
			"}";
		
		ResultSet rs = qef.createQueryExecution(query).execSelect();
		
		List<Observation> observations = new ArrayList<>();
		
		while ( rs.hasNext() ) {
			
			QuerySolution next = rs.next();
			Observation obs = DataCubeFactory.getInstance().createObservation(next.get("observation").asResource().getURI());
			obs.addAllDimensionProperties(dataset.getStructure().getDimensionSpecifications());
			
			for ( MeasureComponentSpecification spec : dataset.getStructure().getMeasureSpecifications() ) {
				
				String variable = spec.getComponentProperty().getName();
				String value = next.get(variable).asLiteral().getLexicalForm();
				String type = next.get(variable).asLiteral().getDatatypeURI();
				obs.addObservationValue(spec.getComponentProperty(), new ObservationValue(value, type));
			}
			
			observations.add(obs);
		}
		
		return observations;
	}

	/**
	 * 
	 * @param qef
	 * @param structureUri
	 * @return
	 */
	private static List<AttributeComponentSpecification> getAttributeComponentSpecifications(QueryExecutionFactory qef, String structureUri) {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	/**
	 * 
	 * @param qef
	 * @param structureUri
	 * @return
	 */
	private static List<MeasureComponentSpecification> getMeasureComponentSpecifications(QueryExecutionFactory qef, String structureUri) {
		
		QueryExecution measureQE = qef.createQueryExecution(
				RdfExport.getNamespaceMappingForQuery() +
				"SELECT * { " +
					" <"+structureUri+"> qb:component ?spec . " +
					" ?spec qb:measure ?measure . " +
					" ?spec rdfs:label ?specLabel . "  +
					" ?measure rdfs:label ?label . " +
					" ?measure rdfs:range ?range . " +
					" ?measure rdfs:subPropertyOf ?subPropertyOf . " +
					" FILTER( lang(?specLabel) = 'de' && lang(?label) = 'de' ) " +
				"}" +
				"ORDER BY ?spec");
		
		ResultSet rs = measureQE.execSelect();
		List<MeasureComponentSpecification> measureSpecs = new ArrayList<>();
		
		while ( rs.hasNext() ) {
			
			QuerySolution next = rs.next();
			
			MeasureProperty measureProperty = DataCubeFactory.getInstance().createMeasureProperty(next.get("?measure").asResource().getURI());
			measureProperty.setRdfsRangeUri(next.get("?range").asResource().getURI());
			measureProperty.setSuperPropertyUri(next.get("?subPropertyOf").asResource().getURI());
			measureProperty.addLabel(next.get("?label").asLiteral().getLexicalForm(), Language.valueOf(next.get("?label").asLiteral().getLanguage()));
			
			MeasureComponentSpecification insolvenciesSpecification = DataCubeFactory.getInstance().createMeasureComponentSpecification(next.get("?spec").asResource().getURI());
			insolvenciesSpecification.setComponentProperty(measureProperty);
			insolvenciesSpecification.addLabel(next.get("?specLabel").asLiteral().getLexicalForm(), Language.valueOf(next.get("?specLabel").asLiteral().getLanguage()));
			
			measureSpecs.add(insolvenciesSpecification);
		}
		
		return measureSpecs;
	}

	/**
	 * 
	 * @param qef
	 * @param structureUri
	 * @return
	 */
	private static List<DimensionComponentSpecification> getDimensionComponentSpecifications(QueryExecutionFactory qef, String structureUri) {
		
		QueryExecution measureQE = qef.createQueryExecution(
				RdfExport.getNamespaceMappingForQuery() +
				"SELECT * { " +
					" <"+structureUri+"> qb:component ?spec . " +
					" ?spec qb:dimension ?measure . " +
					" ?spec rdfs:label ?specLabel . "  +
					" ?measure rdfs:label ?label . " +
					" ?measure rdfs:range ?range . " +
					" ?measure rdfs:subPropertyOf ?subPropertyOf . " +
					" FILTER( lang(?specLabel) = 'de' && lang(?label) = 'de' ) " +
				"}" +
				"ORDER BY ?spec");
		
		ResultSet rs = measureQE.execSelect();
		List<DimensionComponentSpecification> dimensionSpecs = new ArrayList<>();
		
		while ( rs.hasNext() ) {
			
			QuerySolution next = rs.next();
			
			DimensionProperty dimension = DataCubeFactory.getInstance().createDimensionProperty(next.get("?measure").asResource().getURI()); 
			dimension.setRdfsRangeUri(next.get("?range").asResource().getURI());
			dimension.setSuperPropertyUri(next.get("?subPropertyOf").asResource().getURI());
			dimension.addLabel(next.get("?label").asLiteral().getLexicalForm(), Language.valueOf(next.get("?label").asLiteral().getLanguage()));
			
			DimensionComponentSpecification dimensionSpecification = DataCubeFactory.getInstance().createDimensionComponentSpecification(next.get("?spec").asResource().getURI());
			dimensionSpecification.setComponentProperty(dimension);
			dimensionSpecification.addLabel(next.get("?specLabel").asLiteral().getLexicalForm(), Language.valueOf(next.get("?specLabel").asLiteral().getLanguage()));
			
			dimensionSpecs.add(dimensionSpecification);
		}
		
		return dimensionSpecs;
	}
}
