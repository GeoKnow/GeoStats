package eu.geoknow.athenarc.triplegeo;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import au.com.bytecode.opencsv.CSVReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RegionalStatistikLinker {

	public static Model generateLinks(Model model) throws IOException {
		
//		Model links = ModelFactory.createDefaultModel();
		// codes which are no longer valid
		List<String> codesToSkip = Arrays.asList("05354","03241001","151","15101","15151","15153","15154","15159","15171","152","15202","15256","15260","15261","15265","15266","15268","153","15303","15352","15355","15357","15358","15362","15363","15364","15367","15369","15370","141","14161","14166","14167","14171","14173","14177","14178","14181","14182","14188","14191","14193","142","14262","14263","14264","14272","14280","14284","14285","14286","14287","14290","14292","143","14365","14374","14375","14379","14383","14389","13005","13006","13051","13052","13053","13054","13055","13056","13057","13058","13059","13060","13061","13062","13001","13002","10041100","05334002");
		
		Levenshtein levenshtein = new Levenshtein();
		
		List<Resource> federalStates  = getLayers(model, "http://dbpedia.org/ontology/FederalState");
		List<Resource> adminDistricts = getLayers(model, "http://dbpedia.org/ontology/AdministrativeDistrict");
		List<Resource> districts	  = getLayers(model, "http://dbpedia.org/ontology/District");

		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream("/Users/gerb/Development/workspaces/java/geostats/data/regionalstatistik/bevoelkerungstand-173-01-4.csv"), "ISO-8859-1"), ';');
	    for ( String[] line : reader.readAll() ) {
	    	
	    	String id = line[0].trim();
	    	String name = line[1].trim();
	    	
	    	if ( name.equals("Leipzig, Landkreis") ) name = "Landkreis Leipzig";
	    	else if ( name.equals("Karlsruhe, Landkreis") ) name = "Landkreis Karlsruhe";
	    	else if ( name.equals("Heilbronn, Landkreis") ) name = "Landkreis Heilbronn";
	    	else if ( name.equals("Kassel, Landkreis") ) name = "Landkreis Kassel";
	    	else if ( name.contains("Kreisfreie Stadt Rostock") ) name = "Rostock";
	    	else if ( name.equals("Kreisfreie Stadt Schwerin") ) name = "Schwerin";
	    	else {
	    		
	    		name = name.contains(",") ? name.substring(0, name.indexOf(",")) : name ;
	    	}
	    	
	    	if ( codesToSkip.contains(id) ) continue;
	    	
	    	// federal state
	    	if ( id.length() == 2 && StringUtils.isNumeric(id) ) {
	    		
	    		String uri = "";
	    		float bestMatch = 0F;
	    		
	    		for ( Resource r : federalStates ) {
	    			
	    			String resName = r.getProperty(RDFS.label).getString();
	    			float match = levenshtein.getSimilarity(name, resName); 
	    			if ( match >= bestMatch ) {
	    				bestMatch = match;
	    				uri = r.getURI();
	    			}
	    		}
	    		model.add(ResourceFactory.createResource(uri), ResourceFactory.createProperty(ShpToRdf.GEOSTATS_NS, "regionalStatistikId"), id);
	    	}
	    	// administrative district
	    	else if ( id.length() == 3 ) {

	    		String uri = "";
	    		float bestMatch = 0F;
	    		
	    		for ( Resource r : adminDistricts ) {
	    			
	    			String resName = r.getProperty(RDFS.label).getString();
	    			float match = levenshtein.getSimilarity(name, resName); 
	    			match = Math.max(match, levenshtein.getSimilarity("Regierungsbezirk " + name, resName));
	    			
	    			if ( match >= bestMatch ) {
	    				bestMatch = match;
	    				uri = r.getURI();
	    			}
	    		}
	    		model.add(ResourceFactory.createResource(uri), ResourceFactory.createProperty(ShpToRdf.GEOSTATS_NS, "regionalStatistikId"), id);
	    	}
	    	// anything else
	    	else if ( StringUtils.isNumeric(id) ) {
	    	
//	    		System.out.println(id + name);
	    		
	    		String dbpediaUri = "";
	    		float bestMatch = 0F;
	    		
	    		for ( Resource r : districts ) {
	    			
	    			String resName = r.getProperty(RDFS.label).getString().replaceAll("\\(.+?\\)", "");
	    			float match = levenshtein.getSimilarity(name, resName); 
	    			if ( match >= bestMatch ) {
	    				bestMatch = match;
	    				dbpediaUri = r.getURI();
	    			}
	    		}
	    		if ( id.equals("10041") ) 	   addTriple(model, "http://de.dbpedia.org/resource/Regionalverband_Saarbrücken", id, line[1].trim());
	    		else if ( id.equals("13004") ) addTriple(model, "http://de.dbpedia.org/resource/Schwerin", id, line[1].trim());
	    		else if ( id.equals("13072") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Rostock", id, line[1].trim());
	    		else if ( id.equals("09776") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Lindau_(Bodensee)", id, line[1].trim());
	    		else if ( id.equals("05515") ) addTriple(model, "http://de.dbpedia.org/resource/Münster_(Westfalen)", id, line[1].trim());
	    		else if ( id.equals("03458") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Oldenburg", id, line[1].trim()); 
	    		else if ( id.equals("03455") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Friesland", id, line[1].trim());
	    		else if ( id.equals("09571") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Ansbach", id, line[1].trim());
	    		else if ( id.equals("09661") ) addTriple(model, "http://de.dbpedia.org/resource/Aschaffenburg", id, line[1].trim());
	    		else if ( id.equals("09671") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Aschaffenburg", id, line[1].trim());
	    		else if ( id.equals("09761") ) addTriple(model, "http://de.dbpedia.org/resource/Augsburg", id, line[1].trim());
	    		else if ( id.equals("09772") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Augsburg", id, line[1].trim());
	    		else if ( id.equals("09461") ) addTriple(model, "http://de.dbpedia.org/resource/Bamberg", id, line[1].trim());
	    		else if ( id.equals("09471") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Bamberg", id, line[1].trim());
	    		else if ( id.equals("09462") ) addTriple(model, "http://de.dbpedia.org/resource/Bayreuth", id, line[1].trim());
	    		else if ( id.equals("09472") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Bayreuth", id, line[1].trim());
	    		else if ( id.equals("09463") ) addTriple(model, "http://de.dbpedia.org/resource/Coburg", id, line[1].trim());
	    		else if ( id.equals("09473") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Coburg", id, line[1].trim());
	    		else if ( id.equals("09563") ) addTriple(model, "http://de.dbpedia.org/resource/Fürth", id, line[1].trim());
	    		else if ( id.equals("09573") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Fürth", id, line[1].trim());
	    		else if ( id.equals("09464") ) addTriple(model, "http://de.dbpedia.org/resource/Hof_(Saale)", id, line[1].trim());
	    		else if ( id.equals("09475") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Hof", id, line[1].trim());
	    		else if ( id.equals("07312") ) addTriple(model, "http://de.dbpedia.org/resource/Kaiserslautern", id, line[1].trim());
	    		else if ( id.equals("07335") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Kaiserslautern", id, line[1].trim());
	    		else if ( id.equals("03403") ) addTriple(model, "http://de.dbpedia.org/resource/Oldenburg_(Oldenburg)", id, line[1].trim());
	    		else if ( id.equals("03458") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Oldenburg", id, line[1].trim());
	    		else if ( id.equals("09261") ) addTriple(model, "http://de.dbpedia.org/resource/Landshut", id, line[1].trim());
	    		else if ( id.equals("09274") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Landshut", id, line[1].trim());
	    		else if ( id.equals("09162") ) addTriple(model, "http://de.dbpedia.org/resource/München", id, line[1].trim());
	    		else if ( id.equals("09184") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_München", id, line[1].trim());
	    		else if ( id.equals("03404") ) addTriple(model, "http://de.dbpedia.org/resource/Osnabrück", id, line[1].trim());
	    		else if ( id.equals("03459") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Osnabrück", id, line[1].trim());
	    		else if ( id.equals("09262") ) addTriple(model, "http://de.dbpedia.org/resource/Passau", id, line[1].trim());
	    		else if ( id.equals("09275") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Passau", id, line[1].trim());
	    		else if ( id.equals("09362") ) addTriple(model, "http://de.dbpedia.org/resource/Regensburg", id, line[1].trim());
	    		else if ( id.equals("09375") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Regensburg", id, line[1].trim());
	    		else if ( id.equals("09163") ) addTriple(model, "http://de.dbpedia.org/resource/Rosenheim", id, line[1].trim());
	    		else if ( id.equals("09187") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Rosenheim", id, line[1].trim());
	    		else if ( id.equals("09662") ) addTriple(model, "http://de.dbpedia.org/resource/Schweinfurt", id, line[1].trim());
	    		else if ( id.equals("09678") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Schweinfurt", id, line[1].trim());
	    		else if ( id.equals("09663") ) addTriple(model, "http://de.dbpedia.org/resource/Würzburg", id, line[1].trim());
	    		else if ( id.equals("09679") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Würzburg", id, line[1].trim());
	    		else if ( id.equals("08426") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Biberach", id, line[1].trim());
	    		else if ( id.equals("03451") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Ammerland", id, line[1].trim());
	    		else if ( id.equals("09172") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Berchtesgadener_Land", id, line[1].trim());
	    		else if ( id.equals("07134") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Birkenfeld", id, line[1].trim());
	    		else if ( id.equals("15083") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Börde", id, line[1].trim());
	    		else if ( id.equals("09372") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Cham", id, line[1].trim());
	    		else if ( id.equals("03353") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Harburg", id, line[1].trim());
	    		else if ( id.equals("15085") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Harz", id, line[1].trim());
	    		else if ( id.equals("01053") ) addTriple(model, "http://de.dbpedia.org/resource/Kreis_Herzogtum_Lauenburg", id, line[1].trim());
	    		else if ( id.equals("03457") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Leer", id, line[1].trim());
	    		else if ( id.equals("09478") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Lichtenfels", id, line[1].trim());
	    		else if ( id.equals("10043") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Neunkirchen", id, line[1].trim());
	    		else if ( id.equals("06438") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Offenbach", id, line[1].trim());
	    		else if ( id.equals("03356") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Osterholz", id, line[1].trim());
	    		else if ( id.equals("03257") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Schaumburg", id, line[1].trim());
	    		else if ( id.equals("01060") ) addTriple(model, "http://de.dbpedia.org/resource/Kreis_Segeberg", id, line[1].trim());
	    		else if ( id.equals("01061") ) addTriple(model, "http://de.dbpedia.org/resource/Kreis_Steinburg", id, line[1].trim());
	    		else if ( id.equals("01062") ) addTriple(model, "http://de.dbpedia.org/resource/Lreis_Stormarn", id, line[1].trim());
	    		else if ( id.equals("03461") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Wesermarsch", id, line[1].trim());
	    		else if ( id.equals("12070") ) addTriple(model, "http://de.dbpedia.org/resource/Landkreis_Prignitz", id, line[1].trim());
	    		else {
	    			addTriple(model, dbpediaUri, id, line[1].trim());
	    		}
	    		
//	    		List<String> okay = Arrays.asList("Rendsburg-Eckernförde", "Städteregion Aachen (einschl. Stadt Aachen)", "Olpe", "Limburg-Weilburg", "Siegen-Wittgenstein",
//	    				"Neustadt a.d.Waldnaab", "Neumarkt i.d.OPf.", "Weiden i.d.OPf.", "Pfaffenhofen a.d.Ilm", "Mühldorf a.Inn", "",
//	    				"Dillingen a.d.Donau", "Neustadt a.d.Aisch-Bad Windsheim", "Saalfeld-Rudolstadt", "Schmalkalden-Meiningen", "Sächsische Schweiz-Osterzgebirge"
//	    				, "Landkreis Ludwigslust-Parchim", "Landkreis Vorpommern-Greifswald", "Landkreis Vorpommern-Rügen", "Kempten (Allgäu)", "Hof", "Frankenthal (Pfalz)", "Altenkirchen (Westerwald)", "Oldenburg (Oldenburg)", "Rotenburg (Wümme)",
//	    				"Nienburg (Weser)", "Frankfurt (Oder)", "Halle (Saale)", "Landkreis Rostock", "Kreisfreie Stadt Schwerin", "Saarbrücken", "Lindau (Bodensee)",
//	    				"Oldenburg", "Münster", "Friesland");
// 	    		
//	    		if ( bestMatch < 0.9 && !okay.contains(name)) {
//	    			
//	    			System.out.println(id + " -> " + name + " -> " + dbpediaUri);
//	    		}
	    	}
	    }
	    
	    return model;
	}
	
	private static void addTriple(Model model, String dbpediaUri, String id, String originalName) {
		
		Resource dbpedia  = ResourceFactory.createResource(dbpediaUri);
		Resource regional = ResourceFactory.createResource("http://www.regionalstatistik.de/genesis/resource/" + id);
		model.add(dbpedia, OWL.sameAs, regional);
		model.add(regional, RDFS.label, originalName);
	}

	private static List<Resource> getLayers(Model model, String type) {
		
		List<Resource> resources = new ArrayList<>();
		ResIterator listSubjectsWithProperty = model.listSubjectsWithProperty(RDF.type, ResourceFactory.createResource(type));
		while ( listSubjectsWithProperty.hasNext() ) {
			
			resources.add(listSubjectsWithProperty.next());
		}
		
		System.out.println("Size: "  + resources.size());
		
		return resources;
	}
}
