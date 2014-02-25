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

import org.aksw.geostats.json.JsonDataGenerator;
import org.aksw.geostats.linking.NutsDBpediaLinker;
import org.aksw.geostats.linking.RegionalStatistikLinker;
import org.aksw.geostats.rdf.RdfExport;
import org.aksw.geostats.shape.ExtraWurstExtractor;
import org.aksw.geostats.shape.NutsLoader;
import org.json.JSONException;

import com.hp.hpl.jena.rdf.model.Model;
import com.vividsolutions.jts.io.ParseException;

/**
 * Entry point to convert shapefiles into RDF triples.
 * 
 * @author Kostas Patroumpas Last modified by: Kostas Patroumpas, 12/6/2013
 */
public class ShpToRdf {
	
	public static final String GEOSTATS_NS = "http://geostats.aksw.org/";

	public static void main(String[] args) throws IOException, InterruptedException, JSONException, ParseException, ClassNotFoundException, SQLException {
		
		Model model = RdfExport.getModel();
		
		System.out.println("Generating Backend Data!");
		ExtraWurstExtractor.extract(model);
		
		NutsLoader.extract(model);
		NutsDBpediaLinker nutsDBpediaLinker = new NutsDBpediaLinker(model);
		nutsDBpediaLinker.linkDistricts();
		nutsDBpediaLinker.linkAdministrativeDistricts();
		nutsDBpediaLinker.linkFederalStates();
		
		RegionalStatistikLinker.generateLinks(model);
		RdfExport.write(model, "data/geostats.ttl");
		
		System.out.println("Generating View Data!");
		JsonDataGenerator.generate();
	}
}
