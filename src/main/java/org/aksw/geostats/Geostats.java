package org.aksw.geostats;

/*
 * @(#) ShpToRdf.java	version 1.0   8/2/2013
 *
 * Copyright (C) 2013 Institute for the Management of Information Systems, Athena RC, Greece.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aksw.geostats.json.JsonDataGenerator;
import org.aksw.geostats.lgd.LinkedGeoDataStatistik;
import org.aksw.geostats.linking.NutsDBpediaLinker;
import org.aksw.geostats.linking.RegionalStatistikLinker;
import org.aksw.geostats.rdf.RdfExport;
import org.aksw.geostats.shape.ExtraWurstExtractor;
import org.aksw.geostats.shape.NutsLoader;
import org.aksw.geostats.sparqlify.SparqlifyRegionalStatistik;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.antlr.runtime.RecognitionException;
import org.json.JSONException;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.vividsolutions.jts.io.ParseException;

/**
 * Entry point to convert shapefiles into RDF triples.
 * 
 */
public class Geostats {
	
	public static final String GEOSTATS_NS = "http://geostats.aksw.org/";

	public static void main(String[] args) throws IOException, InterruptedException, JSONException, ParseException, ClassNotFoundException, SQLException, RecognitionException, ExecutionException {
		
		Model model = RdfExport.getModel();
		
		System.out.println("Generating Backend Data!");
		ExtraWurstExtractor.extract(model);
		
		NutsLoader.extract(model);
		NutsDBpediaLinker nutsDBpediaLinker = new NutsDBpediaLinker(model);
		nutsDBpediaLinker.linkDistricts();
		nutsDBpediaLinker.linkAdministrativeDistricts();
		nutsDBpediaLinker.linkFederalStates();
		
		RegionalStatistikLinker.generateLinks(model);
		addManualTripels(model);
		RdfExport.write(model, "data/geostats.ttl");
		
		SparqlifyRegionalStatistik.run();
//		LinkedGeoDataStatistik.run();
		
		System.out.println("Generating View Data!");
		JsonDataGenerator.generate();
	}

	private static void addManualTripels(Model model) throws ClassNotFoundException, SQLException {
		
		QueryExecutionFactory sparql = new QueryExecutionFactoryModel(model);
		CacheFrontend cache = new CacheFrontendImpl(CacheCoreH2.create("localhost", 150l * 60l * 60l * 1000l, false));
		QueryExecutionFactory qef = new QueryExecutionFactoryCacheEx(sparql, cache);
		
		List<String> wrongImages = new ArrayList<>();
		ResultSet rs = qef.createQueryExecution("SELECT ?image FROM <http://geostats.aksw.org> WHERE { ?s a <http://dbpedia.org/ontology/Place> . ?s <http://dbpedia.org/ontology/thumbnail> ?image }").execSelect();
		while ( rs.hasNext() ) {
			String uri = rs.next().get("image").asResource().getURI();
			if ( !uri.toLowerCase().contains("wappen") && !uri.toLowerCase().contains("Coat") ) 
				wrongImages.add(uri);
		}
		
		for ( String imageUri : wrongImages) {
			model.removeAll(null, ResourceFactory.createProperty("http://dbpedia.org/ontology/thumbnail"), ResourceFactory.createResource(imageUri));
		}
		
		add(model, "Ortenaukreis", "http://upload.wikimedia.org/wikipedia/commons/6/62/De_ortenau_coat.png");
		add(model, "Landkreis_Offenbach", "http://upload.wikimedia.org/wikipedia/commons/c/c4/Wappen_Kreis_Offenbach.png");
		add(model, "Rhein-Lahn-Kreis", "http://upload.wikimedia.org/wikipedia/commons/3/34/Wappen_Rhein-Lahn-Kreis.png");
		add(model, "Dachau", "http://upload.wikimedia.org/wikipedia/commons/5/5d/Dachau.jpg");
		add(model, "Pfaffenhofen_an_der_Ilm", "http://upload.wikimedia.org/wikipedia/commons/2/27/Wappen_paf.JPG");
		add(model, "Freising", "http://upload.wikimedia.org/wikipedia/commons/4/48/Wappen_Freising.png");
		add(model, "Region_Hannover", "http://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Wappen_der_Region_Hannover.svg/200px-Wappen_der_Region_Hannover.svg.png");
		add(model, "Landkreis_Vorpommern-Greifswald", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Wappen_Landkreis_Vorpommern-Greifswald.svg/200px-Wappen_Landkreis_Vorpommern-Greifswald.svg.png");
		add(model, "Erding", "http://upload.wikimedia.org/wikipedia/commons/thumb/1/14/Coa_de-by-erding.svg/200px-Coa_de-by-erding.svg.png");
		add(model, "Forchheim", "http://upload.wikimedia.org/wikipedia/commons/7/7e/Forchheim_coat_of_arms.png");
		add(model, "Hamburg", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Coat_of_arms_of_Hamburg.svg/200px-Coat_of_arms_of_Hamburg.svg.png");
		add(model, "Köln", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/DEU_Koeln_COA.svg/200px-DEU_Koeln_COA.svg.png");
		add(model, "Recklinghausen", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/57/DEU_Recklinghausen_COA.svg/200px-DEU_Recklinghausen_COA.svg.png");
		add(model, "Duisburg", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Stadtwappen_der_Stadt_Duisburg.svg/200px-Stadtwappen_der_Stadt_Duisburg.svg.png");
		add(model, "Straubing", "http://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/DEU_Straubing_COA.svg/200px-DEU_Straubing_COA.svg.png");
		add(model, "Ansbach", "http://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Wappen_von_Ansbach.svg/200px-Wappen_von_Ansbach.svg.png");
		add(model, "Stendal", "http://upload.wikimedia.org/wikipedia/commons/thumb/9/97/Wappen_Stendal.svg/200px-Wappen_Stendal.svg.png");
		add(model, "Schwerin", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Wappen_Schwerin.svg/200px-Wappen_Schwerin.svg.png");
		add(model, "Hildesheim", "http://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Wappen_Hildesheim.svg/200px-Wappen_Hildesheim.svg.png");
		add(model, "Rotenburg_(Wümme)", "http://upload.wikimedia.org/wikipedia/commons/b/ba/Wappen_Rotenburg_%28W%C3%BCmme%29.png");
		add(model, "Celle", "http://upload.wikimedia.org/wikipedia/commons/3/37/Wappen_Celle.png");
		add(model, "Plön", "http://upload.wikimedia.org/wikipedia/commons/b/b3/Wappen_von_Ploen.png");
		add(model, "Kreis_Steinburg", "http://upload.wikimedia.org/wikipedia/commons/a/a7/Wappen_Kreis_Steinburg.png");
		add(model, "Landkreis_Nordwestmecklenburg", "http://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/DEU_Nordwestmecklenburg_COA.svg/200px-DEU_Nordwestmecklenburg_COA.svg.png");
		add(model, "Göttingen", "http://upload.wikimedia.org/wikipedia/commons/0/0a/Stadtwappen_Goettingen.PNG");
		add(model, "Landkreis_Rosenheim", "http://upload.wikimedia.org/wikipedia/commons/e/e3/Wappen_LandkreisRosenheim.png");
		add(model, "Uelzen", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/50/DEU_Uelzen_COA.svg/200px-DEU_Uelzen_COA.svg.png");
		add(model, "Altötting", "http://upload.wikimedia.org/wikipedia/commons/d/d6/Wappen_Altoetting.png");
		add(model, "Holzminden", "http://upload.wikimedia.org/wikipedia/commons/5/54/Stadtwappen_der_Stadt_Holzminden.jpg");
		add(model, "Schwandorf", "http://upload.wikimedia.org/wikipedia/commons/4/45/Wappen_Schwandorf.jpg");
		add(model, "Bad_Kissingen", "http://upload.wikimedia.org/wikipedia/commons/3/32/Wappen_Bad_Kissingen.png");
		add(model, "Gifhorn", "http://upload.wikimedia.org/wikipedia/commons/2/2b/Wappen_Gifhorn.PNG");
		add(model, "Vechta", "http://upload.wikimedia.org/wikipedia/commons/f/ff/Vechta-Wappen.jpg");
		add(model, "Peine", "http://upload.wikimedia.org/wikipedia/commons/7/7d/Wappen_Peine.png");
		add(model, "Deggendorf", "http://upload.wikimedia.org/wikipedia/commons/0/0c/Wappen_Deggendorf.png");
		add(model, "Landkreis_Erlangen-Höchstadt", "http://upload.wikimedia.org/wikipedia/commons/8/84/Wappen_Landkreis_Erlangen-Hoechstadt.png");
		add(model, "Landkreis_Mayen-Koblenz", "http://upload.wikimedia.org/wikipedia/commons/8/80/Wappen_Landkreis_Mayen-Koblenz.png");
		add(model, "Amberg", "http://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/DEU_Amberg_COA.svg/200px-DEU_Amberg_COA.svg.png");
		add(model, "Miltenberg", "http://upload.wikimedia.org/wikipedia/commons/3/36/Wappen_miltenberg.png");
		add(model, "Kitzingen", "http://upload.wikimedia.org/wikipedia/commons/b/bb/Wappen_von_Kitzingen.png");
		add(model, "Schwabach", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/59/DEU_Schwabach_COA.svg/200px-DEU_Schwabach_COA.svg.png");
		add(model, "Wunsiedel", "http://upload.wikimedia.org/wikipedia/commons/1/1c/Wappen_Wunsiedel.png");
		add(model, "Tirschenreuth", "http://upload.wikimedia.org/wikipedia/commons/2/24/Wappen_Tirschenreuth.png");
		add(model, "Landkreis_Regensburg", "http://upload.wikimedia.org/wikipedia/commons/7/70/Wappen_Landkreis_Regensburg.png");
		add(model, "Neustadt_an_der_Waldnaab", "http://upload.wikimedia.org/wikipedia/commons/7/75/Wappen_Neustadt_a_d_Waldnaab.png");
		add(model, "Landkreis_Passau", "http://upload.wikimedia.org/wikipedia/commons/6/6a/Wappen_Landkreis_Passau.png");
		add(model, "Emmendingen", "http://upload.wikimedia.org/wikipedia/commons/5/5b/Wappen_Emmendingen.png");
		add(model, "Neckar-Odenwald-Kreis", "http://upload.wikimedia.org/wikipedia/commons/c/c4/Wappen_Neckar-Odenwald-Kreis.png");
		add(model, "Landkreis_Mainz-Bingen", "http://upload.wikimedia.org/wikipedia/commons/b/b5/Wappen_Landkreis_Mainz-Bingen.png");
		add(model, "Landkreis_Kaiserslautern", "http://upload.wikimedia.org/wikipedia/commons/3/33/Wappen_Landkreis_Kaiserslautern.png");
		add(model, "Landkreis_Birkenfeld", "http://upload.wikimedia.org/wikipedia/commons/4/45/Wappen_Landkreis_Birkenfeld.png");
		add(model, "Aurich", "http://upload.wikimedia.org/wikipedia/commons/f/fa/Wappen_Aurich.png");
		add(model, "Wolfenbüttel", "http://upload.wikimedia.org/wikipedia/commons/7/76/Wf_wappen.png");
		add(model, "Kreis_Herzogtum_Lauenburg", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Wappen_Herzogtum_Lauenburg_alt.svg/200px-Wappen_Herzogtum_Lauenburg_alt.svg.png");
		add(model, "Kreis_Segeberg", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Coa_Kreis_Segeberg.svg/200px-Coa_Kreis_Segeberg.svg.png");
		add(model, "Braunschweig", "http://upload.wikimedia.org/wikipedia/commons/3/3f/Brunswick_Coat_of_Arms.png");
		add(model, "Salzgitter", "http://upload.wikimedia.org/wikipedia/commons/thumb/c/c7/Coat_of_arms_of_Salzgitter.svg/200px-Coat_of_arms_of_Salzgitter.svg.png");
		add(model, "Osterode_am_Harz", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/85/CoA_Osterode-am-Harz_official-Shield.svg/200px-CoA_Osterode-am-Harz_official-Shield.svg.png");
		add(model, "Landkreis_Osterholz", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Osterholz.svg/200px-Osterholz.svg.png");
		add(model, "Stade", "http://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/DEU_Stade_COA.svg/200px-DEU_Stade_COA.svg.png");
		add(model, "Oldenburg_(Oldenburg)", "http://upload.wikimedia.org/wikipedia/commons/thumb/2/25/Oldenburg_coat_of_arms.svg/200px-Oldenburg_coat_of_arms.svg.png");
		add(model, "Wilhelmshaven", "http://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/DEU_Wilhelmshaven_COA.svg/200px-DEU_Wilhelmshaven_COA.svg.png");
		add(model, "Cloppenburg", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/DEU_Cloppenburg_COA.svg/200px-DEU_Cloppenburg_COA.svg.png");
		add(model, "Landkreis_Osnabrück", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/de/DEU_Landkreis_Osnabr%C3%BCck_COA.svg/200px-DEU_Landkreis_Osnabr%C3%BCck_COA.svg.png");
		add(model, "Wittmund", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d5/DEU_Wittmund_COA.svg/200px-DEU_Wittmund_COA.svg.png");
		add(model, "Essen", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/82/DEU_Essen_COA.svg/200px-DEU_Essen_COA.svg.png");
		add(model, "Krefeld", "http://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/DEU_Krefeld_COA.svg/200px-DEU_Krefeld_COA.svg.png");
		add(model, "Mönchengladbach", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/DEU_Moenchengladbach_COA.svg/200px-DEU_Moenchengladbach_COA.svg.png");
		add(model, "Oberhausen", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/42/DEU_Oberhausen_COA.svg/200px-DEU_Oberhausen_COA.svg.png");
		add(model, "Remscheid", "http://upload.wikimedia.org/wikipedia/commons/thumb/3/30/DEU_Remscheid_COA.svg/200px-DEU_Remscheid_COA.svg.png");
		add(model, "Wuppertal", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/df/DEU_Wuppertal_COA.svg/200px-DEU_Wuppertal_COA.svg.png");
		add(model, "Kleve", "http://upload.wikimedia.org/wikipedia/commons/thumb/3/39/DEU_Kleve_COA.svg/200px-DEU_Kleve_COA.svg.png");
		add(model, "Mettmann", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/DEU_Mettmann_COA.svg/200px-DEU_Mettmann_COA.svg.png");
		add(model, "Wesel", "http://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/DEU_Wesel_COA.svg/200px-DEU_Wesel_COA.svg.png");
		add(model, "Leverkusen", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/86/DEU_Leverkusen_COA.svg/200px-DEU_Leverkusen_COA.svg.png");
		add(model, "Städteregion_Aachen", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/DEU_Staedteregion_Aachen_COA.svg/200px-DEU_Staedteregion_Aachen_COA.svg.png");
		add(model, "Heinsberg", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/55/DEU_Heinsberg_COA.svg/200px-DEU_Heinsberg_COA.svg.png");
		add(model, "Oberbergischer_Kreis", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/DEU_Oberbergischer_Kreis_COA.svg/200px-DEU_Oberbergischer_Kreis_COA.svg.png");
		add(model, "Rheinisch-Bergischer_Kreis", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/DEU_Rheinisch-Bergischer_Kreis_COA.svg/200px-DEU_Rheinisch-Bergischer_Kreis_COA.svg.png");
		add(model, "Rhein-Sieg-Kreis", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/84/DEU_Rhein-Sieg-Kreis_COA.svg/200px-DEU_Rhein-Sieg-Kreis_COA.svg.png");
		add(model, "Bottrop", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/DEU_Bottrop_COA.svg/200px-DEU_Bottrop_COA.svg.png");
		add(model, "Gelsenkirchen", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/8b/DEU_Gelsenkirchen_COA.svg/200px-DEU_Gelsenkirchen_COA.svg.png");
		add(model, "Borken", "http://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/DEU_Borken_%28Westf.%29_COA.svg/200px-DEU_Borken_%28Westf.%29_COA.svg.png");
		add(model, "Coesfeld", "http://upload.wikimedia.org/wikipedia/commons/thumb/6/69/DEU_Coesfeld_COA.svg/200px-DEU_Coesfeld_COA.svg.png");
		add(model, "Steinfurt", "http://upload.wikimedia.org/wikipedia/commons/thumb/6/63/DE_Steinfurt_COA.svg/200px-DE_Steinfurt_COA.svg.png");
		add(model, "Warendorf", "http://upload.wikimedia.org/wikipedia/commons/thumb/b/bf/DE_Warendorf_COA.svg/200px-DE_Warendorf_COA.svg.png");
		add(model, "Gütersloh", "http://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Wappen_der_Stadt_G%C3%BCtersloh.svg/200px-Wappen_der_Stadt_G%C3%BCtersloh.svg.png");
		add(model, "Kreis_Lippe", "http://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Kreiswappen_des_Kreises_Lippe.svg/200px-Kreiswappen_des_Kreises_Lippe.svg.png");
		add(model, "Paderborn", "http://upload.wikimedia.org/wikipedia/commons/thumb/1/19/DEU_Paderborn_COA.svg/200px-DEU_Paderborn_COA.svg.png");
		add(model, "Dortmund", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/Coat_of_arms_of_Dortmund.svg/200px-Coat_of_arms_of_Dortmund.svg.png");
		add(model, "Hamm", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/DEU_Hamm_COA.svg/200px-DEU_Hamm_COA.svg.png");
		add(model, "Herne", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Herne_Coat_of_Arms.svg/200px-Herne_Coat_of_Arms.svg.png");
		add(model, "Soest", "http://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Soest-coa.svg/200px-Soest-coa.svg.png");
		add(model, "Kreis_Bergstraße", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/84/WappenKreisBergstrasse.svg/200px-WappenKreisBergstrasse.svg.png");
		add(model, "Groß-Gerau", "http://upload.wikimedia.org/wikipedia/commons/c/cd/GG_Gross-Gerau.jpg");
		add(model, "Kassel", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/de/Coat_of_arms_of_Kassel.svg/200px-Coat_of_arms_of_Kassel.svg.png");
		add(model, "Trier", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/db/Trier_Rheinland-Palatinate_Germany_CoA.svg/200px-Trier_Rheinland-Palatinate_Germany_CoA.svg.png");
		add(model, "Landkreis_Vulkaneifel", "http://upload.wikimedia.org/wikipedia/commons/thumb/7/76/CoA_Landkreis_Vulkaneifel.svg/200px-CoA_Landkreis_Vulkaneifel.svg.png");
		add(model, "Karlsruhe", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/5b/Coat_of_arms_de-bw_Karlsruhe.svg/200px-Coat_of_arms_de-bw_Karlsruhe.svg.png");
		add(model, "Landkreis_Karlsruhe", "http://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Coat_of_arms_of_Karlsruhe_%28district%29.svg/200px-Coat_of_arms_of_Karlsruhe_%28district%29.svg.png");
		add(model, "Ludwigshafen_am_Rhein", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/DEU_Ludwigshafen_COA.svg/200px-DEU_Ludwigshafen_COA.svg.png");
		add(model, "Mainz", "http://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Coat_of_arms_of_Mainz-2008_new.svg/200px-Coat_of_arms_of_Mainz-2008_new.svg.png");
		add(model, "Speyer", "http://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/DEU_Speyer_COA.svg/200px-DEU_Speyer_COA.svg.png");
		add(model, "Worms", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/DEU_Worms_COA.svg/200px-DEU_Worms_COA.svg.png");
		add(model, "Germersheim", "http://upload.wikimedia.org/wikipedia/commons/8/8c/Coat_of_Arms_of_Germersheim.gif");
		add(model, "Stuttgart", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/Coat_of_arms_of_Stuttgart.svg/200px-Coat_of_arms_of_Stuttgart.svg.png");
		add(model, "Böblingen", "http://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Coa_Germany_Town_B%C3%B6blingen.svg/200px-Coa_Germany_Town_B%C3%B6blingen.svg.png");
		add(model, "Berlin", "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Coat_of_arms_of_Berlin.svg/200px-Coat_of_arms_of_Berlin.svg.png");
		add(model, "Kreis_Siegen-Wittgenstein", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/DEU_Kreis_Siegen-Wittgenstein_COA.svg/200px-DEU_Kreis_Siegen-Wittgenstein_COA.svg.png");
		add(model, "Rhein-Neckar-Kreis", "http://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/COA_Rhein-Neckar-Kreis.svg/200px-COA_Rhein-Neckar-Kreis.svg.png");
		add(model, "Freudenstadt", "http://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Freudenstadt-CoA-Vector.svg/200px-Freudenstadt-CoA-Vector.svg.png");
		add(model, "Ulm", "http://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Coat_of_arms_of_Ulm.svg/200px-Coat_of_arms_of_Ulm.svg.png");
		add(model, "Landkreis_München", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Coat_of_Arms_of_Munich_%28district%29.svg/200px-Coat_of_Arms_of_Munich_%28district%29.svg.png");
		add(model, "Landkreis_Regen", "http://upload.wikimedia.org/wikipedia/commons/2/25/Wappen_Landkreis_Regen.png");
		add(model, "Weiden_in_der_Oberpfalz", "http://upload.wikimedia.org/wikipedia/commons/thumb/7/71/Stadt_Weiden_in_der_Opf.svg/200px-Stadt_Weiden_in_der_Opf.svg.png");
		add(model, "Landkreis_Cham", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/DEU_Landkreis_Cham_COA.svg/200px-DEU_Landkreis_Cham_COA.svg.png");
		add(model, "Landkreis_Haßberge", "http://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Wappen_Landkreis_Ha%C3%9Fberge.svg/200px-Wappen_Landkreis_Ha%C3%9Fberge.svg.png");
		add(model, "Günzburg", "http://upload.wikimedia.org/wikipedia/commons/2/20/Coa_guenzburg.png");
		add(model, "Potsdam", "http://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Coat_of_arms_of_Potsdam.svg/200px-Coat_of_arms_of_Potsdam.svg.png");
		add(model, "Landkreis_Ludwigslust-Parchim", "http://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Coats_of_arms_of_None.svg/200px-Coats_of_arms_of_None.svg.png");
		add(model, "Chemnitz", "http://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Coat_of_arms_of_Chemnitz.svg/200px-Coat_of_arms_of_Chemnitz.svg.png");
		add(model, "Bautzen", "http://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Coat_of_arm_Bautzen.svg/200px-Coat_of_arm_Bautzen.svg.png");
		add(model, "Leipzig", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Coat_of_arms_of_Leipzig.svg/200px-Coat_of_arms_of_Leipzig.svg.png");
		add(model, "Halle_(Saale)", "http://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Coat_of_arms_of_Halle_%28Saale%29.svg/200px-Coat_of_arms_of_Halle_%28Saale%29.svg.png");
		add(model, "Hildburghausen", "http://upload.wikimedia.org/wikipedia/commons/3/3a/Hildburghausen_coa.png");
		add(model, "Greiz", "http://upload.wikimedia.org/wikipedia/commons/3/3a/Greiz_coa.png");
		add(model, "Freie_Hansestadt_Bremen", "http://upload.wikimedia.org/wikipedia/commons/thumb/6/64/Bremen_Wappen%28Mittel%29.svg/200px-Bremen_Wappen%28Mittel%29.svg.png");
		add(model, "Rheinland-Pfalz", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Coat_of_arms_of_Rhineland-Palatinate.svg/200px-Coat_of_arms_of_Rhineland-Palatinate.svg.png");
		add(model, "Niedersachsen", "http://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Coat_of_arms_of_Lower_Saxony.svg/200px-Coat_of_arms_of_Lower_Saxony.svg.png");
		add(model, "Baden-Württemberg", "http://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Coat_of_arms_of_Baden-W%C3%BCrttemberg_%28lesser%29.svg/200px-Coat_of_arms_of_Baden-W%C3%BCrttemberg_%28lesser%29.svg.png");
		add(model, "Bayern", "http://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Bayern-1950.png/200px-Bayern-1950.png");
		add(model, "Sachsen-Anhalt", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Wappen_Sachsen-Anhalt.svg/200px-Wappen_Sachsen-Anhalt.svg.png");
		add(model, "Hessen", "http://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Coat_of_arms_of_Hesse.svg/200px-Coat_of_arms_of_Hesse.svg.png");
		add(model, "Thüringen", "http://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Coat_of_arms_of_Thuringia.svg/200px-Coat_of_arms_of_Thuringia.svg.png");
		add(model, "Brandenburg", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/45/Brandenburg_Wappen.svg/200px-Brandenburg_Wappen.svg.png");
		add(model, "Schleswig-Holstein", "http://upload.wikimedia.org/wikipedia/commons/thumb/6/60/Coat_of_arms_of_Schleswig-Holstein.svg/200px-Coat_of_arms_of_Schleswig-Holstein.svg.png");
		add(model, "Saarland", "http://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Wappen_des_Saarlands.svg/200px-Wappen_des_Saarlands.svg.png");
		add(model, "Nordrhein-Westfalen", "http://upload.wikimedia.org/wikipedia/commons/thumb/b/bb/Coat_of_arms_of_North_Rhine-Westfalia.svg/200px-Coat_of_arms_of_North_Rhine-Westfalia.svg.png");
		add(model, "Mecklenburg-Vorpommern", "http://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Coat_of_arms_of_Mecklenburg-Western_Pomerania_%28great%29.svg/200px-Coat_of_arms_of_Mecklenburg-Western_Pomerania_%28great%29.svg.png");
		add(model, "Sachsen", "http://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Coat_of_arms_of_Saxony.svg/200px-Coat_of_arms_of_Saxony.svg.png");
		add(model, "Landkreis_Mecklenburgische_Seenplatte", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Wappen_des_Landkreises_Mecklenburgische_Seenplatte.png/198px-Wappen_des_Landkreises_Mecklenburgische_Seenplatte.png");
	}

	private static void add(Model model, String uriLocalName, String picture) {
		
		model.removeAll(ResourceFactory.createResource("http://de.dbpedia.org/resource/" + uriLocalName), ResourceFactory.createProperty("http://dbpedia.org/ontology/thumbnail"), null);
		model.add(ResourceFactory.createResource("http://de.dbpedia.org/resource/" + uriLocalName), ResourceFactory.createProperty("http://dbpedia.org/ontology/thumbnail"), ResourceFactory.createResource(picture));
	}
}
