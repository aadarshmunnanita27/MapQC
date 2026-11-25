package com.aadarsh.mapqc;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoJsonParserTest {

    private final GeoJsonParser parser = new GeoJsonParser();

    @Test
    void parsesValidFeatureCollection() {
        String json = """
            {"type":"FeatureCollection","features":[
              {"type":"Feature","properties":{"name":"A"},
               "geometry":{"type":"LineString","coordinates":[[0,0],[1,1]]}}]}
            """;
        List<RoadFeature> roads = parser.parse(json);
        assertEquals(1, roads.size());
        assertEquals("A", roads.get(0).name());
    }

    @Test
    void usesDefaultNameWhenMissing() {
        String json = """
            {"type":"FeatureCollection","features":[
              {"type":"Feature","properties":{},
               "geometry":{"type":"LineString","coordinates":[[0,0],[1,1]]}}]}
            """;
        assertEquals("unnamed", parser.parse(json).get(0).name());
    }

    @Test
    void rejectsNonFeatureCollection() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("{\"type\":\"Point\"}"));
    }

    @Test
    void rejectsNonLineString() {
        String json = """
            {"type":"FeatureCollection","features":[
              {"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[0,0]}}]}
            """;
        assertThrows(IllegalArgumentException.class, () -> parser.parse(json));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("not json"));
    }
}
