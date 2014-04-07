package org.aksw.geostats.lgd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.aksw.geostats.lgd.LinkedGeoDataStatistik.Job;
import org.postgis.MultiPolygon;
import org.postgis.Polygon;

public class LinkedGeoDataCallable implements Callable<Job> {

	private Job job;

	public LinkedGeoDataCallable(Job job) {
		
		this.job = job;
	}

	@Override
	public Job call() throws Exception {
		
		for ( String amenity : this.job.amenity ) {
			
			Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/osm", "postgres", "");
			Statement stmt = connection.createStatement();
			String multipolygon = new MultiPolygon(this.job.entity.polygons.toArray(new Polygon[0])).toString();
			String sql = "SELECT COUNT(*) as count FROM nodes node WHERE ((tags->'amenity') = '"+amenity+"') AND ST_Intersects(node.geom, ST_GeomFromText('"+ multipolygon  + "', 4326));";
			ResultSet result = stmt.executeQuery(sql);
			while ( result.next() ) {
				
//				System.out.println(amenity+ ": " +result.getInt("count"));
				this.job.statistics.put(amenity, result.getInt("count"));
			}
			connection.close();
		}
		
		System.out.println("Entity " + (LinkedGeoDataStatistik.ENTITIES++) + "/"+LinkedGeoDataStatistik.TOTAL_ENTITIES +" finished ("+job.entity.uri +")");
		
		return this.job;
	}
}
