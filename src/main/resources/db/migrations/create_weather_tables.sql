CREATE TABLE weather_locations
(
    id          UUID                        NOT NULL,
    name        VARCHAR(100)                NOT NULL,
    latitude    DECIMAL(10, 7)              NOT NULL,
    longitude   DECIMAL(10, 7)              NOT NULL,
    state       VARCHAR(2)                  NOT NULL,
    country     VARCHAR(2),
    timezone    VARCHAR(50),
    property_id UUID,
    active      BOOLEAN                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_weather_locations PRIMARY KEY (id)
);

ALTER TABLE weather_locations
    ADD CONSTRAINT FK_WEATHER_LOCATIONS_ON_PROPERTY FOREIGN KEY (property_id) REFERENCES properties (id);

CREATE TABLE weather_currents
(
    id                  UUID                        NOT NULL,
    location_id         UUID                        NOT NULL,
    timestamp           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    temperature         DECIMAL(5, 2)               NOT NULL,
    feels_like          DECIMAL(5, 2),
    humidity            INTEGER,
    pressure            INTEGER,
    wind_speed          DECIMAL(5, 2),
    wind_direction      INTEGER,
    wind_gusts          DECIMAL(5, 2),
    clouds              INTEGER,
    visibility          INTEGER,
    uv_index            DECIMAL(3, 1),
    rainfall            DECIMAL(5, 2),
    snowfall            DECIMAL(5, 2),
    weather_condition   VARCHAR(50),
    weather_description VARCHAR(255),
    weather_code        INTEGER,
    source              VARCHAR(50),
    data_quality_score  DECIMAL(3, 2),
    fetch_latency_ms    INTEGER,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_weather_currents PRIMARY KEY (id)
);

ALTER TABLE weather_currents
    ADD CONSTRAINT FK_WEATHER_CURRENTS_ON_LOCATION FOREIGN KEY (location_id) REFERENCES weather_locations (id);

CREATE TABLE weather_forecasts
(
    id                         UUID                        NOT NULL,
    location_id                UUID                        NOT NULL,
    forecast_date              date                        NOT NULL,
    forecast_hour              INTEGER,
    temperature_min            DECIMAL(5, 2),
    temperature_max            DECIMAL(5, 2),
    temperature_avg            DECIMAL(5, 2),
    humidity_avg               INTEGER,
    humidity_min               INTEGER,
    humidity_max               INTEGER,
    rainfall_probability       INTEGER,
    rainfall_amount            DECIMAL(5, 2),
    rainfall_accumulated_7d    DECIMAL(6, 2),
    wind_speed_avg             DECIMAL(5, 2),
    wind_speed_max             DECIMAL(5, 2),
    wind_direction             INTEGER,
    uv_index_max               DECIMAL(3, 1),
    weather_condition          VARCHAR(50),
    weather_description        VARCHAR(255),
    weather_code               INTEGER,
    evapotranspiration         DECIMAL(5, 2),
    soil_moisture_0_to_10cm    DECIMAL(5, 2),
    soil_temperature_0_to_10cm DECIMAL(5, 2),
    created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_weather_forecasts PRIMARY KEY (id)
);

ALTER TABLE weather_forecasts
    ADD CONSTRAINT uk_forecast_location_date_hour UNIQUE (location_id, forecast_date, forecast_hour);

CREATE INDEX idx_forecast_location_date ON weather_forecasts (location_id, forecast_date);

ALTER TABLE weather_forecasts
    ADD CONSTRAINT FK_WEATHER_FORECASTS_ON_LOCATION FOREIGN KEY (location_id) REFERENCES weather_locations (id);

CREATE TABLE weather_alerts
(
    id                  UUID                        NOT NULL,
    location_id         UUID                        NOT NULL,
    alert_type          VARCHAR(50)                 NOT NULL,
    severity            VARCHAR(20)                 NOT NULL,
    title               VARCHAR(255)                NOT NULL,
    description         TEXT,
    start_time          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_active           BOOLEAN                     NOT NULL,
    recommended_actions TEXT,
    trigger_condition   VARCHAR(255),
    source              VARCHAR(50),
    notified            BOOLEAN                     NOT NULL,
    notified_at         TIMESTAMP WITHOUT TIME ZONE,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_weather_alerts PRIMARY KEY (id)
);

ALTER TABLE weather_alerts
    ADD CONSTRAINT FK_WEATHER_ALERTS_ON_LOCATION FOREIGN KEY (location_id) REFERENCES weather_locations (id);