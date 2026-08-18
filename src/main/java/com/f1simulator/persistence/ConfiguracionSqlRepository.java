package com.f1simulator.persistence;

import com.f1simulator.model.ConfiguracionVehiculo;
import com.f1simulator.model.ModoConduccion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de Historial de Configuraciones contra PostgreSQL.
 * Persiste entre ejecuciones, cumpliendo la historia de usuario de
 * "revisar configuraciones previas" (un HashMap no sobrevive al cierre de la
 * app).
 */
public class ConfiguracionSqlRepository {

    private final Connection conexion;

    public ConfiguracionSqlRepository() {
        this.conexion = ConexionPostgreSQL.getInstancia().getConexion();
    }

    public ConfiguracionVehiculo guardar(ConfiguracionVehiculo config) {
        String sql = "INSERT INTO historial_configuraciones "
                + "(vehiculo_id, piloto_id, modo_conduccion, carga_aerodinamica, presion_neumaticos, estrategia_combustible) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, config.getVehiculoId());
            stmt.setInt(2, config.getPilotoId());
            stmt.setString(3, config.getModoConduccion().name());
            stmt.setString(4, config.getCargaAerodinamica());
            stmt.setString(5, config.getPresionNeumaticos());
            stmt.setString(6, config.getEstrategiaCombustible());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    config.setId(keys.getInt(1));
                }
            }
            return config;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar configuración de vehículo", e);
        }
    }

    public List<ConfiguracionVehiculo> historialPorVehiculo(int vehiculoId) {
        String sql = "SELECT * FROM historial_configuraciones WHERE vehiculo_id = ? ORDER BY fecha_creacion DESC";
        return consultar(sql, vehiculoId);
    }

    public List<ConfiguracionVehiculo> historialPorPiloto(int pilotoId) {
        String sql = "SELECT * FROM historial_configuraciones WHERE piloto_id = ? ORDER BY fecha_creacion DESC";
        return consultar(sql, pilotoId);
    }

    public List<ConfiguracionVehiculo> obtenerHistorial() {
        String sql = "SELECT * FROM historial_configuraciones ORDER BY fecha_creacion DESC";
        List<ConfiguracionVehiculo> resultado = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener el historial completo", e);
        }
        return resultado;
    }

    private List<ConfiguracionVehiculo> consultar(String sql, int parametro) {
        List<ConfiguracionVehiculo> resultado = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar historial de configuraciones", e);
        }
        return resultado;
    }

    private ConfiguracionVehiculo mapearFila(ResultSet rs) throws SQLException {
        ConfiguracionVehiculo config = new ConfiguracionVehiculo(
                rs.getInt("vehiculo_id"),
                rs.getInt("piloto_id"),
                ModoConduccion.valueOf(rs.getString("modo_conduccion")),
                rs.getString("carga_aerodinamica"),
                rs.getString("presion_neumaticos"),
                rs.getString("estrategia_combustible"));
        config.setId(rs.getInt("id"));
        config.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        return config;
    }
}