package com.aadarsh.mapqc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs the data-quality (topology) checks. Each check is one INSERT ... SELECT
 * that finds bad roads using PostGIS functions and stores them in the issues table.
 */
@Service
public class ValidationService {

    private static final String INVALID = """
        INSERT INTO issues(road_id, issue_type, description, geom)
        SELECT id, 'INVALID_GEOMETRY', 'Invalid geometry: ' || ST_IsValidReason(geom), ST_Centroid(geom)
        FROM roads WHERE NOT ST_IsValid(geom)
        """;

    private static final String DUPLICATE_ROAD = """
        INSERT INTO issues(road_id, issue_type, description, geom)
        SELECT a.id, 'DUPLICATE_ROAD', 'Same geometry as road ' || b.id, ST_Centroid(a.geom)
        FROM roads a JOIN roads b ON a.id > b.id
        WHERE ST_IsValid(a.geom) AND ST_IsValid(b.geom) AND ST_Equals(a.geom, b.geom)
        """;

    private static final String DUPLICATE_NODE = """
        INSERT INTO issues(road_id, issue_type, description, geom)
        SELECT id, 'DUPLICATE_NODE', 'Road has repeated consecutive points', ST_Centroid(geom)
        FROM roads
        WHERE ST_IsValid(geom) AND ST_NPoints(geom) > ST_NPoints(ST_RemoveRepeatedPoints(geom))
        """;

    private static final String DISCONNECTED = """
        INSERT INTO issues(road_id, issue_type, description, geom)
        SELECT r.id, 'DISCONNECTED_ROAD', 'Road does not touch any other road', ST_Centroid(r.geom)
        FROM roads r
        WHERE ST_IsValid(r.geom)
          AND NOT EXISTS (SELECT 1 FROM roads o
                          WHERE o.id <> r.id AND ST_IsValid(o.geom) AND ST_Intersects(r.geom, o.geom))
        """;

    private final JdbcTemplate jdbc;

    public ValidationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public Map<String, Integer> runAll() {
        jdbc.update("DELETE FROM issues");   // start fresh on every run
        Map<String, Integer> found = new LinkedHashMap<>();
        found.put("INVALID_GEOMETRY", jdbc.update(INVALID));
        found.put("DUPLICATE_ROAD", jdbc.update(DUPLICATE_ROAD));
        found.put("DUPLICATE_NODE", jdbc.update(DUPLICATE_NODE));
        found.put("DISCONNECTED_ROAD", jdbc.update(DISCONNECTED));
        return found;
    }
}
