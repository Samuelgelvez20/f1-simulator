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
 * A diferencia de los repos en memoria, este SÍ persiste entre ejecuciones
 * de la aplicación, tal como lo exige la historia de usuario correspondiente.
 */
public class ResultadoClasificacionSqlRepository {

    private final Connection conexion;

    public ResultadoClasificacionSqlRepository() {
        this.conexion = ConexionPostgreSQL.getInstancia().getConexion();
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
        String sql = "SELECT * FROM resultados_clasificacion WHERE circuito_id = ? ORDER BY tiempo_vuelta_segundos ASC";
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
        String sql = "SELECT * FROM resultados_clasificacion WHERE piloto_id = ? ORDER BY fecha DESC";
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
        String sql = "SELECT * FROM resultados_clasificacion ORDER BY fecha DESC";
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

    private ResultadoClasificacion mapearFila(ResultSet rs) throws SQLException {
        ResultadoClasificacion r = new ResultadoClasificacion(
                rs.getInt("sesion_id"),
                rs.getInt("piloto_id"),
                rs.getString("piloto_nombre"),
                rs.getInt("circuito_id"),
                rs.getDouble("tiempo_vuelta_segundos"),
                TipoClima.valueOf(rs.getString("clima_sesion"))
        );
        r.setId(rs.getInt("id"));
        r.setPosicion(rs.getInt("posicion"));
        r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        return r;
    }
}