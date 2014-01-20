package eu.geoknow.athenarc.triplegeo;

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
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

import eu.geoknow.athenarc.triplegeo.shape.ShpFileLoader;


/**
 * Entry point to convert shapefiles into RDF triples.
 * 
 * @author Kostas Patroumpas Last modified by: Kostas Patroumpas, 12/6/2013
 */
public class ShpToRdf {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		Model model = RdfExport.getModelFromConfiguration("http://geostats.aksw.org/");
		
		List<ShpFileLoader> fileLoader = new ArrayList<>();
		fileLoader.add(new DistrictShpFileLoader(model));
		fileLoader.add(new AdministrativeDistrictShpFileLoader(model));
		fileLoader.add(new FederalStateShpFileLoader(model));
		
		for ( ShpFileLoader loader : fileLoader) {
			
			loader.generateRDF();
		}
		
		RdfExport.write(model, "/Users/gerb/Development/workspaces/data/geostats/rdf/geostats.ttl");
	}
}
