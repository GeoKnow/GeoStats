/**
 * 
 */
package eu.geoknow.athenarc.triplegeo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;

import au.com.bytecode.opencsv.CSVReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import eu.geoknow.athenarc.triplegeo.utils.UtilsLib;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class NutsLoader extends DefaultShapeFileLoader {
	
	public NutsLoader(Model model) {
		
//		super.featureAttribute = "ID_1";
//		super.featureClass = "TYPE_1";
//		super.featureName = "NAME_1";
		super.path = "data/nuts/";
		super.fileName = "NUTS_RG_03M_2010";
		super.model = model;
//		super.areaType = "state";
		super.getShapeFileFeatureCollection();
	}
	
//	public static void main(String[] args) throws IOException, InterruptedException {
//		
//		new NutsLoader(null).generateRDF();
//	}

	@Override
	public void generateRDF() throws IOException, InterruptedException {
		
		Map<String,String> idToLabels = readLabels();
		
		Model nutsModel = FileManager.get().loadModel("/Users/gerb/Development/workspaces/data/geostats/nuts/nuts-rdf-0.91.ttl");
		nutsModel.add(model);
		FeatureIterator iterator = this.featureCollection.features();
		
		try {

			System.out.println(UtilsLib.getGMTime() + " Started processing features...");

			while (iterator.hasNext()) {
				SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				
				String level = feature.getAttribute("STAT_LEVL_").toString();
				String code  = feature.getAttribute("NUTS_ID").toString();
				String uri   = "http://nuts.geovocab.org/id/" + code;
				
				List<String> codes = Arrays.asList("DE80I","DE80H","DE80G","DE80F","DE80E","DE80D","DE80C","DE80B","DE80A","DE809","DE808","DE807","DE806","DE805","DE801","DE802", "DE256");
				if ( codes.contains(code) ) continue;
				
//				if ( name.equals("Rostock") ) insert(model, name, "DE803", "3", (MultiPolygon) geometry);
//				if ( name.equals("Schwerin") ) insert(model, name, "DE804", "3", (MultiPolygon) geometry);
//				if ( name.equals("Mecklenburgische Seenplatte") ) insert(model, name, "DE80J", "3", (MultiPolygon) geometry);
//				if ( name.equals("Landkreis Rostock") ) insert(model, name, "DE80K", "3", (MultiPolygon) geometry);
//				if ( name.equals("Vorpommern-Rügen") ) insert(model, name, "DE80L", "3", (MultiPolygon) geometry);
//				if ( name.equals("Nordwestmecklenburg") ) insert(model, name, "DE80M", "3", (MultiPolygon) geometry);
//				if ( name.equals("Vorpommern-Greifswald") ) insert(model, name, "DE80N", "3", (MultiPolygon) geometry);
//				if ( name.equals("Ludwigslust-Parchim") ) insert(model, name, "DE80O", "3", (MultiPolygon) geometry);
//				if ( name.equals("Heidekreis") ) insert(model, name, "DE938", "3", (MultiPolygon) geometry);
//				if ( name.equals("Vulkaneifel") ) insert(model, name, "DEB24", "3", (MultiPolygon) geometry);
//				if ( name.equals("Regionalverband Saarbrücken") ) insert(model, name, "DEC01", "3", (MultiPolygon) geometry);
				
				
				if ( !code.startsWith("DE") || code.equals("DE")  ) continue;
				
				Resource subject = ResourceFactory.createResource(uri);
				
				model.add(subject, ResourceFactory.createProperty("http://rdfdata.eionet.europa.eu/ramon/ontology/level"), level);
				model.add(subject, ResourceFactory.createProperty("http://rdfdata.eionet.europa.eu/ramon/ontology/code"), code);
				
				MultiPolygon multiPolygon = (MultiPolygon) geometry;
				for (int i = 0; i < multiPolygon.getNumGeometries(); ++i)
					RdfExport.insertPolygon(model, uri, multiPolygon.getGeometryN(i));
				
				String sparql = "SELECT * { <"+uri+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label . }";  

				Query qry = QueryFactory.create(sparql);
				QueryExecution qe = QueryExecutionFactory.create(qry, nutsModel);
				ResultSet rs = qe.execSelect();
				String label = "";
				
				while ( rs.hasNext() ) {
					
					label = fixLabel(rs.next().get("label").asLiteral().getLexicalForm());
					break;
				}
				if ( label.isEmpty() ) label = fixLabel(idToLabels.get(code));
				
				model.add(subject, RDFS.label, label);
			}
		}
		catch ( Exception e) {
			
			e.printStackTrace();
		}
	}

	private String fixLabel(String label) {
		
		label = label.replaceAll(".*- ", "").replace(", Landkreis", "").replace(", Kreisfreie Stadt", "").replace(",Kreisfreie Stadt", "").replace(", Stadtkreis", "");
		if ( !label.contains("-") && !label.contains("(") ) label = WordUtils.capitalizeFully(label);
		label = label.replace("A.d.", "an der");
		label = label.replace("An Der", "an der");
		label = label.replace("In Der", "in der");
		label = label.replace("A. D.", "an der");
		label = label.replace("A.", "am");
		label = label.replace("I. D. Opf.", "in der Oberpfalz");
		label = label.replace("I. D. Opf", "in der Oberpfalz");
		label = label.replace("I.", "im");
		label = label.replace(" Am ", " am ");
		label = label.replace(" Im ", " im ");
		label = label.replace("RHEINLAND-PFALZ", "Rheinland-Pfalz");
		label = label.replace("SACHSEN-ANHALT", "Sachsen-Anhalt");
		label = label.replace("MECKLENBURG-VORPOMMERN", "Mecklenburg-Vorpommern");
		label = label.replace("BADEN-WÜRTTEMBERG", "Baden-Württemberg");
		label = label.replace("SCHLESWIG-HOLSTEIN", "Schleswig-Holstein");
		label = label.replace("NORDRHEIN-WESTFALEN", "Nordrhein-Westfalen");
		
		return label;
	}

	private Map<String, String> readLabels() throws IOException {
		
		Map<String, String> idToLabels = new HashMap<>();
		
		CSVReader reader = new CSVReader(new FileReader("data/nuts/NUTS_2010.csv"), ',');
		
		for ( String[] line : reader.readAll()) idToLabels.put(line[0],line[1]);
		
		return idToLabels;
	}
}