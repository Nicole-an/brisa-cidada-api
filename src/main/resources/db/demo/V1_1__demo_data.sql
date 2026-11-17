-- DADOS FICTÍCIOS DE DEMONSTRAÇÃO (somente perfil "dev").
-- Locais, sensores e leituras são inventados; as leituras usam origem SIMULATED
-- e NÃO representam medições reais.

INSERT INTO heat_zones (name, neighborhood, city, latitude, longitude, risk_level, active, created_at, updated_at)
VALUES (N'Zona Demo Centro', N'Centro (fictício)', N'Cidade Exemplo', -23.550000, -46.633000, 'HIGH', 1, SYSUTCDATETIME(), SYSUTCDATETIME()),
       (N'Zona Demo Parque', N'Jardim Verde (fictício)', N'Cidade Exemplo', -23.587000, -46.657000, 'MODERATE', 1, SYSUTCDATETIME(), SYSUTCDATETIME());

INSERT INTO sensors (code, name, latitude, longitude, status, installation_date, last_communication_at, heat_zone_id, created_at, updated_at)
SELECT 'DEMO-SENSOR-001', N'Sensor demo praça central', -23.550500, -46.633500, 'ACTIVE', CAST('2025-01-15' AS DATE), DATEADD(MINUTE, -10, SYSUTCDATETIME()), id, SYSUTCDATETIME(), SYSUTCDATETIME()
FROM heat_zones WHERE name = N'Zona Demo Centro';

INSERT INTO sensors (code, name, latitude, longitude, status, installation_date, last_communication_at, heat_zone_id, created_at, updated_at)
SELECT 'DEMO-SENSOR-002', N'Sensor demo parque', -23.587500, -46.657500, 'ACTIVE', CAST('2025-02-01' AS DATE), DATEADD(MINUTE, -15, SYSUTCDATETIME()), id, SYSUTCDATETIME(), SYSUTCDATETIME()
FROM heat_zones WHERE name = N'Zona Demo Parque';

INSERT INTO temperature_readings (sensor_id, measured_at, temperature_celsius, humidity_percent, source, quality, created_at, updated_at)
SELECT id, DATEADD(MINUTE, -40, SYSUTCDATETIME()), 34.50, 55.00, 'SIMULATED', 'VALID', SYSUTCDATETIME(), SYSUTCDATETIME()
FROM sensors WHERE code = 'DEMO-SENSOR-001';

INSERT INTO temperature_readings (sensor_id, measured_at, temperature_celsius, humidity_percent, source, quality, created_at, updated_at)
SELECT id, DATEADD(MINUTE, -10, SYSUTCDATETIME()), 36.20, 50.00, 'SIMULATED', 'VALID', SYSUTCDATETIME(), SYSUTCDATETIME()
FROM sensors WHERE code = 'DEMO-SENSOR-001';

INSERT INTO temperature_readings (sensor_id, measured_at, temperature_celsius, humidity_percent, source, quality, created_at, updated_at)
SELECT id, DATEADD(MINUTE, -15, SYSUTCDATETIME()), 28.40, 62.00, 'SIMULATED', 'VALID', SYSUTCDATETIME(), SYSUTCDATETIME()
FROM sensors WHERE code = 'DEMO-SENSOR-002';

INSERT INTO cooling_points (name, address, neighborhood, latitude, longitude, opening_hours, capacity, accessibility_info, active, heat_zone_id, created_at, updated_at)
SELECT N'Biblioteca Demo Climatizada', N'Rua Exemplo, 100 (fictício)', N'Centro (fictício)', -23.551000, -46.634000, N'Seg-Sex 08:00-20:00', 80,
       N'Rampa de acesso e banheiro adaptado', 1, id, SYSUTCDATETIME(), SYSUTCDATETIME()
FROM heat_zones WHERE name = N'Zona Demo Centro';

INSERT INTO cooling_points (name, address, neighborhood, latitude, longitude, opening_hours, capacity, accessibility_info, active, heat_zone_id, created_at, updated_at)
VALUES (N'Centro Comunitário Demo', N'Avenida Modelo, 500 (fictício)', N'Vila Nova (fictício)', -23.600000, -46.670000, N'Todos os dias 09:00-18:00', 50,
        NULL, 1, NULL, SYSUTCDATETIME(), SYSUTCDATETIME());
