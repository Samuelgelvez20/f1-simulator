package com.f1simulator.repository;

import com.f1simulator.model.Circuito;
import com.f1simulator.model.GanadorHistorico;
import com.f1simulator.model.RecordVuelta;
import com.f1simulator.model.TipoClima;
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
 * Repositorio de Circuitos contra PostgreSQL (Fase 2: migración desde HashMap).
 * Misma API pública que el repositorio en memoria.
 *
 * Los récords de vuelta y ganadores históricos se cargan junto con el
 * circuito (records_vuelta / ganadores_historicos). No hay UI que los cree,
 * por lo que el guardado/actualizado solo escribe la fila base del circuito
 * y deja esas tablas hijas intactas.
 */
public class CircuitoRepository implements Repository<Circuito, Integer> {

    private final Connection conexion;

    public CircuitoRepository() {
        this.conexion = ConexionPostgreSQL.getInstancia().getConexion();
    }

    @Override
    public Circuito guardar(Circuito circuito) {
        String sql = "INSERT INTO circuitos (nombre, pais, longitud_km, vueltas, descripcion, clima_promedio) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (id) DO UPDATE SET "
                + "nombre = EXCLUDED.nombre, pais = EXCLUDED.pais, longitud_km = EXCLUDED.longitud_km, "
                + "vueltas = EXCLUDED.vueltas, descripcion = EXCLUDED.descripcion, "
                + "clima_promedio = EXCLUDED.clima_promedio";

        try (PreparedStatement stmt = conexion.prepareStatement(sql, new String[] { "id" })) {
            stmt.setString(1, circuito.getNombre());
            stmt.setString(2, circuito.getPais());
            stmt.setDouble(3, circuito.getLongitudKm());
            stmt.setInt(4, circuito.getVueltas());
            stmt.setString(5, circuito.getDescripcion());
            stmt.setString(6, circuito.getClimaPromedio().name());
            stmt.executeUpdate();

            if (circuito.getId() == 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        circuito.setId(keys.getInt(1));
                    }
                }
            }
            return circuito;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el circuito " + circuito.getNombre(), e);
        }
    }

    @Override
    public Optional<Circuito> buscarPorId(Integer id) {
        List<Circuito> resultado = cargarCircuitos("WHERE c.id = ?", id);
        return resultado.stream().findFirst();
    }

    @Override
    public List<Circuito> listarTodos() {
        return cargarCircuitos("", (Object[]) null);
    }

    @Override
    public boolean actualizar(Integer id, Circuito circuitoActualizado) {
        String sql = "UPDATE circuitos SET nombre = ?, pais = ?, longitud_km = ?, vueltas = ?, " + "descripcion = ?, clima_promedio = ? WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, circuitoActualizado.getNombre());
            stmt.setString(2, circuitoActualizado.getPais());
            stmt.setDouble(3, circuitoActualizado.getLongitudKm());
            stmt.setInt(4, circuitoActualizado.getVueltas());
            stmt.setString(5, circuitoActualizado.getDescripcion());
            stmt.setString(6, circuitoActualizado.getClimaPromedio().name());
            stmt.setInt(7, id);
            boolean actualizado = stmt.executeUpdate() > 0;

            if (actualizado && circuitoActualizado.getRecordVuelta() != null) {
                guardarRecordVuelta(id, circuitoActualizado.getRecordVuelta());
            }
            return actualizado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el circuito " + id, e);
        }
    }

    /**
     * Guarda el récord de vuelta del circuito. Como records_vuelta es una tabla
     * hija (1 récord por circuito), se borra el registro anterior y se inserta
     * el nuevo, para evitar duplicados si no hay una restricción UNIQUE en la
     * base de datos sobre circuito_id.
    */

    private void guardarRecordVuelta(int circuitoId, RecordVuelta record) {
        String deleteSql = "DELETE FROM records_vuelta WHERE circuito_id = ?";
        String insertSql = "INSERT INTO records_vuelta (circuito_id, piloto, tiempo_str, ano) VALUES (?, ?, ?, ?)";

        try (PreparedStatement deleteStmt = conexion.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, circuitoId);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al limpiar el récord de vuelta anterior del circuito " + circuitoId, e);
        }

        try (PreparedStatement insertStmt = conexion.prepareStatement(insertSql)) {
            insertStmt.setInt(1, circuitoId);
            insertStmt.setString(2, record.getPiloto());
            insertStmt.setString(3, record.getTiempoStr());
            insertStmt.setInt(4, record.getAno());
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el récord de vuelta del circuito " + circuitoId, e);
        }
    }

    /**
     * Registra al ganador de una simulación en el historial del circuito.
     * INSERT simple (append): el historial conserva todos los ganadores;
     * nunca se borra ni se sobrescribe un ganador anterior.
     */
    public void registrarGanador(int circuitoId, GanadorHistorico ganador) {
        String sql = "INSERT INTO ganadores_historicos (circuito_id, temporada, piloto) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, circuitoId);
            stmt.setString(2, ganador.getTemporada());
            stmt.setString(3, ganador.getPiloto());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el ganador del circuito " + circuitoId, e);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        // records_vuelta, ganadores_historicos y resultados_clasificacion se
        // borran en cascada (ON DELETE CASCADE).
        String sql = "DELETE FROM circuitos WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el circuito " + id, e);
        }
    }

    private List<Circuito> cargarCircuitos(String where, Object... parametros) {
        String sql = "SELECT c.id, c.nombre, c.pais, c.longitud_km, c.vueltas, c.descripcion, c.clima_promedio "
                + "FROM circuitos c " + where + " ORDER BY c.id";

        List<Circuito> circuitos = new ArrayList<>();
        Map<Integer, Circuito> porId = new HashMap<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            if (parametros != null) {
                for (int i = 0; i < parametros.length; i++) {
                    stmt.setObject(i + 1, parametros[i]);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Circuito c = new Circuito(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("pais"),
                            rs.getDouble("longitud_km"),
                            rs.getInt("vueltas"),
                            rs.getString("descripcion"),
                            TipoClima.valueOf(rs.getString("clima_promedio")));
                    porId.put(c.getId(), c);
                    circuitos.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar circuitos", e);
        }

        if (circuitos.isEmpty()) {
            return circuitos;
        }

        cargarRecords(porId);
        cargarGanadores(porId);
        return circuitos;
    }

    private void cargarRecords(Map<Integer, Circuito> porId) {
        if (porId.isEmpty()) return;
        String in = construirIn(porId.keySet().size());
        String sql = "SELECT circuito_id, piloto, tiempo_str, ano FROM records_vuelta "
                + "WHERE circuito_id IN (" + in + ")";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            asignarClaves(stmt, porId.keySet());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Circuito c = porId.get(rs.getInt("circuito_id"));
                    if (c != null) {
                        c.setRecordVuelta(new RecordVuelta(
                                rs.getString("tiempo_str"), rs.getString("piloto"), rs.getInt("ano")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar los récords de vuelta", e);
        }
    }

    private void cargarGanadores(Map<Integer, Circuito> porId) {
        if (porId.isEmpty()) return;
        String in = construirIn(porId.keySet().size());
        String sql = "SELECT circuito_id, temporada, piloto FROM ganadores_historicos "
                + "WHERE circuito_id IN (" + in + ") ORDER BY id";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            asignarClaves(stmt, porId.keySet());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Circuito c = porId.get(rs.getInt("circuito_id"));
                    if (c != null) {
                        c.agregarGanador(new GanadorHistorico(rs.getString("temporada"), rs.getString("piloto")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar los ganadores históricos", e);
        }
    }

    private String construirIn(int cantidad) {
        StringBuilder sb = new StringBuilder("?");
        for (int i = 1; i < cantidad; i++) {
            sb.append(", ?");
        }
        return sb.toString();
    }

    private void asignarClaves(PreparedStatement stmt, java.util.Set<Integer> claves) throws SQLException {
        int i = 1;
        for (Integer clave : claves) {
            stmt.setInt(i++, clave);
        }
    }
}