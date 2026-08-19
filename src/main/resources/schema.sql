-- Esquema de PostgreSQL para F1 Simulator (Fase 2: migración completa)
-- Todas las entidades maestras (equipos, pilotos, vehículos, circuitos) se
-- persisten en PostgreSQL, además de los históricos (resultados y configuraciones).
-- Idempotente: CREATE TABLE IF NOT EXISTS permite ejecutarlo en cada arranque.

-- ============================================================
-- Entidades maestras
-- ============================================================

CREATE TABLE IF NOT EXISTS equipos (
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    pais   VARCHAR(100),
    motor  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS pilotos (
    id        INTEGER PRIMARY KEY,  -- el ID lo ingresa el usuario (igual que en memoria)
    nombre    VARCHAR(150) NOT NULL,
    rol       VARCHAR(20) CHECK (rol IN ('Líder', 'Escudero')),
    -- Al eliminar un equipo, sus pilotos quedan sin equipo (ON DELETE SET NULL),
    -- igual que ocurría con el HashMap (que lo permitía silenciosamente).
    equipo_id INTEGER REFERENCES equipos (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS vehiculos (
    id                   SERIAL PRIMARY KEY,
    -- equipo_id es la FK real; equipo_nombre conserva el nombre aunque el
    -- equipo se elimine (paridad con el modelo Vehiculo.equipoNombre).
    equipo_id            INTEGER REFERENCES equipos (id) ON DELETE SET NULL,
    equipo_nombre        VARCHAR(100) NOT NULL,
    modelo               VARCHAR(100) NOT NULL,
    motor                VARCHAR(100) NOT NULL,
    velocidad_maxima_kmh NUMERIC(8,2) NOT NULL,
    aceleracion_0100     NUMERIC(6,2) NOT NULL,
    tipo_neumatico       VARCHAR(20) NOT NULL
        CHECK (tipo_neumatico IN ('SECO', 'INTERMEDIO', 'LLUVIA'))
);

-- Relación N:M vehículo <-> piloto (Vehiculo.pilotoIds en memoria)
CREATE TABLE IF NOT EXISTS vehiculo_piloto (
    vehiculo_id INTEGER NOT NULL REFERENCES vehiculos (id) ON DELETE CASCADE,
    piloto_id   INTEGER NOT NULL REFERENCES pilotos (id) ON DELETE CASCADE,
    PRIMARY KEY (vehiculo_id, piloto_id)
);

-- Rendimiento por modo de conducción y clima (se calcula con VehiculoFactory;
-- el seed NO lo inserta a mano; BaseDatosInitializer lo completa al arrancar)
CREATE TABLE IF NOT EXISTS rendimiento_vehiculo (
    vehiculo_id             INTEGER NOT NULL REFERENCES vehiculos (id) ON DELETE CASCADE,
    modo_conduccion         VARCHAR(30) NOT NULL
        CHECK (modo_conduccion IN ('NORMAL', 'AGRESIVA', 'AHORRO_COMBUSTIBLE')),
    velocidad_promedio_kmh  NUMERIC(8,2) NOT NULL,
    consumo_seco            NUMERIC(8,3) NOT NULL,
    consumo_lluvioso        NUMERIC(8,3) NOT NULL,
    consumo_extremo         NUMERIC(8,3) NOT NULL,
    desgaste_seco           NUMERIC(8,3) NOT NULL,
    desgaste_lluvioso       NUMERIC(8,3) NOT NULL,
    desgaste_extremo        NUMERIC(8,3) NOT NULL,
    PRIMARY KEY (vehiculo_id, modo_conduccion)
);

CREATE TABLE IF NOT EXISTS circuitos (
    id             SERIAL PRIMARY KEY,
    nombre         VARCHAR(150) UNIQUE NOT NULL,
    pais           VARCHAR(100),
    longitud_km    NUMERIC(8,3) NOT NULL,
    vueltas        INTEGER NOT NULL,
    descripcion    TEXT,
    clima_promedio VARCHAR(20) NOT NULL
        CHECK (clima_promedio IN ('SECO', 'LLUVIOSO', 'EXTREMO'))
);

-- Récord de vuelta por circuito (Circuito.recordVuelta: un solo récord)
CREATE TABLE IF NOT EXISTS records_vuelta (
    circuito_id INTEGER PRIMARY KEY REFERENCES circuitos (id) ON DELETE CASCADE,
    piloto      VARCHAR(150) NOT NULL,
    tiempo_str  VARCHAR(30) NOT NULL,
    ano         INTEGER NOT NULL
);

-- Ganadores históricos por circuito (Circuito.ganadores)
CREATE TABLE IF NOT EXISTS ganadores_historicos (
    id          SERIAL PRIMARY KEY,
    circuito_id INTEGER NOT NULL REFERENCES circuitos (id) ON DELETE CASCADE,
    temporada   VARCHAR(20) NOT NULL,
    piloto      VARCHAR(150) NOT NULL
);

-- ============================================================
-- Históricos (se conservan; ahora con FKs reales)
-- ============================================================

CREATE TABLE IF NOT EXISTS historial_configuraciones (
    id                       SERIAL PRIMARY KEY,
    -- CASCADE: eliminar vehículo/piloto borra su historial de configuraciones
    vehiculo_id              INTEGER NOT NULL REFERENCES vehiculos (id) ON DELETE CASCADE,
    piloto_id                INTEGER NOT NULL REFERENCES pilotos (id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS resultados_clasificacion (
    id                      SERIAL PRIMARY KEY,
    sesion_id               INTEGER NOT NULL,
    piloto_id               INTEGER NOT NULL REFERENCES pilotos (id) ON DELETE CASCADE,
    piloto_nombre           VARCHAR(150) NOT NULL,
    circuito_id             INTEGER NOT NULL REFERENCES circuitos (id) ON DELETE CASCADE,
    tiempo_vuelta_segundos  NUMERIC(10,3) NOT NULL,
    clima_sesion            VARCHAR(20) NOT NULL
        CHECK (clima_sesion IN ('SECO', 'LLUVIOSO', 'EXTREMO')),
    posicion                INTEGER,
    fecha                   TIMESTAMP NOT NULL DEFAULT now()
);

-- ============================================================
-- Índices
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_resultados_circuito ON resultados_clasificacion (circuito_id);
CREATE INDEX IF NOT EXISTS idx_resultados_piloto ON resultados_clasificacion (piloto_id);
CREATE INDEX IF NOT EXISTS idx_config_vehiculo ON historial_configuraciones (vehiculo_id);
CREATE INDEX IF NOT EXISTS idx_config_piloto ON historial_configuraciones (piloto_id);
CREATE INDEX IF NOT EXISTS idx_vehiculo_piloto_piloto ON vehiculo_piloto (piloto_id);
CREATE INDEX IF NOT EXISTS idx_vehiculo_piloto_vehiculo ON vehiculo_piloto (vehiculo_id);