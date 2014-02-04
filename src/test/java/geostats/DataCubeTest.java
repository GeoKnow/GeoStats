package geostats;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.aksw.datacube.DataSet;
import org.aksw.datacube.DataStructureDefinition;
import org.aksw.datacube.Language;
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
		
		DataStructureDefinition structure = new DataStructureDefinition();
		structure.addDimensionSpecification(refPeriod);
		structure.addDimensionSpecification(refAreaDimension);
		structure.addMeasureSpecification(beantragteInsolvenzverfahrenMeasure);
		
		DataSet dataset = new DataSet(structure);
		dataset.addLabel("This is a dataset!", Language.en);
		dataset.addLabel("Das ist ein Datensatz!", Language.de);
		dataset.addLabel("This is a comment!", Language.en);
		dataset.addLabel("Das ist ein Kommentar!", Language.de);
		dataset.setPublishDate("2014-02-04");
		dataset.setPublisherUri("http://semanticweb.org/id/AKSW");
		dataset.addSubjectUri("http://purl.org/linked-data/sdmx/2009/subject#3.2");
		
		Observation obs = new Observation();
		obs.addObservationValue(beantragteInsolvenzverfahrenMeasure.getComponentProperty(), new ObservationValue("157", "http://www.w3.org/2001/XMLSchema#int"));
		
		dataset.addObservation(obs);
	}
	
	@Test
	public void testReadModel(){
			
		Model model = FileManager.get().loadModel("data/sparqlify/insolvenzen/insolvenzen-kreisebene-325-31-4.ttl", "TURTLE");
		List<DataSet> datasets = RdfToDataCube.generate(RdfToDataCube.createQueryExecutionFactory(model));
		
		System.out.println(datasets.get(0).getObservations().size());
	}

	private MeasureComponentSpecification generateInsolvenzenSpecification() {
		
		MeasureProperty totalInsolvenciesDimension = new MeasureProperty("beantragteInsolvenzverfahren");
		totalInsolvenciesDimension.setRdfsRangeUri("http://www.w3.org/2001/XMLSchema#integer");
		totalInsolvenciesDimension.setSuperPropertyUri("http://purl.org/linked-data/sdmx/2009/measure#obsValue");
		totalInsolvenciesDimension.addLabel("Total insolvencies registerd", Language.en);
		totalInsolvenciesDimension.addLabel("Beantragte Insolvenzverfahren Insgesamt", Language.de);
		
		MeasureComponentSpecification insolvenciesSpecification = new MeasureComponentSpecification(totalInsolvenciesDimension);
		insolvenciesSpecification.addLabel("Insolvencies (Total) Specification", Language.en);
		insolvenciesSpecification.addLabel("Insolvenzen (Total) Spezifikation", Language.de);
		
		return insolvenciesSpecification;
	}

	private DimensionComponentSpecification generateRefPeriodDimensionSpecification() {
		
		DimensionProperty refPeriodDimension = new DimensionProperty("refPeriod");
		refPeriodDimension.setConceptUri("http://purl.org/linked-data/sdmx/2009/concept#refPeriod");
		refPeriodDimension.setRdfsRangeUri("http://reference.data.gov.uk/def/intervals/Interval");
		refPeriodDimension.setSuperPropertyUri("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod");
		refPeriodDimension.addLabel("Place", Language.en);
		refPeriodDimension.addLabel("Ort", Language.de);
		
		DimensionComponentSpecification refPeriod = new DimensionComponentSpecification(refPeriodDimension);
		refPeriod.addLabel("Time of Measure", Language.en);
		refPeriod.addLabel("Zeit der Messung", Language.de);
		refPeriod.setOrder(2);
		
		return refPeriod;
	}

	private DimensionComponentSpecification generateRefAreaDimensionSpecification() {
		
		DimensionProperty refAreaDimension = new DimensionProperty("refArea");
		refAreaDimension.setConceptUri("http://purl.org/linked-data/sdmx/2009/concept#refArea");
		refAreaDimension.setRdfsRangeUri("http://data.ordnancesurvey.co.uk/ontology/admingeo//UnitaryAuthority");
		refAreaDimension.setSuperPropertyUri("http://purl.org/linked-data/sdmx/2009/dimension#refArea");
		refAreaDimension.addLabel("Place", Language.en);
		refAreaDimension.addLabel("Ort", Language.de);
		
		DimensionComponentSpecification refArea = new DimensionComponentSpecification(refAreaDimension);
		refArea.addLabel("Place of Measure", Language.en);
		refArea.addLabel("Ort der Messung", Language.de);
		refArea.setOrder(1);
		
		return refArea;
	}
}
