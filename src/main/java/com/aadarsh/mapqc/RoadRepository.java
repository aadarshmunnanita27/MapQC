package com.aadarsh.mapqc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/** All database access for roads and issues (plain SQL with JdbcTemplate). */
@Repository
public class RoadRepository {

    private final JdbcTemplate jdbc;

    public RoadRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int saveAll(List<RoadFeature> roads) {
        for (RoadFeature r : roads) {
            jdbc.update("INSERT INTO roads(name, geom) VALUES (?, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326))",
                    r.name(), r.geometryJson());
        }
        return roads.size();
    }

    public void deleteAll() {
        jdbc.update("DELETE FROM issues");
        jdbc.update("DELETE FROM roads");
    }

    public String roadsAsGeoJson() {
        return jdbc.queryForObject("""
            SELECT json_build_object('type','FeatureCollection','features',
              COALESCE(json_agg(json_build_object('type','Feature',
                'geometry', ST_AsGeoJSON(geom)::json,
                'properties', json_build_object('id', id, 'name', name))), '[]'::json))::text
            FROM roads
            """, String.class);
    }

    public String issuesAsGeoJson() {
        return jdbc.queryForObject("""
            SELECT json_build_object('type','FeatureCollection','features',
              COALESCE(json_agg(json_build_object('type','Feature',
                'geometry', ST_AsGeoJSON(geom)::json,
                'properties', json_build_object('road_id', road_id, 'type', issue_type,
                                                'description', description))), '[]'::json))::text
            FROM issues
            """, String.class);
    }

    public List<Map<String, Object>> issueSummary() {
        return jdbc.queryForList("SELECT issue_type, COUNT(*) AS total FROM issues GROUP BY issue_type ORDER BY issue_type");
    }
}
