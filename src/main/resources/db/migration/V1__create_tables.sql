CREATE TABLE heat_zones (
    id           BIGINT IDENTITY(1,1) NOT NULL,
    name         NVARCHAR(120) NOT NULL,
    neighborhood NVARCHAR(120) NOT NULL,
    city         NVARCHAR(120) NOT NULL,
    latitude     DECIMAL(9,6)  NOT NULL,
    longitude    DECIMAL(9,6)  NOT NULL,
    risk_level   VARCHAR(20)   NOT NULL,
    active       BIT           NOT NULL DEFAULT 1,
    created_at   DATETIME2(6)  NOT NULL,
    updated_at   DATETIME2(6)  NOT NULL,
    CONSTRAINT pk_heat_zones PRIMARY KEY (id),
    CONSTRAINT uk_heat_zones_name_city UNIQUE (name, city),
    CONSTRAINT ck_heat_zones_risk_level CHECK (risk_level IN ('LOW', 'MODERATE', 'HIGH', 'EXTREME')),
    CONSTRAINT ck_heat_zones_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_heat_zones_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE sensors (
    id                    BIGINT IDENTITY(1,1) NOT NULL,
    code                  VARCHAR(40)   NOT NULL,
    name                  NVARCHAR(120) NOT NULL,
    latitude              DECIMAL(9,6)  NOT NULL,
    longitude             DECIMAL(9,6)  NOT NULL,
    status                VARCHAR(20)   NOT NULL,
    installation_date     DATE          NOT NULL,
    last_communication_at DATETIME2(6)  NULL,
    heat_zone_id          BIGINT        NOT NULL,
    created_at            DATETIME2(6)  NOT NULL,
    updated_at            DATETIME2(6)  NOT NULL,
    CONSTRAINT pk_sensors PRIMARY KEY (id),
    CONSTRAINT uk_sensors_code UNIQUE (code),
    CONSTRAINT fk_sensors_heat_zone FOREIGN KEY (heat_zone_id) REFERENCES heat_zones (id),
    CONSTRAINT ck_sensors_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
    CONSTRAINT ck_sensors_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_sensors_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE INDEX ix_sensors_heat_zone_id ON sensors (heat_zone_id);

CREATE TABLE temperature_readings (
    id                  BIGINT IDENTITY(1,1) NOT NULL,
    sensor_id           BIGINT       NOT NULL,
    measured_at         DATETIME2(6) NOT NULL,
    temperature_celsius DECIMAL(5,2) NOT NULL,
    humidity_percent    DECIMAL(5,2) NOT NULL,
    source              VARCHAR(20)  NOT NULL,
    quality             VARCHAR(20)  NOT NULL,
    created_at          DATETIME2(6) NOT NULL,
    updated_at          DATETIME2(6) NOT NULL,
    CONSTRAINT pk_temperature_readings PRIMARY KEY (id),
    CONSTRAINT uk_temperature_readings_sensor_measured UNIQUE (sensor_id, measured_at),
    CONSTRAINT fk_temperature_readings_sensor FOREIGN KEY (sensor_id) REFERENCES sensors (id),
    CONSTRAINT ck_temperature_readings_temperature CHECK (temperature_celsius BETWEEN -50 AND 70),
    CONSTRAINT ck_temperature_readings_humidity CHECK (humidity_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_temperature_readings_source CHECK (source IN ('SENSOR', 'WEATHER_SERVICE', 'MANUAL', 'SIMULATED')),
    CONSTRAINT ck_temperature_readings_quality CHECK (quality IN ('VALID', 'SUSPECT', 'INVALID'))
);

CREATE INDEX ix_temperature_readings_measured_at ON temperature_readings (measured_at);

CREATE TABLE cooling_points (
    id                 BIGINT IDENTITY(1,1) NOT NULL,
    name               NVARCHAR(120) NOT NULL,
    address            NVARCHAR(200) NOT NULL,
    neighborhood       NVARCHAR(120) NOT NULL,
    latitude           DECIMAL(9,6)  NOT NULL,
    longitude          DECIMAL(9,6)  NOT NULL,
    opening_hours      NVARCHAR(120) NOT NULL,
    capacity           INT           NOT NULL,
    accessibility_info NVARCHAR(500) NULL,
    active             BIT           NOT NULL DEFAULT 1,
    heat_zone_id       BIGINT        NULL,
    created_at         DATETIME2(6)  NOT NULL,
    updated_at         DATETIME2(6)  NOT NULL,
    CONSTRAINT pk_cooling_points PRIMARY KEY (id),
    CONSTRAINT uk_cooling_points_name_address UNIQUE (name, address),
    CONSTRAINT fk_cooling_points_heat_zone FOREIGN KEY (heat_zone_id) REFERENCES heat_zones (id),
    CONSTRAINT ck_cooling_points_capacity CHECK (capacity > 0),
    CONSTRAINT ck_cooling_points_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_cooling_points_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE INDEX ix_cooling_points_heat_zone_id ON cooling_points (heat_zone_id);
