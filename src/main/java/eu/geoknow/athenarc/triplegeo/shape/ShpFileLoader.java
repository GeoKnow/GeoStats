/*
 * @(#) ShpConnector.java 	 version 1.0   12/6/2013
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
package eu.geoknow.athenarc.triplegeo.shape;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
//import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;


//import com.vividsolutions.jts.geom.Polygon;
import eu.geoknow.athenarc.triplegeo.Constants;
import eu.geoknow.athenarc.triplegeo.ShpToRdf;
import eu.geoknow.athenarc.triplegeo.utils.Configuration;
import eu.geoknow.athenarc.triplegeo.utils.UtilsConstants;
import eu.geoknow.athenarc.triplegeo.utils.UtilsLib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aksw.disambiguation.UriResolver;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
//import org.opengis.geometry.MismatchedDimensionException;
//import org.opengis.referencing.FactoryException;
//import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
//import org.opengis.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;

/**
 * Class to convert shapefiles to RDF.
 * 
 * @author jonbaraq initially implemented for geometry2rdf utility (source:
 *         https://github.com/boricles/geometry2rdf/tree/master/Geometry2RDF)
 *         Modified by: Kostas Patroumpas, 8/2/2013 Modified: 6/3/2013, added
 *         support for transformation from a given CRS to WGS84 Modified:
 *         15/3/2013, added support for exporting custom geometries to (1)
 *         Virtuoso RDF and (2) according to WGS84 Geopositioning RDF vocabulary
 *         Last modified by: Kostas Patroumpas, 12/6/2013
 */

public interface ShpFileLoader {

	/**
	 * Loads the shape file from the configuration path and returns the feature
	 * collection associated according to the configuration.
	 * 
	 * @param shapePath
	 *            with the path to the shapefile.
	 * @param featureString
	 *            with the featureString to filter.
	 * 
	 * @return FeatureCollection with the collection of features filtered.
	 */
	public FeatureCollection getShapeFileFeatureCollection();

	/**
	 * 
	 * Writes the RDF model into a file
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void generateRDF() throws IOException, InterruptedException;
}
