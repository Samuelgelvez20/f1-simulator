package com.f1simulator.ui;

import com.f1simulator.factory.VehiculoFactory;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.Piloto;
import com.f1simulator.model.TipoNeumatico;
import com.f1simulator.model.Vehiculo;
import com.f1simulator.repository.EquipoRepository;
import com.f1simulator.repository.PilotoRepository;
import com.f1simulator.repository.VehiculoRepository;
import com.f1simulator.model.Equipo;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

/**
 * Menú de gestión de Vehículos con JOptionPane: agregar (vía Factory),
 * editar, eliminar, buscar, ver especificaciones y comparar.
 */
public class VehiculoUI {

    private final VehiculoRepository vehiculoRepo;
    private final EquipoRepository equipoRepo;
    private final PilotoRepository pilotoRepo;
    private final VehiculoFactory factory = new VehiculoFactory();

    public VehiculoUI(VehiculoRepository vehiculoRepo, EquipoRepository equipoRepo, PilotoRepository pilotoRepo) {
        this.vehiculoRepo = vehiculoRepo;
        this.equipoRepo = equipoRepo;
        this.pilotoRepo = pilotoRepo;
    }

    public void mostrarMenu() {
        String[] opciones = {
                "Agregar vehículo", "Editar vehículo", "Eliminar vehículo",
                "Buscar vehículo", "Ver especificaciones", "Comparar vehículos",
                "Asignar piloto a vehículo", "Listar todos", "Volver"
        };
        int seleccion;
        do {
            seleccion = JOptionPane.showOptionDialog(null, "Gestión de Vehículos",
                    "Vehículos", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, opciones, opciones[0]);

            switch (seleccion) {
                case 0 -> agregarVehiculo();
                case 1 -> editarVehiculo();
                case 2 -> eliminarVehiculo();
                case 3 -> buscarVehiculo();
                case 4 -> verEspecificaciones();
                case 5 -> compararVehiculos();
                case 6 -> asignarPilotoAVehiculo();
                case 7 -> listarTodos();
                default -> { /* Volver o cerrar */ }
            }
        } while (seleccion != 8 && seleccion != -1);
    }

    /**
     * Historia de usuario "Asignación de pilotos a vehículos": el piloto solo
     * puede asignarse a un vehículo de su propio equipo. Necesario también
     * para la simulación (Día 3), que resuelve qué vehículo maneja cada piloto.
     */
    private void asignarPilotoAVehiculo() {
        if (pilotoRepo.listarTodos().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay pilotos registrados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] nombresPilotos = pilotoRepo.listarTodos().stream()
                .map(p -> p.getId() + " - " + p.getNombre() + " (" + (p.getEquipo() != null ? p.getEquipo().getNombre() : "Sin equipo") + ")")
                .toArray(String[]::new);
        String seleccionPiloto = (String) JOptionPane.showInputDialog(null, "Selecciona el piloto:",
                "Piloto", JOptionPane.QUESTION_MESSAGE, null, nombresPilotos, nombresPilotos[0]);
        if (seleccionPiloto == null) return;

        int pilotoId = Integer.parseInt(seleccionPiloto.split(" - ")[0]);
        Piloto piloto = pilotoRepo.buscarPorId(pilotoId).orElse(null);
        if (piloto == null || piloto.getEquipo() == null) {
            JOptionPane.showMessageDialog(null, "El piloto no tiene un equipo asignado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Vehiculo> vehiculosDelEquipo = vehiculoRepo.listarPorEquipo(piloto.getEquipo().getNombre());
        if (vehiculosDelEquipo.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "El equipo \"" + piloto.getEquipo().getNombre() + "\" no tiene vehículos registrados.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] nombresVehiculos = vehiculosDelEquipo.stream()
                .map(v -> v.getId() + " - " + v.getModelo())
                .toArray(String[]::new);
        String seleccionVehiculo = (String) JOptionPane.showInputDialog(null,
                "Selecciona el vehículo para " + piloto.getNombre() + ":",
                "Vehículo", JOptionPane.QUESTION_MESSAGE, null, nombresVehiculos, nombresVehiculos[0]);
        if (seleccionVehiculo == null) return;

        int vehiculoId = Integer.parseInt(seleccionVehiculo.split(" - ")[0]);
        Vehiculo vehiculo = vehiculoRepo.buscarPorId(vehiculoId).orElse(null);
        if (vehiculo == null) return;

        vehiculo.asignarPiloto(pilotoId);
        vehiculoRepo.actualizar(vehiculo.getId(), vehiculo);
        JOptionPane.showMessageDialog(null,
                piloto.getNombre() + " fue asignado al vehículo " + vehiculo.getModelo() + ".",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void agregarVehiculo() {
        try {
            String equipoNombre = pedirEquipoExistente();
            if (equipoNombre == null) return;

            String modelo = pedirTexto("Modelo del vehículo:");
            if (modelo == null) return;
            String motor = pedirTexto("Motor:");
            if (motor == null) return;

            double velocidadMax = Double.parseDouble(pedirTexto("Velocidad máxima (km/h):"));
            if (velocidadMax <= 0) throw new IllegalArgumentException("La velocidad máxima debe ser mayor a 0");

            double aceleracion = Double.parseDouble(pedirTexto("Aceleración 0-100 (segundos):"));
            if (aceleracion <= 0) throw new IllegalArgumentException("La aceleración debe ser mayor a 0");

            TipoNeumatico neumatico = pedirNeumatico();
            if (neumatico == null) return;

            Vehiculo vehiculo = factory.crearVehiculo(equipoNombre, modelo, motor, velocidadMax, aceleracion, neumatico);
            vehiculoRepo.guardar(vehiculo);

            JOptionPane.showMessageDialog(null,
                    "Vehículo agregado. El rendimiento por modo de conducción se calculó automáticamente.\n\n" + vehiculo,
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Velocidad y aceleración deben ser numéricas.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarVehiculo() {
        Integer id = pedirId();
        if (id == null) return;
        Optional<Vehiculo> existente = vehiculoRepo.buscarPorId(id);
        if (existente.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No existe un vehículo con ese ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Vehiculo actual = existente.get();
            String modelo = pedirTexto("Modelo (" + actual.getModelo() + "):");
            if (modelo == null) return;
            double velocidadMax = Double.parseDouble(pedirTexto("Velocidad máxima (" + actual.getVelocidadMaximaKmh() + "):"));
            double aceleracion = Double.parseDouble(pedirTexto("Aceleración (" + actual.getAceleracion0100() + "):"));
            TipoNeumatico neumatico = pedirNeumatico();
            if (neumatico == null) return;

            Vehiculo actualizado = factory.crearVehiculo(actual.getEquipoNombre(), modelo, actual.getMotor(),
                    velocidadMax, aceleracion, neumatico);
            vehiculoRepo.actualizar(id, actualizado);

            JOptionPane.showMessageDialog(null, "Vehículo actualizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Velocidad y aceleración deben ser numéricas.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarVehiculo() {
        Integer id = pedirId();
        if (id == null) return;
        Optional<Vehiculo> existente = vehiculoRepo.buscarPorId(id);
        if (existente.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No existe un vehículo con ese ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Eliminar el vehículo \"" + existente.get().getModelo() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            vehiculoRepo.eliminar(id);
            JOptionPane.showMessageDialog(null, "Vehículo eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void buscarVehiculo() {
        String texto = pedirTexto("Buscar por modelo, motor o equipo:");
        if (texto == null) return;
        mostrarListado(vehiculoRepo.buscarPorCaracteristica(texto), "Resultados de búsqueda");
    }

    private void verEspecificaciones() {
        Integer id = pedirId();
        if (id == null) return;
        Optional<Vehiculo> vehiculo = vehiculoRepo.buscarPorId(id);
        if (vehiculo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No existe un vehículo con ese ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(null, formatoEspecificaciones(vehiculo.get()), "Especificaciones", JOptionPane.PLAIN_MESSAGE);
    }

    private void compararVehiculos() {
        Integer id1 = pedirId();
        if (id1 == null) return;
        Integer id2 = pedirId();
        if (id2 == null) return;

        Optional<Vehiculo> v1 = vehiculoRepo.buscarPorId(id1);
        Optional<Vehiculo> v2 = vehiculoRepo.buscarPorId(id2);
        if (v1.isEmpty() || v2.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Uno de los vehículos no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String comparacion = "=== " + v1.get().getModelo() + " ===\n" + formatoEspecificaciones(v1.get())
                + "\n\n=== " + v2.get().getModelo() + " ===\n" + formatoEspecificaciones(v2.get());

        JOptionPane.showMessageDialog(null, comparacion, "Comparación de vehículos", JOptionPane.PLAIN_MESSAGE);
    }

    private void listarTodos() {
        mostrarListado(vehiculoRepo.listarTodos(), "Vehículos registrados");
    }

    private String formatoEspecificaciones(Vehiculo v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v).append("\n\nRendimiento por modo de conducción:\n");
        for (ModoConduccion modo : ModoConduccion.values()) {
            sb.append(" - ").append(modo).append(": ").append(v.getRendimiento().get(modo)).append("\n");
        }
        return sb.toString();
    }

    private void mostrarListado(List<Vehiculo> lista, String titulo) {
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay vehículos para mostrar.", titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnas = { "ID", "Modelo", "Motor", "Equipo", "Vel. máx (km/h)", "Acel. 0-100 (s)", "Pilotos" };
        DefaultTableModel model = new DefaultTableModel(columnas, 0);

        for (Vehiculo v : lista) {
            String pilotos = "Ninguno";
            if (v.getPilotoIds() != null && !v.getPilotoIds().isEmpty()) {
                pilotos = v.getPilotoIds().stream()
                        .map(id -> pilotoRepo.buscarPorId(id).map(Piloto::getNombre).orElse("ID " + id))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Ninguno");
            }
            model.addRow(new Object[] { v.getId(), v.getModelo(), v.getMotor(), v.getEquipoNombre(),
                    v.getVelocidadMaximaKmh(), v.getAceleracion0100(), pilotos });
        }

        JTable table = new JTable(model);
        table.setDefaultEditor(Object.class, null);
        JScrollPane scrollPane = new JScrollPane(table);
        JOptionPane.showMessageDialog(null, scrollPane, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    // ---- Helpers de entrada ----

    private String pedirTexto(String mensaje) {
        String valor = JOptionPane.showInputDialog(null, mensaje);
        if (valor == null) return null;
        if (valor.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Este campo no puede estar vacío.", "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return pedirTexto(mensaje);
        }
        return valor.trim();
    }

    private Integer pedirId() {
        String valor = JOptionPane.showInputDialog(null, "ID del vehículo:");
        if (valor == null) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El ID debe ser un número.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            return pedirId();
        }
    }

    private TipoNeumatico pedirNeumatico() {
        TipoNeumatico[] opciones = TipoNeumatico.values();
        String[] nombres = new String[opciones.length];
        for (int i = 0; i < opciones.length; i++) nombres[i] = opciones[i].name();

        String seleccion = (String) JOptionPane.showInputDialog(null, "Tipo de neumático equipado:",
                "Neumático", JOptionPane.QUESTION_MESSAGE, null, nombres, nombres[0]);
        if (seleccion == null) return null;
        return TipoNeumatico.valueOf(seleccion);
    }

    private String pedirEquipoExistente() {
    if (equipoRepo.listarTodos().isEmpty()) {
        JOptionPane.showMessageDialog(null, "No hay equipos registrados. Regístralo primero en el módulo de Equipos.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return null;
    }
    String[] nombresEquipos = equipoRepo.listarTodos().stream()
            .map(Equipo::getNombre)
            .toArray(String[]::new);
    return (String) JOptionPane.showInputDialog(null, "Seleccione el equipo:",
            "Equipo", JOptionPane.QUESTION_MESSAGE, null, nombresEquipos, nombresEquipos[0]);
}
}