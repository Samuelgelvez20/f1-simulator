package com.f1simulator.persistence;

import com.f1simulator.factory.VehiculoFactory;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.RendimientoPorModo;
import com.f1simulator.model.TipoClima;
import com.f1simulator.model.TipoNeumatico;
import com.f1simulator.model.Vehiculo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Inicializador idempotente de la base de datos (Fase 2): ejecuta schema.sql
 * y seed.sql al arrancar (CREATE TABLE IF NOT EXISTS + ON CONFLICT DO NOTHING),
 * de modo que la primera ejecución funcione sin psql manual y las siguientes
 * no rompan nada.
 *
 * Además completa el rendimiento por modo de los vehículos sembrados: el seed
 * NO inserta rendimiento a mano (exigencia del enunciado) sino que este se
 * calcula siempre con {@link VehiculoFactory} y se persiste en
 * rendimiento_vehiculo.
 */
public class BaseDatosInitializer {

    private static final String SCHEMA = "schema.sql";
    private static final String SEED = "seed.sql";

    private BaseDatosInitializer() {
    }

    public static void ejecutar() {
        Connection conexion = ConexionPostgreSQL.getInstancia().getConexion();
        ejecutarScript(conexion, SCHEMA);
        ejecutarScript(conexion, SEED);
        completarRendimientoFaltante(conexion);
    }

    private static void ejecutarScript(Connection conexion, String recurso) {
        List<String> sentencias = leerSentencias(recurso);
        try (Statement stmt = conexion.createStatement()) {
            for (String sql : sentencias) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    throw new RuntimeException(
                            "Error ejecutando sentencia de " + recurso + ": " + resumir(sql), e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar el esquema desde " + recurso, e);
        }
    }

    private static List<String> leerSentencias(String recurso) {
        List<String> sentencias = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        try (InputStream in = BaseDatosInitializer.class.getClassLoader().getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso " + recurso + " en el classpath.");
            }
            try (BufferedReader lector = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    String lineaLimpia = linea.trim();
                    if (lineaLimpia.isEmpty() || lineaLimpia.startsWith("--")) {
                        continue;
                    }
                    actual.append(linea).append('\n');
                    if (lineaLimpia.endsWith(";")) {
                        sentencias.add(actual.toString());
                        actual.setLength(0);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el recurso " + recurso, e);
        }
        if (!actual.toString().isBlank()) {
            throw new IllegalStateException("El recurso " + recurso + " termina sin punto y coma.");
        }
        return sentencias;
    }

    private static String resumir(String sql) {
        String unalinea = sql.replace('\n', ' ').trim();
        return unalinea.length() > 120 ? unalinea.substring(0, 120) + "..." : unalinea;
    }

    /**
     * Para cada vehículo sin filas en rendimiento_vehiculo (caso del seed),
     * calcula su rendimiento con VehiculoFactory y lo persiste.
     */
    private static void completarRendimientoFaltante(Connection conexion) {
        String consulta = "SELECT id, equipo_nombre, modelo, motor, velocidad_maxima_kmh, "
                + "aceleracion_0100, tipo_neumatico FROM vehiculos v "
                + "WHERE NOT EXISTS (SELECT 1 FROM rendimiento_vehiculo r WHERE r.vehiculo_id = v.id)";

        List<Vehiculo> pendientes = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(consulta); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vehiculo v = new Vehiculo(
                        rs.getInt("id"),
                        rs.getString("equipo_nombre"),
                        rs.getString("modelo"),
                        rs.getString("motor"),
                        rs.getDouble("velocidad_maxima_kmh"),
                        rs.getDouble("aceleracion_0100"),
                        TipoNeumatico.valueOf(rs.getString("tipo_neumatico")));
                pendientes.add(v);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar vehículos sin rendimiento", e);
        }

        if (pendientes.isEmpty()) {
            return;
        }

        VehiculoFactory factory = new VehiculoFactory();
        String insertar = "INSERT INTO rendimiento_vehiculo "
                + "(vehiculo_id, modo_conduccion, velocidad_promedio_kmh, "
                + "consumo_seco, consumo_lluvioso, consumo_extremo, "
                + "desgaste_seco, desgaste_lluvioso, desgaste_extremo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conexion.prepareStatement(insertar)) {
            for (Vehiculo v : pendientes) {
                Vehiculo completo = factory.crearVehiculo(v.getEquipoNombre(), v.getModelo(),
                        v.getMotor(), v.getVelocidadMaximaKmh(), v.getAceleracion0100(),
                        v.getTipoNeumatico());
                for (ModoConduccion modo : ModoConduccion.values()) {
                    RendimientoPorModo r = completo.getRendimiento().get(modo);
                    stmt.setInt(1, v.getId());
                    stmt.setString(2, modo.name());
                    stmt.setDouble(3, r.getVelocidadPromedioKmh());
                    stmt.setDouble(4, r.getConsumo(TipoClima.SECO));
                    stmt.setDouble(5, r.getConsumo(TipoClima.LLUVIOSO));
                    stmt.setDouble(6, r.getConsumo(TipoClima.EXTREMO));
                    stmt.setDouble(7, r.getDesgaste(TipoClima.SECO));
                    stmt.setDouble(8, r.getDesgaste(TipoClima.LLUVIOSO));
                    stmt.setDouble(9, r.getDesgaste(TipoClima.EXTREMO));
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error al persistir rendimiento calculado por VehiculoFactory", e);
        }
    }
}