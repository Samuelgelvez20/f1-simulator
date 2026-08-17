package com.f1simulator.ui;

import com.f1simulator.factory.VehiculoFactory;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.TipoNeumatico;
import com.f1simulator.model.Vehiculo;
import com.f1simulator.repository.VehiculoRepository;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Optional;

/**
 * Menú de gestión de Vehículos con JOptionPane: agregar (vía Factory),
 * editar, eliminar, buscar, ver especificaciones y comparar.
 */
public class VehiculoUI {

    private final VehiculoRepository vehiculoRepo;
    private final VehiculoFactory factory = new VehiculoFactory();

    public VehiculoUI(VehiculoRepository vehiculoRepo) {
        this.vehiculoRepo = vehiculoRepo;
    }

    public void mostrarMenu() {
        String[] opciones = {
                "Agregar vehículo", "Editar vehículo", "Eliminar vehículo",
                "Buscar vehículo", "Ver especificaciones", "Comparar vehículos",
                "Listar todos", "Volver"
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
                case 6 -> listarTodos();
                default -> { /* Volver o cerrar */ }
            }
        } while (seleccion != 7 && seleccion != -1);
    }

    private void agregarVehiculo() {
        try {
            String equipoNombre = pedirTexto("Nombre del equipo (debe existir):");
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
        StringBuilder sb = new StringBuilder();
        lista.forEach(v -> sb.append(v).append("\n"));
        JOptionPane.showMessageDialog(null, sb.toString(), titulo, JOptionPane.PLAIN_MESSAGE);
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
}
