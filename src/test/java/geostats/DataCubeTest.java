package geostats;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.aksw.Constants;
import org.aksw.datacube.DataSet;
import org.aksw.datacube.DataStructureDefinition;
import org.aksw.datacube.Language;
import org.aksw.datacube.factory.DataCubeFactory;
import org.aksw.datacube.observation.ObservationValue;
import org.aksw.datacube.observation.Observation;
import org.aksw.datacube.property.DimensionProperty;
import org.aksw.datacube.property.MeasureProperty;
import org.aksw.datacube.rdf.DataCubeToRdf;
import org.aksw.datacube.rdf.RdfToDataCube;
import org.aksw.datacube.spec.DimensionComponentSpecification;
import org.aksw.datacube.spec.MeasureComponentSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class DataCubeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateModel() {
		
		DimensionComponentSpecification refAreaDimension = generateRefAreaDimensionSpecification();
		DimensionComponentSpecification refPeriod = generateRefPeriodDimensionSpecification();
		MeasureComponentSpecification beantragteInsolvenzverfahrenMeasure = generateInsolvenzenSpecification();
		
		DataStructureDefinition structure = DataCubeFactory.getInstance().createDataStructureDefinition(null);
		structure.addDimensionSpecification(refPeriod);
		structure.addDimensionSpecification(refAreaDimension);
		structure.addMeasureSpecification(beantragteInsolvenzverfahrenMeasure);
		
		DataSet dataset = DataCubeFactory.getInstance().createDataSet(null);
		dataset.addLabel("This is a dataset!", Language.en);
		dataset.addLabel("Das ist ein Datensatz!", Language.de);
		dataset.addLabel("This is a comment!", Language.en);
		dataset.addLabel("Das ist ein Kommentar!", Language.de);
		dataset.setPublishDate("2014-02-04");
		dataset.setPublisherUri("http://semanticweb.org/id/AKSW");
		dataset.addSubjectUri("http://purl.org/linked-data/sdmx/2009/subject#3.2");
		
		Observation obs = DataCubeFactory.getInstance().createObservation(null);
		obs.addObservationValue(beantragteInsolvenzverfahrenMeasure.getComponentProperty(), new ObservationValue("157", "http://www.w3.org/2001/XMLSchema#int"));
		dataset.addObservation(obs);
	}
	
	@Test
	public void testReadModel(){
			
		Model model = FileManager.get().loadModel("data/sparqlify/insolvenzen/insolvenzen-kreisebene-325-31-4.ttl", "TURTLE");
		Map<String,DataSet> datasets = RdfToDataCube.read(RdfToDataCube.createQueryExecutionFactory(model));
		
		assertTrue(datasets.containsKey("http://geostats.aksw.org/qb/GeoStatsInsolvenzen"));
		
		DataSet dataset = datasets.get("http://geostats.aksw.org/qb/GeoStatsInsolvenzen");
		assertTrue(dataset.getStructure() != null);
		assertEquals("There should be 2 dimensions defined!" , 2, dataset.getStructure().getDimensionSpecifications().size());
		assertEquals("There should be 6 measures defined!" , 6, dataset.getStructure().getMeasureSpecifications().size());
		assertEquals("There should be 525 observations" , 525, dataset.getObservations().size());
		
	}

	private MeasureComponentSpecification generateInsolvenzenSpecification() {
		
		MeasureProperty totalInsolvenciesDimension = DataCubeFactory.getInstance().createMeasureProperty(Constants.GEOSTATS_DATA_CUBE_NS + "beantragteInsolvenzverfahren");
		totalInsolvenciesDimension.setRdfsRangeUri("http://www.w3.org/2001/XMLSchema#integer");
		totalInsolvenciesDimension.setSuperPropertyUri("http://purl.org/linked-data/sdmx/2009/measure#obsValue");
		totalInsolvenciesDimension.addLabel("Total insolvencies registerd", Language.en);
		totalInsolvenciesDimension.addLabel("Beantragte Insolvenzverfahren Insgesamt", Language.de);
		
		MeasureComponentSpecification insolvenciesSpecification = DataCubeFactory.getInstance().createMeasureComponentSpecification(Constants.GEOSTATS_DATA_CUBE_NS + "beantragteInsolvenzverfahrenSpecification");
		insolvenciesSpecification.setComponentProperty(totalInsolvenciesDimension);
		insolvenciesSpecification.addLabel("Insolvencies (Total) Specification", Language.en);
		insolvenciesSpecification.addLabel("Insolvenzen (Total) Spezifikation", Language.de);
		
		return insolvenciesSpecification;
	}

	private DimensionComponentSpecification generateRefPeriodDimensionSpecification() {
		
		DimensionProperty refPeriodDimension = DataCubeFactory.getInstance().createDimensionProperty(Constants.GEOSTATS_DATA_CUBE_NS + "refPeriod");
		refPeriodDimension.setConceptUri("http://purl.org/linked-data/sdmx/2009/concept#refPeriod");
		refPeriodDimension.setRdfsRangeUri("http://reference.data.gov.uk/def/intervals/Interval");
		refPeriodDimension.setSuperPropertyUri("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod");
		refPeriodDimension.addLabel("Place", Language.en);
		refPeriodDimension.addLabel("Ort", Language.de);
		
		DimensionComponentSpecification refPeriod = DataCubeFactory.getInstance().createDimensionComponentSpecification(Constants.GEOSTATS_DATA_CUBE_NS + "refPeriodSpecification");
		refPeriod.setComponentProperty(refPeriodDimension);
		refPeriod.addLabel("Time of Measure", Language.en);
		refPeriod.addLabel("Zeit der Messung", Language.de);
		refPeriod.setOrder(2);
		
		return refPeriod;
	}

	private DimensionComponentSpecification generateRefAreaDimensionSpecification() {
		
		DimensionProperty refAreaDimension = DataCubeFactory.getInstance().createDimensionProperty(Constants.GEOSTATS_DATA_CUBE_NS + "refArea");
		refAreaDimension.setConceptUri("http://purl.org/linked-data/sdmx/2009/concept#refArea");
		refAreaDimension.setRdfsRangeUri("http://data.ordnancesurvey.co.uk/ontology/admingeo//UnitaryAuthority");
		refAreaDimension.setSuperPropertyUri("http://purl.org/linked-data/sdmx/2009/dimension#refArea");
		refAreaDimension.addLabel("Place", Language.en);
		refAreaDimension.addLabel("Ort", Language.de);
		
		DimensionComponentSpecification refArea = DataCubeFactory.getInstance().createDimensionComponentSpecification(Constants.GEOSTATS_DATA_CUBE_NS + "refAreaSpecification");
		refArea.setComponentProperty(refAreaDimension);
		refArea.addLabel("Place of Measure", Language.en);
		refArea.addLabel("Ort der Messung", Language.de);
		refArea.setOrder(1);
		
		return refArea;
	}
}
