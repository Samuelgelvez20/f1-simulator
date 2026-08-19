package com.f1simulator.repository;

import com.f1simulator.model.Equipo;
import com.f1simulator.model.Piloto;
import com.f1simulator.persistence.ConexionPostgreSQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de Equipos contra PostgreSQL (Fase 2: migración desde HashMap).
 * Misma API pública que el repositorio en memoria; el nombre del equipo sigue
 * actuando como clave (columna UNIQUE de la tabla equipos).
 *
 * El ON DELETE SET NULL de pilotos.equipo_id / vehiculos.equipo_id mantiene la
 * paridad con el comportamiento anterior: eliminar un equipo deja a sus
 * pilotos y vehículos sin equipo, sin romper la UI.
 */
public class EquipoRepository implements Repository<Equipo, String> {

    private final Connection conexion;

    public EquipoRepository() {
        this.conexion = ConexionPostgreSQL.getInstancia().getConexion();
    }

    @Override
    public Equipo guardar(Equipo equipo) {
        String sql = "INSERT INTO equipos (nombre, pais, motor) VALUES (?, ?, ?) "
                + "ON CONFLICT (nombre) DO UPDATE SET pais = EXCLUDED.pais, motor = EXCLUDED.motor";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, equipo.getNombre());
            stmt.setString(2, equipo.getPais());
            stmt.setString(3, equipo.getMotor());
            stmt.executeUpdate();
            return equipo;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el equipo " + equipo.getNombre(), e);
        }
    }

    @Override
    public Optional<Equipo> buscarPorId(String nombre) {
        List<Equipo> resultado = cargarEquipos("WHERE e.nombre = ?", nombre);
        return resultado.stream().findFirst();
    }

    @Override
    public List<Equipo> listarTodos() {
        return cargarEquipos("", (Object[]) null);
    }

    @Override
    public boolean actualizar(String nombre, Equipo equipoActualizado) {
        String sql = "UPDATE equipos SET pais = ?, motor = ? WHERE nombre = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, equipoActualizado.getPais());
            stmt.setString(2, equipoActualizado.getMotor());
            stmt.setString(3, nombre);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el equipo " + nombre, e);
        }
    }

    @Override
    public boolean eliminar(String nombre) {
        String sql = "DELETE FROM equipos WHERE nombre = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el equipo " + nombre, e);
        }
    }

    /**
     * Carga los equipos (con su lista de pilotos, como hacía el HashMap) y
     * opcionalmente los registros de pilotos asociados en una sola pasada.
     */
    private List<Equipo> cargarEquipos(String where, Object... parametros) {
        String sqlEquipos = "SELECT id, nombre, pais, motor FROM equipos e " + where + " ORDER BY e.nombre";
        List<Equipo> equipos = new ArrayList<>();
        Map<Integer, Equipo> porId = new HashMap<>();

        try (PreparedStatement stmt = conexion.prepareStatement(sqlEquipos)) {
            asignarParametros(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Equipo e = new Equipo(rs.getString("nombre"), rs.getString("pais"), rs.getString("motor"));
                    porId.put(rs.getInt("id"), e);
                    equipos.add(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar equipos", e);
        }

        if (equipos.isEmpty()) {
            return equipos;
        }

        String sqlPilotos = "SELECT p.id, p.nombre, p.rol, p.equipo_id FROM pilotos p "
                + "WHERE p.equipo_id IS NOT NULL ORDER BY p.nombre";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlPilotos); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Equipo equipo = porId.get(rs.getInt("equipo_id"));
                if (equipo != null) {
                    Piloto piloto = new Piloto(rs.getInt("id"), rs.getString("nombre"), equipo, rs.getString("rol"));
                    equipo.agregarPiloto(piloto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar los pilotos de los equipos", e);
        }

        return equipos;
    }

    private void asignarParametros(PreparedStatement stmt, Object... parametros) throws SQLException {
        if (parametros == null) {
            return;
        }
        for (int i = 0; i < parametros.length; i++) {
            stmt.setObject(i + 1, parametros[i]);
        }
    }
}