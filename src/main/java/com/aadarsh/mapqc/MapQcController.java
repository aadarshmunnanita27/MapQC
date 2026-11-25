package com.aadarsh.mapqc;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MapQcController {

    private final RoadRepository roads;
    private final GeoJsonParser parser;
    private final ValidationService validation;

    public MapQcController(RoadRepository roads, GeoJsonParser parser, ValidationService validation) {
        this.roads = roads;
        this.parser = parser;
        this.validation = validation;
    }

    @PostMapping("/roads/upload")
    public Map<String, Object> upload(@RequestBody String geojson) {
        return Map.of("inserted", roads.saveAll(parser.parse(geojson)));
    }

    @PostMapping("/roads/load-sample")
    public Map<String, Object> loadSample() throws IOException {
        String json = new ClassPathResource("sample-roads.geojson")
                .getContentAsString(StandardCharsets.UTF_8);
        return Map.of("inserted", roads.saveAll(parser.parse(json)));
    }

    @DeleteMapping("/roads")
    public Map<String, Object> clear() {
        roads.deleteAll();
        return Map.of("cleared", true);
    }

    @GetMapping(value = "/roads", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getRoads() {
        return roads.roadsAsGeoJson();
    }

    @PostMapping("/validate")
    public Map<String, Integer> validate() {
        return validation.runAll();
    }

    @GetMapping(value = "/issues", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getIssues() {
        return roads.issuesAsGeoJson();
    }

    @GetMapping("/summary")
    public List<Map<String, Object>> summary() {
        return roads.issueSummary();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}
