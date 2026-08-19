-- Datos iniciales de F1 Simulator (Fase 2)
-- Son los mismos datos de ejemplo que ya funcionaban en memoria, para
-- mantener consistencia entre el comportamiento anterior y PostgreSQL.
-- Idempotente: ON CONFLICT DO NOTHING permite ejecutarlo en cada arranque.

-- ============================================================
-- Equipos (los IDs se generan en orden: 1, 2, 3)
-- ============================================================

INSERT INTO equipos (nombre, pais, motor)
VALUES ('Red Bull Racing', 'Austria', 'Honda'),
       ('Scuderia Ferrari', 'Italia', 'Ferrari'),
       ('Mercedes-AMG Petronas', 'Alemania', 'Mercedes'),
       ('McLaren', 'Reino Unido', 'Mercedes'),
       ('Aston Martin', 'Reino Unido', 'Mercedes'),
       ('Alpine', 'Francia', 'Renault'),
       ('Haas', 'Estados Unidos', 'Ferrari'),
       ('Visa Cash App RB', 'Italia', 'Honda'),
       ('Williams', 'Reino Unido', 'Mercedes'),
       ('Kick Sauber', 'Suiza', 'Ferrari')
ON CONFLICT (nombre) DO NOTHING;

-- ============================================================
-- Pilotos (ID ingresado por el usuario, igual que en memoria)
-- ============================================================

INSERT INTO pilotos (id, nombre, rol, equipo_id)
VALUES (1, 'Max Verstappen', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Red Bull Racing')),
       (2, 'Sergio Pérez', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Red Bull Racing')),
       (3, 'Charles Leclerc', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Scuderia Ferrari')),
       (4, 'Lewis Hamilton', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Mercedes-AMG Petronas')),
       (5, 'George Russell', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Mercedes-AMG Petronas')),
       (6, 'Carlos Sainz', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Scuderia Ferrari')),
       (7, 'Lando Norris', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'McLaren')),
       (8, 'Oscar Piastri', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'McLaren')),
       (9, 'Fernando Alonso', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Aston Martin')),
       (10, 'Lance Stroll', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Aston Martin')),
       (11, 'Esteban Ocon', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Alpine')),
       (12, 'Pierre Gasly', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Alpine')),
       (13, 'Valtteri Bottas', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Kick Sauber')),
       (14, 'Zhou Guanyu', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Kick Sauber')),
       (15, 'Kevin Magnussen', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Haas')),
       (16, 'Nico Hülkenberg', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Haas')),
       (17, 'Yuki Tsunoda', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Visa Cash App RB')),
       (18, 'Daniel Ricciardo', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Visa Cash App RB')),
       (19, 'Alexander Albon', 'Líder',
        (SELECT id FROM equipos WHERE nombre = 'Williams')),
       (20, 'Logan Sargeant', 'Escudero',
        (SELECT id FROM equipos WHERE nombre = 'Williams'))
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Circuitos (IDs 1, 2, 3)
-- ============================================================

INSERT INTO circuitos (nombre, pais, longitud_km, vueltas, descripcion, clima_promedio)
VALUES ('Circuito de Mónaco', 'Mónaco', 3.34, 78, 'Circuito urbano de Montecarlo', 'SECO'),
       ('Silverstone', 'Reino Unido', 5.89, 52, 'Circuito de velocidad en Northamptonshire', 'LLUVIOSO'),
       ('Spa-Francorchamps', 'Bélgica', 7.00, 44, 'Circuito de los Ardenas, el más largo del calendario', 'EXTREMO'),
       ('Circuito de Monza', 'Italia', 5.793, 53, 'El Templo de la Velocidad', 'SECO'),
       ('Interlagos', 'Brasil', 4.309, 71, 'Circuito de Interlagos en São Paulo', 'LLUVIOSO'),
       ('Circuito de Yas Marina', 'Emiratos Árabes Unidos', 5.281, 58, 'Circuito nocturno de Abu Dabi', 'SECO'),
       ('Circuito de Suzuka', 'Japón', 5.807, 53, 'Trazado en figura de ocho de Suzuka', 'EXTREMO')
ON CONFLICT (nombre) DO NOTHING;

-- ============================================================
-- Vehículos (IDs 1, 2, 3).
-- NOTA: el rendimiento por modo/conducción NO se inserta aquí; lo calcula
-- VehiculoFactory (BaseDatosInitializer lo completa al arrancar), tal como
-- exige el enunciado: el rendimiento se genera automáticamente, no a mano.
-- ============================================================

INSERT INTO vehiculos (id, equipo_id, equipo_nombre, modelo, motor, velocidad_maxima_kmh, aceleracion_0100, tipo_neumatico)
VALUES (1, (SELECT id FROM equipos WHERE nombre = 'Red Bull Racing'), 'Red Bull Racing',
        'RB20', 'Honda', 360, 2.5, 'SECO'),
       (2, (SELECT id FROM equipos WHERE nombre = 'Scuderia Ferrari'), 'Scuderia Ferrari',
        'SF-24', 'Ferrari', 355, 2.6, 'INTERMEDIO'),
       (3, (SELECT id FROM equipos WHERE nombre = 'Mercedes-AMG Petronas'), 'Mercedes-AMG Petronas',
        'W15', 'Mercedes', 350, 2.7, 'LLUVIA'),
       (4, (SELECT id FROM equipos WHERE nombre = 'McLaren'), 'McLaren',
        'MCL38', 'Mercedes', 358, 2.55, 'SECO'),
       (5, (SELECT id FROM equipos WHERE nombre = 'Aston Martin'), 'Aston Martin',
        'AMR24', 'Mercedes', 352, 2.65, 'INTERMEDIO'),
       (6, (SELECT id FROM equipos WHERE nombre = 'Alpine'), 'Alpine',
        'A524', 'Renault', 345, 2.7, 'LLUVIA'),
       (7, (SELECT id FROM equipos WHERE nombre = 'Haas'), 'Haas',
        'VF-24', 'Ferrari', 343, 2.68, 'INTERMEDIO'),
       (8, (SELECT id FROM equipos WHERE nombre = 'Visa Cash App RB'), 'Visa Cash App RB',
        'VCARB01', 'Honda', 349, 2.6, 'SECO'),
       (9, (SELECT id FROM equipos WHERE nombre = 'Williams'), 'Williams',
        'FW46', 'Mercedes', 342, 2.7, 'LLUVIA'),
       (10, (SELECT id FROM equipos WHERE nombre = 'Kick Sauber'), 'Kick Sauber',
        'C44', 'Ferrari', 347, 2.68, 'INTERMEDIO')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Asignaciones piloto <-> vehículo (misma regla que en memoria:
-- el piloto solo puede asignarse a un vehículo de su propio equipo).
-- Sin estas asignaciones la simulación del Día 3 no tiene participantes.
-- ============================================================

INSERT INTO vehiculo_piloto (vehiculo_id, piloto_id)
VALUES ((SELECT id FROM vehiculos WHERE modelo = 'RB20'), 1),   -- RB20 <- Max Verstappen
       ((SELECT id FROM vehiculos WHERE modelo = 'RB20'), 2),   -- RB20 <- Sergio Pérez
       ((SELECT id FROM vehiculos WHERE modelo = 'SF-24'), 3),  -- SF-24 <- Charles Leclerc
       ((SELECT id FROM vehiculos WHERE modelo = 'SF-24'), 6),  -- SF-24 <- Carlos Sainz
       ((SELECT id FROM vehiculos WHERE modelo = 'W15'), 4),    -- W15 <- Lewis Hamilton
       ((SELECT id FROM vehiculos WHERE modelo = 'W15'), 5),    -- W15 <- George Russell
       ((SELECT id FROM vehiculos WHERE modelo = 'MCL38'), 7),  -- MCL38 <- Lando Norris
       ((SELECT id FROM vehiculos WHERE modelo = 'MCL38'), 8),  -- MCL38 <- Oscar Piastri
       ((SELECT id FROM vehiculos WHERE modelo = 'AMR24'), 9),  -- AMR24 <- Fernando Alonso
       ((SELECT id FROM vehiculos WHERE modelo = 'AMR24'), 10), -- AMR24 <- Lance Stroll
       ((SELECT id FROM vehiculos WHERE modelo = 'A524'), 11),  -- A524 <- Esteban Ocon
       ((SELECT id FROM vehiculos WHERE modelo = 'A524'), 12),  -- A524 <- Pierre Gasly
       ((SELECT id FROM vehiculos WHERE modelo = 'C44'), 13),   -- C44 <- Valtteri Bottas
       ((SELECT id FROM vehiculos WHERE modelo = 'C44'), 14),   -- C44 <- Zhou Guanyu
       ((SELECT id FROM vehiculos WHERE modelo = 'VF-24'), 15), -- VF-24 <- Kevin Magnussen
       ((SELECT id FROM vehiculos WHERE modelo = 'VF-24'), 16), -- VF-24 <- Nico Hülkenberg
       ((SELECT id FROM vehiculos WHERE modelo = 'VCARB01'), 17), -- VCARB01 <- Yuki Tsunoda
       ((SELECT id FROM vehiculos WHERE modelo = 'VCARB01'), 18), -- VCARB01 <- Daniel Ricciardo
       ((SELECT id FROM vehiculos WHERE modelo = 'FW46'), 19),  -- FW46 <- Alexander Albon
       ((SELECT id FROM vehiculos WHERE modelo = 'FW46'), 20)   -- FW46 <- Logan Sargeant
ON CONFLICT (vehiculo_id, piloto_id) DO NOTHING;

-- Records de vuelta y ganadores históricos quedan vacíos (igual que en
-- memoria: no hay UI que los cree; se muestran como "Sin récord" en el detalle).