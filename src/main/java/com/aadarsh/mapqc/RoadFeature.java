package com.aadarsh.mapqc;

/** One road read from a GeoJSON file: a name plus its geometry as a JSON string. */
public record RoadFeature(String name, String geometryJson) { }
