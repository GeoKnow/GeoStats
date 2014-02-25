package org.aksw.geostats;

public class Constants {

	public static final String GEOSTATS_DATA_CUBE_NS = "http://geostats.aksw.org/qb/";

	public static final String NS_GEO = "http://www.opengis.net/ont/geosparql#";
	public static final String NS_SF = "http://www.opengis.net/ont/sf#";
	public static final String NS_GML = "http://loki.cae.drexel.edu/~wbs/ontology/2004/09/ogc-gml#";
	public static final String NS_DC = "http://purl.org/dc/terms/";
	public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema#";
	//Alias for most common namespaces
	  public static final String NSGEO = "http://www.opengis.net/ont/geosparql#";
	  public static final String NSSF =  "http://www.opengis.net/ont/sf#";
	  public static final String NSGML = "http://loki.cae.drexel.edu/~wbs/ontology/2004/09/ogc-gml#";
	  public static final String NSXSD = "http://www.w3.org/2001/XMLSchema#";
	  public static final String NSPOS = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	  public static final String NSVIRT = "http://www.openlinksw.com/schemas/virtrdf#";

	  //alias for most common tags
	  public static final String GEOMETRY = "Geometry";
	  public static final String FEATURE = "Feature";
	  public static final String LINE_STRING = "LineString";
	  public static final String MULTI_LINE_STRING = "MultiLineString";
	  public static final String POLYGON = "Polygon";
	  public static final String MULTI_POLYGON = "MultiPolygon";
	  public static final String POINT = "Point";
	  public static final String LATITUDE = "lat";
	  public static final String LONGITUDE = "long";
	  public static final String GML = "gml";
	  public static final String WKT = "asWKT";
	  public static final String WKTLiteral = "wktLiteral";
	  public static final String NAME = "name";
	  public static final String TYPE = "type";
}
