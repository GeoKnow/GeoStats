/**
 * 
 */
package org.aksw;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.sparqlify.config.syntax.NamedViewTemplateDefinition;
import org.aksw.sparqlify.config.syntax.TemplateConfig;
import org.aksw.sparqlify.config.syntax.ViewTemplateDefinition;
import org.aksw.sparqlify.core.test.TestBundle;
import org.aksw.sparqlify.csv.CsvMapperCliMain;
import org.aksw.sparqlify.csv.CsvParserConfig;
import org.aksw.sparqlify.csv.InputSupplierCSVReader;
import org.aksw.sparqlify.csv.TripleIteratorTracking;
import org.aksw.sparqlify.validation.LoggerCount;
import org.aksw.sparqlify.web.SparqlFormatterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.antlr.runtime.RecognitionException;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.io.InputSupplier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.geoknow.athenarc.triplegeo.RdfExport;

/**
 * @author Daniel Gerber <daniel.gerber@icloud.com>
 * 
 */
public class SparqlifyRegionalStatistik {
	
	private static final Logger logger = LoggerFactory.getLogger(SparqlifyRegionalStatistik.class);

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	public static void main(String[] args) throws IOException, RecognitionException {

		CsvParserConfig csvConfig = new CsvParserConfig();
		boolean firstRowAsColumnHeaders = true;
		csvConfig.setFieldDelimiter(null);
		csvConfig.setFieldSeparator(";".charAt(0));
		csvConfig.setEscapeCharacter("\\".charAt(0));

		Resource csv = new FileSystemResource("data/sparqlify/insolvenzen/insolvenzen-kreisebene-325-31-4.csv");
		InputSupplier<Reader> readerSupplier = new InputSupplierResourceReader(csv);
		InputSupplier<CSVReader> csvReaderSupplier = new InputSupplierCSVReader(readerSupplier, csvConfig);
		ResultSet rs = CsvMapperCliMain.createResultSetFromCsv(csvReaderSupplier, firstRowAsColumnHeaders, 100);

		Resource mapping = new FileSystemResource("data/sparqlify/insolvenzen/sparqlify-mapping.txt");
		InputStream mappingIn = mapping.getInputStream();
		LoggerCount loggerCount = new LoggerCount(logger);
		TemplateConfig tc = CsvMapperCliMain.readTemplateConfig(mappingIn, loggerCount);

		Map<String, NamedViewTemplateDefinition> viewIndex = CsvMapperCliMain.indexViews(tc.getDefinitions(), loggerCount);
		ViewTemplateDefinition view = CsvMapperCliMain.pickView(viewIndex, null);

		TripleIteratorTracking it = CsvMapperCliMain.createTripleIterator(rs, view);
		Model model = ModelFactory.createDefaultModel();
		while (it.hasNext() ) { model.add(model.asStatement(it.next())); }
		
		RdfExport.write(model, "data/sparqlify/insolvenzen/insolvenzen-kreisebene-325-31-4.ttl");
//		SparqlFormatterUtils.writeText(System.out, it);
	}

	static class InputSupplierResourceStream implements InputSupplier<InputStream> {
		private Resource resource;

		public InputSupplierResourceStream(Resource resource) {
			this.resource = resource;
		}

		@Override
		public InputStream getInput() throws IOException {
			return resource.getInputStream();
		}
	}

	static class InputSupplierResourceReader implements InputSupplier<Reader> {
		private Resource resource;

		public InputSupplierResourceReader(Resource resource) {
			this.resource = resource;
		}

		@Override
		public Reader getInput() throws IOException {
			InputStream in = resource.getInputStream();
			Reader result = new InputStreamReader(in);
			return result;
		}
	}
}
