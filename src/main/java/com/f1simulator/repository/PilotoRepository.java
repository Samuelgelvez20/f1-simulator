package com.f1simulator.repository;

import com.f1simulator.model.Equipo;
import com.f1simulator.model.Piloto;
import com.f1simulator.persistence.ConexionPostgreSQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Pilotos contra PostgreSQL (Fase 2: migración desde HashMap).
 * Misma API pública que el repositorio en memoria: el ID del piloto lo
 * ingresa el usuario y es la clave primaria de la tabla pilotos.
 *
 * La relación con el equipo se persiste como FK (pilotos.equipo_id). Al
 * cargar un piloto se reconstruye su objeto Equipo (nombre, país, motor)
 * para que la UI siga funcionando sin cambios.
 */
public class PilotoRepository implements Repository<Piloto, Integer> {

    private static final String COLUMNAS = "p.id, p.nombre, p.rol, p.equipo_id, "
            + "e.nombre AS equipo_nombre, e.pais AS equipo_pais, e.motor AS equipo_motor";

    private final Connection conexion;

    public PilotoRepository() {
        this.conexion = ConexionPostgreSQL.getInstancia().getConexion();
    }

    @Override
    public Piloto guardar(Piloto piloto) {
        String sql = "INSERT INTO pilotos (id, nombre, rol, equipo_id) VALUES (?, ?, ?, "
                + "(SELECT id FROM equipos WHERE nombre = ?)) "
                + "ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre, rol = EXCLUDED.rol, "
                + "equipo_id = EXCLUDED.equipo_id";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, piloto.getId());
            stmt.setString(2, piloto.getNombre());
            stmt.setString(3, piloto.getRol());
            stmt.setString(4, piloto.getEquipo() != null ? piloto.getEquipo().getNombre() : null);
            stmt.executeUpdate();
            return piloto;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el piloto " + piloto.getNombre(), e);
        }
    }

    @Override
    public Optional<Piloto> buscarPorId(Integer id) {
        List<Piloto> resultado = cargarPilotos("WHERE p.id = ?", id);
        return resultado.stream().findFirst();
    }

    @Override
    public List<Piloto> listarTodos() {
        return cargarPilotos("", (Object[]) null);
    }

    @Override
    public boolean actualizar(Integer id, Piloto pilotoActualizado) {
        String sql = "UPDATE pilotos SET nombre = ?, rol = ?, equipo_id = "
                + "(SELECT id FROM equipos WHERE nombre = ?) WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, pilotoActualizado.getNombre());
            stmt.setString(2, pilotoActualizado.getRol());
            stmt.setString(3, pilotoActualizado.getEquipo() != null ? pilotoActualizado.getEquipo().getNombre() : null);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el piloto " + id, e);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        // Los históricos (resultados, configuraciones) y las asignaciones
        // vehiculo_piloto se borran en cascada (ON DELETE CASCADE).
        String sql = "DELETE FROM pilotos WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el piloto " + id, e);
        }
    }

    private List<Piloto> cargarPilotos(String where, Object... parametros) {
        String sql = "SELECT " + COLUMNAS + " FROM pilotos p "
                + "LEFT JOIN equipos e ON e.id = p.equipo_id " + where + " ORDER BY p.id";
        List<Piloto> pilotos = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            if (parametros != null) {
                for (int i = 0; i < parametros.length; i++) {
                    stmt.setObject(i + 1, parametros[i]);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Equipo equipo = null;
                    if (rs.getString("equipo_nombre") != null) {
                        equipo = new Equipo(rs.getString("equipo_nombre"),
                                rs.getString("equipo_pais"), rs.getString("equipo_motor"));
                    }
                    Piloto piloto = new Piloto(rs.getInt("id"), rs.getString("nombre"), equipo, rs.getString("rol"));
                    pilotos.add(piloto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pilotos", e);
        }
        return pilotos;
    }
}