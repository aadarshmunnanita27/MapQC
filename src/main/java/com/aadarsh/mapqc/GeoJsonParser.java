package com.aadarsh.mapqc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Reads a GeoJSON FeatureCollection and returns the LineString roads inside it. */
@Component
public class GeoJsonParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<RoadFeature> parse(String geojson) {
        JsonNode root;
        try {
            root = mapper.readTree(geojson);
        } catch (Exception e) {
            throw new IllegalArgumentException("Body is not valid JSON");
        }
        if (!"FeatureCollection".equals(root.path("type").asText())) {
            throw new IllegalArgumentException("GeoJSON must be a FeatureCollection");
        }
        List<RoadFeature> roads = new ArrayList<>();
        for (JsonNode feature : root.path("features")) {
            JsonNode geometry = feature.path("geometry");
            if (!"LineString".equals(geometry.path("type").asText())) {
                throw new IllegalArgumentException("Only LineString geometries are supported");
            }
            String name = feature.path("properties").path("name").asText("unnamed");
            roads.add(new RoadFeature(name, geometry.toString()));
        }
        return roads;
    }
}
