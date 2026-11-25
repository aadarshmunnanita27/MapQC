# MapQC - Map Data Validation and Geospatial Analytics Service

Spring Boot + PostgreSQL/PostGIS service that loads road data (GeoJSON), finds data-quality problems
and shows them on a map.

## Run (needs Docker)
    docker compose up --build        # starts PostGIS + the app
    open http://localhost:8080

Click: 1. Clear data -> 2. Load sample roads -> 3. Run validation. Red circles = problems.

## Run locally without Docker for the app
    docker compose up db             # only the database (port 5433)
    mvn spring-boot:run
    mvn test                         # unit tests

## REST APIs
| Method | URL | What it does |
|---|---|---|
| POST | /api/roads/upload | upload a GeoJSON FeatureCollection of LineStrings |
| POST | /api/roads/load-sample | load the built-in sample |
| DELETE | /api/roads | delete all data |
| GET | /api/roads | roads as GeoJSON |
| POST | /api/validate | run all checks, returns count per issue type |
| GET | /api/issues | issues as GeoJSON |
| GET | /api/summary | issue counts by type |

## The 4 checks (all in ValidationService.java)
1. INVALID_GEOMETRY - `ST_IsValid` is false (e.g. a line with two identical points)
2. DUPLICATE_ROAD - two roads with the same geometry (`ST_Equals`)
3. DUPLICATE_NODE - repeated consecutive points (`ST_NPoints` vs `ST_RemoveRepeatedPoints`)
4. DISCONNECTED_ROAD - road that touches no other road (`ST_Intersects` inside `NOT EXISTS`)

## Structure
    MapQcController   -> REST endpoints
    GeoJsonParser     -> reads/validates uploaded GeoJSON (unit tested)
    RoadRepository    -> SQL via JdbcTemplate
    ValidationService -> the 4 PostGIS checks
    static/index.html -> React + Leaflet dashboard
