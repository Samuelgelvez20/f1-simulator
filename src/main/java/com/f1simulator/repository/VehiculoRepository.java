package com.f1simulator.repository;

import com.f1simulator.factory.VehiculoFactory;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.RendimientoPorModo;
import com.f1simulator.model.TipoClima;
import com.f1simulator.model.TipoNeumatico;
import com.f1simulator.model.Vehiculo;
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
 * Repositorio de Vehículos contra PostgreSQL (Fase 2: migración desde HashMap).
 * Misma API pública que el repositorio en memoria.
 *
 * Persiste: datos base del vehículo (tabla vehiculos), su rendimiento por
 * modo/clima (rendimiento_vehiculo, calculado por VehiculoFactory) y las
 * asignaciones piloto <-> vehículo (vehiculo_piloto). El guardado/actualizado
 * es transaccional para que nunca queden filas a medias.
 *
 * Las asignaciones piloto <-> vehículo se persisten al momento: la UI llama
 * a actualizar() tras cada modificación (p. ej. asignarPilotoAVehiculo).
 *
 * Nota de concurrencia: la simulación (Día 3) solo escribe desde el hilo
 * principal después de invokeAll; esta conexión compartida no se usa desde
 * varios hilos a la vez.
 */
public class VehiculoRepository implements Repository<Vehiculo, Integer> {

    private final Connection conexion;

    public VehiculoRepository() {
        this.conexion = ConexionPostgreSQL.getInstancia().getConexion();
    }

    @Override
    public Vehiculo guardar(Vehiculo vehiculo) {
        enTransaccion(() -> {
            String sql = "INSERT INTO vehiculos "
                    + "(equipo_id, equipo_nombre, modelo, motor, velocidad_maxima_kmh, aceleracion_0100, tipo_neumatico) "
                    + "VALUES ((SELECT id FROM equipos WHERE nombre = ?), ?, ?, ?, ?, ?, ?) "
                    + "ON CONFLICT (id) DO UPDATE SET "
                    + "equipo_id = EXCLUDED.equipo_id, equipo_nombre = EXCLUDED.equipo_nombre, "
                    + "modelo = EXCLUDED.modelo, motor = EXCLUDED.motor, "
                    + "velocidad_maxima_kmh = EXCLUDED.velocidad_maxima_kmh, "
                    + "aceleracion_0100 = EXCLUDED.aceleracion_0100, tipo_neumatico = EXCLUDED.tipo_neumatico";

            try (PreparedStatement stmt = conexion.prepareStatement(sql, new String[] { "id" })) {
                stmt.setString(1, vehiculo.getEquipoNombre());
                stmt.setString(2, vehiculo.getEquipoNombre());
                stmt.setString(3, vehiculo.getModelo());
                stmt.setString(4, vehiculo.getMotor());
                stmt.setDouble(5, vehiculo.getVelocidadMaximaKmh());
                stmt.setDouble(6, vehiculo.getAceleracion0100());
                stmt.setString(7, vehiculo.getTipoNeumatico().name());
                stmt.executeUpdate();

                if (vehiculo.getId() == 0) {
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            vehiculo.setId(keys.getInt(1));
                        }
                    }
                }
            }
            persistirRendimientoYAsignaciones(vehiculo);
        });
        return vehiculo;
    }

    @Override
    public Optional<Vehiculo> buscarPorId(Integer id) {
        List<Vehiculo> resultado = cargarVehiculos("WHERE v.id = ?", id);
        return resultado.stream().findFirst();
    }

    @Override
    public List<Vehiculo> listarTodos() {
        return cargarVehiculos("", (Object[]) null);
    }

    @Override
    public boolean actualizar(Integer id, Vehiculo actualizado) {
        if (buscarPorId(id).isEmpty()) {
            return false;
        }
        enTransaccion(() -> {
            String sql = "UPDATE vehiculos SET "
                    + "equipo_id = (SELECT id FROM equipos WHERE nombre = ?), equipo_nombre = ?, "
                    + "modelo = ?, motor = ?, velocidad_maxima_kmh = ?, aceleracion_0100 = ?, tipo_neumatico = ? "
                    + "WHERE id = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                stmt.setString(1, actualizado.getEquipoNombre());
                stmt.setString(2, actualizado.getEquipoNombre());
                stmt.setString(3, actualizado.getModelo());
                stmt.setString(4, actualizado.getMotor());
                stmt.setDouble(5, actualizado.getVelocidadMaximaKmh());
                stmt.setDouble(6, actualizado.getAceleracion0100());
                stmt.setString(7, actualizado.getTipoNeumatico().name());
                stmt.setInt(8, id);
                stmt.executeUpdate();
            }
            // El objeto reemplazado llega con pilotoIds vacío (VehiculoFactory lo
            // crea nuevo): las asignaciones anteriores se pierden, igual que ocurría
            // con el HashMap que reemplazaba el objeto completo.
            persistirRendimientoYAsignaciones(actualizado);
        });
        return true;
    }

    @Override
    public boolean eliminar(Integer id) {
        // vehiculo_piloto, rendimiento_vehiculo e historial_configuraciones se
        // borran en cascada (ON DELETE CASCADE).
        String sql = "DELETE FROM vehiculos WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el vehículo " + id, e);
        }
    }

    /** Búsqueda por características: modelo, motor o equipo (historia de usuario). */
    public List<Vehiculo> buscarPorCaracteristica(String texto) {
        String filtro = "%" + texto.toLowerCase() + "%";
        return cargarVehiculos("WHERE LOWER(v.modelo) LIKE ? OR LOWER(v.motor) LIKE ? OR LOWER(v.equipo_nombre) LIKE ?",
                filtro, filtro, filtro);
    }

    /** Vehículos de un equipo específico (asignación piloto-vehículo, Día 2). */
    public List<Vehiculo> listarPorEquipo(String equipoNombre) {
        return cargarVehiculos("WHERE LOWER(v.equipo_nombre) = LOWER(?)", equipoNombre);
    }

    /** Vehículo asignado a un piloto (según vehiculo_piloto), si existe. Lo usa la simulación (Día 3). */
    public Optional<Vehiculo> buscarPorPiloto(int pilotoId) {
        List<Vehiculo> resultado = cargarVehiculos(
                "WHERE EXISTS (SELECT 1 FROM vehiculo_piloto vp WHERE vp.vehiculo_id = v.id AND vp.piloto_id = ?)",
                pilotoId);
        return resultado.stream().findFirst();
    }

    // ------------------------------------------------------------
    // Persistencia del rendimiento y las asignaciones
    // ------------------------------------------------------------

    /**
     * Reconstruye rendimiento_vehiculo y vehiculo_piloto para el vehículo dado.
     * Se ejecuta dentro de la transacción de guardar/actualizar.
     */
    private void persistirRendimientoYAsignaciones(Vehiculo vehiculo) throws SQLException {
        try (PreparedStatement borrar = conexion.prepareStatement("DELETE FROM rendimiento_vehiculo WHERE vehiculo_id = ?")) {
            borrar.setInt(1, vehiculo.getId());
            borrar.executeUpdate();
        }

        // Si por algún motivo el objeto llegó sin rendimiento (p. ej. una carga
        // incompleta), se calcula con VehiculoFactory, como en la UI.
        if (vehiculo.getRendimiento().isEmpty()) {
            Vehiculo completo = new VehiculoFactory().crearVehiculo(vehiculo.getEquipoNombre(),
                    vehiculo.getModelo(), vehiculo.getMotor(), vehiculo.getVelocidadMaximaKmh(),
                    vehiculo.getAceleracion0100(), vehiculo.getTipoNeumatico());
            for (ModoConduccion modo : ModoConduccion.values()) {
                vehiculo.setRendimientoModo(modo, completo.getRendimiento().get(modo));
            }
        }

        String insertar = "INSERT INTO rendimiento_vehiculo "
                + "(vehiculo_id, modo_conduccion, velocidad_promedio_kmh, "
                + "consumo_seco, consumo_lluvioso, consumo_extremo, "
                + "desgaste_seco, desgaste_lluvioso, desgaste_extremo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(insertar)) {
            for (ModoConduccion modo : ModoConduccion.values()) {
                RendimientoPorModo r = vehiculo.getRendimiento().get(modo);
                stmt.setInt(1, vehiculo.getId());
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
            stmt.executeBatch();
        }

        try (PreparedStatement borrar = conexion.prepareStatement("DELETE FROM vehiculo_piloto WHERE vehiculo_id = ?")) {
            borrar.setInt(1, vehiculo.getId());
            borrar.executeUpdate();
        }

        try (PreparedStatement stmt = conexion.prepareStatement("INSERT INTO vehiculo_piloto (vehiculo_id, piloto_id) VALUES (?, ?)")) {
            for (Integer pilotoId : vehiculo.getPilotoIds()) {
                stmt.setInt(1, vehiculo.getId());
                stmt.setInt(2, pilotoId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // ------------------------------------------------------------
    // Carga de vehículos (datos + rendimiento + asignaciones)
    // ------------------------------------------------------------

    private List<Vehiculo> cargarVehiculos(String where, Object... parametros) {
        String sql = "SELECT v.id, v.equipo_nombre, v.modelo, v.motor, "
                + "v.velocidad_maxima_kmh, v.aceleracion_0100, v.tipo_neumatico FROM vehiculos v "
                + where + " ORDER BY v.id";

        List<Vehiculo> vehiculos = new ArrayList<>();
        Map<Integer, Vehiculo> porId = new HashMap<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            if (parametros != null) {
                for (int i = 0; i < parametros.length; i++) {
                    stmt.setObject(i + 1, parametros[i]);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehiculo v = new Vehiculo(
                            rs.getInt("id"),
                            rs.getString("equipo_nombre"),
                            rs.getString("modelo"),
                            rs.getString("motor"),
                            rs.getDouble("velocidad_maxima_kmh"),
                            rs.getDouble("aceleracion_0100"),
                            TipoNeumatico.valueOf(rs.getString("tipo_neumatico")));
                    porId.put(v.getId(), v);
                    vehiculos.add(v);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar vehículos", e);
        }

        if (vehiculos.isEmpty()) {
            return vehiculos;
        }

        cargarRendimiento(porId);
        cargarAsignaciones(porId);
        return vehiculos;
    }

    private void cargarRendimiento(Map<Integer, Vehiculo> porId) {
        String in = construirIn(porId.keySet().size());
        String sql = "SELECT vehiculo_id, modo_conduccion, velocidad_promedio_kmh, "
                + "consumo_seco, consumo_lluvioso, consumo_extremo, "
                + "desgaste_seco, desgaste_lluvioso, desgaste_extremo "
                + "FROM rendimiento_vehiculo WHERE vehiculo_id IN (" + in + ")";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            asignarClaves(stmt, porId.keySet());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehiculo v = porId.get(rs.getInt("vehiculo_id"));
                    if (v == null) continue;
                    RendimientoPorModo r = new RendimientoPorModo(rs.getDouble("velocidad_promedio_kmh"));
                    r.setConsumo(TipoClima.SECO, rs.getDouble("consumo_seco"));
                    r.setConsumo(TipoClima.LLUVIOSO, rs.getDouble("consumo_lluvioso"));
                    r.setConsumo(TipoClima.EXTREMO, rs.getDouble("consumo_extremo"));
                    r.setDesgaste(TipoClima.SECO, rs.getDouble("desgaste_seco"));
                    r.setDesgaste(TipoClima.LLUVIOSO, rs.getDouble("desgaste_lluvioso"));
                    r.setDesgaste(TipoClima.EXTREMO, rs.getDouble("desgaste_extremo"));
                    v.setRendimientoModo(ModoConduccion.valueOf(rs.getString("modo_conduccion")), r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar el rendimiento de los vehículos", e);
        }

        // Respaldo: si la tabla quedó incompleta, se recalcula con VehiculoFactory
        // para que la simulación y la UI nunca vean un vehículo sin rendimiento.
        VehiculoFactory factory = new VehiculoFactory();
        for (Vehiculo v : porId.values()) {
            if (v.getRendimiento().isEmpty()) {
                Vehiculo completo = factory.crearVehiculo(v.getEquipoNombre(), v.getModelo(),
                        v.getMotor(), v.getVelocidadMaximaKmh(), v.getAceleracion0100(), v.getTipoNeumatico());
                for (ModoConduccion modo : ModoConduccion.values()) {
                    v.setRendimientoModo(modo, completo.getRendimiento().get(modo));
                }
            }
        }
    }

    private void cargarAsignaciones(Map<Integer, Vehiculo> porId) {
        String in = construirIn(porId.keySet().size());
        String sql = "SELECT vehiculo_id, piloto_id FROM vehiculo_piloto WHERE vehiculo_id IN (" + in + ")";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            asignarClaves(stmt, porId.keySet());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehiculo v = porId.get(rs.getInt("vehiculo_id"));
                    if (v != null) {
                        v.asignarPiloto(rs.getInt("piloto_id"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar las asignaciones de vehículos", e);
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

    // ------------------------------------------------------------
    // Transacciones sobre la conexión compartida
    // ------------------------------------------------------------

    private interface OperacionSql {
        void ejecutar() throws SQLException;
    }

    private void enTransaccion(OperacionSql operacion) {
        boolean autoCommitPrevio;
        try {
            autoCommitPrevio = conexion.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el modo auto-commit", e);
        }
        try {
            conexion.setAutoCommit(false);
            operacion.ejecutar();
            conexion.commit();
        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException ignorada) {
                // si el rollback falla, se pierde la transacción; nada más se puede hacer
            }
            throw new RuntimeException("Error en la transacción del vehículo; se revirtieron los cambios", e);
        } finally {
            try {
                conexion.setAutoCommit(autoCommitPrevio);
            } catch (SQLException ignorada) {
                // no se debe enmascarar el error original
            }
        }
    }
}