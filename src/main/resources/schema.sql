-- Esquema de PostgreSQL para F1 Simulator
-- Solo cubre lo que el enunciado exige persistir de verdad:
-- resultados de clasificación e historial de configuraciones.
-- Circuitos, Pilotos, Equipos y Vehículos viven en memoria (HashMap),
-- según lo pedido en la sección "Gestión de Datos" del proyecto.

CREATE TABLE IF NOT EXISTS resultados_clasificacion (
    id                      SERIAL PRIMARY KEY,
    sesion_id               INTEGER NOT NULL,
    piloto_id               INTEGER NOT NULL,
    piloto_nombre           VARCHAR(150) NOT NULL,
    circuito_id             INTEGER NOT NULL,
    tiempo_vuelta_segundos  NUMERIC(10,3) NOT NULL,
    clima_sesion            VARCHAR(20) NOT NULL
        CHECK (clima_sesion IN ('SECO', 'LLUVIOSO', 'EXTREMO')),
    posicion                INTEGER,
    fecha                   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS historial_configuraciones (
    id                       SERIAL PRIMARY KEY,
    vehiculo_id              INTEGER NOT NULL,
    piloto_id                INTEGER NOT NULL,
    modo_conduccion          VARCHAR(30) NOT NULL
        CHECK (modo_conduccion IN ('NORMAL', 'AGRESIVA', 'AHORRO_COMBUSTIBLE')),
    carga_aerodinamica       VARCHAR(20) NOT NULL
        CHECK (carga_aerodinamica IN ('BAJA', 'MEDIA', 'ALTA')),
    presion_neumaticos       VARCHAR(20) NOT NULL
        CHECK (presion_neumaticos IN ('BAJA', 'ESTANDAR', 'ALTA')),
    estrategia_combustible   VARCHAR(20) NOT NULL
        CHECK (estrategia_combustible IN ('AGRESIVA', 'BALANCEADA', 'AHORRO')),
    fecha_creacion           TIMESTAMP NOT NULL DEFAULT now()
);

-- Índices para las consultas que ya tenemos planeadas (historial por vehículo/piloto,
-- comparación de tiempos por circuito)
CREATE INDEX IF NOT EXISTS idx_resultados_circuito ON resultados_clasificacion (circuito_id);
CREATE INDEX IF NOT EXISTS idx_resultados_piloto ON resultados_clasificacion (piloto_id);
CREATE INDEX IF NOT EXISTS idx_config_vehiculo ON historial_configuraciones (vehiculo_id);
CREATE INDEX IF NOT EXISTS idx_config_piloto ON historial_configuraciones (piloto_id);