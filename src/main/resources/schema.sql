CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS roads (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100),
    geom geometry(LineString, 4326) NOT NULL
);

CREATE TABLE IF NOT EXISTS issues (
    id          SERIAL PRIMARY KEY,
    road_id     INT REFERENCES roads(id) ON DELETE CASCADE,
    issue_type  VARCHAR(50),
    description TEXT,
    geom        geometry(Geometry, 4326)
);

CREATE INDEX IF NOT EXISTS idx_roads_geom ON roads USING GIST (geom);
