package com.f1simulator.persistence;

import com.f1simulator.model.ResultadoClasificacion;
import com.f1simulator.model.TipoClima;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de Resultados de Clasificación contra PostgreSQL, usando
 * PreparedStatement (mismo patrón JDBC visto en clase con MySQL).
 *
 * Fase 2: se corrige la consulta por circuito (antes buscaba una columna
 * circuito_nombre inexistente, lo que hacía fallar el reporte "Comparación
 * de Tiempos"), y ahora todas las consultas traen el nombre del circuito y
 * el modelo del vehículo mediante JOIN/subconsulta, alimentando las columnas
 * "Circuito" y "Vehículo" de la UI de reportes.
 */
public class ResultadoClasificacionSqlRepository {

    private final Connection conexion;

    public ResultadoClasificacionSqlRepository() {
        this.conexion = ConexionPostgreSQL.getInstancia().getConexion();
    }

    /**
     * Resultados de un circuito buscado por NOMBRE (lo que pide la UI de
     * reportes). Se resuelve con JOIN a la tabla circuitos.
     */
    public List<ResultadoClasificacion> obtenerPorCircuito(String nombreCircuito) {
        String sql = "SELECT " + COLUMNAS + " FROM resultados_clasificacion r "
                + "JOIN circuitos c ON c.id = r.circuito_id "
                + "WHERE LOWER(c.nombre) = LOWER(?) ORDER BY r.tiempo_vuelta_segundos ASC";
        List<ResultadoClasificacion> resultados = new ArrayList<>();

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombreCircuito);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar resultados por nombre de circuito", e);
        }
        return resultados;
    }

    public ResultadoClasificacion guardar(ResultadoClasificacion resultado) {
        String sql = "INSERT INTO resultados_clasificacion "
                + "(sesion_id, piloto_id, piloto_nombre, circuito_id, tiempo_vuelta_segundos, clima_sesion, posicion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, resultado.getSesionId());
            stmt.setInt(2, resultado.getPilotoId());
            stmt.setString(3, resultado.getPilotoNombre());
            stmt.setInt(4, resultado.getCircuitoId());
            stmt.setDouble(5, resultado.getTiempoVueltaSegundos());
            stmt.setString(6, resultado.getClimaSesion().name());
            stmt.setInt(7, resultado.getPosicion());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    resultado.setId(keys.getInt(1));
                }
            }
            return resultado;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar resultado de clasificación", e);
        }
    }

    public List<ResultadoClasificacion> listarPorCircuito(int circuitoId) {
        String sql = "SELECT " + COLUMNAS + " FROM resultados_clasificacion r "
                + "JOIN circuitos c ON c.id = r.circuito_id "
                + "WHERE r.circuito_id = ? ORDER BY r.tiempo_vuelta_segundos ASC";
        List<ResultadoClasificacion> resultados = new ArrayList<>();

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, circuitoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar resultados por circuito", e);
        }
        return resultados;
    }

    public List<ResultadoClasificacion> listarPorPiloto(int pilotoId) {
        String sql = "SELECT " + COLUMNAS + " FROM resultados_clasificacion r "
                + "JOIN circuitos c ON c.id = r.circuito_id "
                + "WHERE r.piloto_id = ? ORDER BY r.fecha DESC";
        List<ResultadoClasificacion> resultados = new ArrayList<>();

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, pilotoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar resultados por piloto", e);
        }
        return resultados;
    }

    public List<ResultadoClasificacion> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM resultados_clasificacion r "
                + "JOIN circuitos c ON c.id = r.circuito_id "
                + "ORDER BY r.fecha DESC";
        List<ResultadoClasificacion> resultados = new ArrayList<>();

        try (PreparedStatement stmt = conexion.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resultados.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar resultados", e);
        }
        return resultados;
    }

    /**
     * Columnas comunes: r.* más el nombre del circuito (JOIN) y el modelo del
     * vehículo asignado al piloto (subconsulta, para no duplicar filas si un
     * piloto tuviera más de un vehículo).
     */
    private static final String COLUMNAS = "r.id, r.sesion_id, r.piloto_id, r.piloto_nombre, "
            + "r.circuito_id, r.tiempo_vuelta_segundos, r.clima_sesion, r.posicion, r.fecha, "
            + "c.nombre AS circuito_nombre, "
            + "(SELECT v.modelo FROM vehiculo_piloto vp JOIN vehiculos v ON v.id = vp.vehiculo_id "
            + " WHERE vp.piloto_id = r.piloto_id LIMIT 1) AS vehiculo_modelo";

    private ResultadoClasificacion mapearFila(ResultSet rs) throws SQLException {
        ResultadoClasificacion r = new ResultadoClasificacion(
                rs.getInt("sesion_id"),
                rs.getInt("piloto_id"),
                rs.getString("piloto_nombre"),
                rs.getInt("circuito_id"),
                rs.getDouble("tiempo_vuelta_segundos"),
                TipoClima.valueOf(rs.getString("clima_sesion")));
        r.setId(rs.getInt("id"));
        r.setPosicion(rs.getInt("posicion"));
        if (rs.getTimestamp("fecha") != null) {
            r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        }
        r.setCircuitoNombre(rs.getString("circuito_nombre"));
        r.setVehiculoModelo(rs.getString("vehiculo_modelo"));
        return r;
    }
}