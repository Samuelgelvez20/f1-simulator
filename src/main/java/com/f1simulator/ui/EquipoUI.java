package com.f1simulator.ui;

import com.f1simulator.model.Equipo;
import com.f1simulator.repository.EquipoRepository;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.Optional;
import java.util.stream.Collectors;
import com.f1simulator.model.Piloto;

public class EquipoUI {

    private final EquipoRepository equipoRepository;

    public EquipoUI(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    public void mostrarMenu() {
        boolean salir = false;
        while (!salir) {
            String[] opciones = { "Registrar Equipo", "Editar Equipo", "Eliminar Equipo", "Listar Todos",
                    "Listar (JTable)", "Volver" };
            int seleccion = JOptionPane.showOptionDialog(null, "Menú de Equipos", "Gestión de Equipos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

            switch (seleccion) {
                case 0:
                    registrarEquipo();
                    break;
                case 1:
                    editarEquipo();
                    break;
                case 2:
                    eliminarEquipo();
                    break;
                case 3:
                    listarEquipos();
                    break;
                case 4:
                    mostrarListaEquipos();
                    break;
                case 5:
                case JOptionPane.CLOSED_OPTION:
                    salir = true;
                    break;
            }
        }
    }

    private void registrarEquipo() {
        try {
            String nombre = JOptionPane.showInputDialog("Ingrese el nombre del equipo (ID):");
            if (nombre == null || nombre.trim().isEmpty())
                return;

            if (equipoRepository.buscarPorId(nombre).isPresent()) {
                JOptionPane.showMessageDialog(null, "Ya existe un equipo con ese nombre.");
                return;
            }

            String pais = JOptionPane.showInputDialog("Ingrese el país del equipo:");
            if (pais == null || pais.trim().isEmpty())
                return;

            String motor = JOptionPane.showInputDialog("Ingrese el proveedor de motor:");
            if (motor == null || motor.trim().isEmpty())
                return;

            Equipo nuevoEquipo = new Equipo(nombre, pais, motor);
            equipoRepository.guardar(nuevoEquipo);
            JOptionPane.showMessageDialog(null, "Equipo registrado exitosamente.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrió un error inesperado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarEquipo() {
        String nombre = JOptionPane.showInputDialog("Ingrese el nombre del equipo a editar:");
        if (nombre == null || nombre.trim().isEmpty())
            return;

        Optional<Equipo> equipoOpt = equipoRepository.buscarPorId(nombre);
        if (equipoOpt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontró el equipo.", "No encontrado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Equipo equipo = equipoOpt.get();

        try {
            String pais = JOptionPane.showInputDialog("País actual: " + equipo.getPais() + "\nNuevo país:",
                    equipo.getPais());
            if (pais != null && !pais.trim().isEmpty())
                equipo.setPais(pais);

            String motor = JOptionPane.showInputDialog("Motor actual: " + equipo.getMotor() + "\nNuevo motor:",
                    equipo.getMotor());
            if (motor != null && !motor.trim().isEmpty())
                equipo.setMotor(motor);

            equipoRepository.actualizar(nombre, equipo);
            JOptionPane.showMessageDialog(null, "Equipo actualizado exitosamente.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrió un error inesperado al editar.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarEquipo() {
        String nombre = JOptionPane.showInputDialog("Ingrese el nombre del equipo a eliminar:");
        if (nombre == null || nombre.trim().isEmpty())
            return;

        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Está seguro que desea eliminar el equipo " + nombre + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = equipoRepository.eliminar(nombre);
            if (eliminado) {
                JOptionPane.showMessageDialog(null, "Equipo eliminado.");
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el equipo.");
            }
        }
    }

    private void listarEquipos() {
        StringBuilder sb = new StringBuilder("Lista de Equipos:\n\n");
        for (Equipo e : equipoRepository.listarTodos()) {
            sb.append(e.toString()).append("\n");
        }
        if (equipoRepository.listarTodos().isEmpty()) {
            sb.append("No hay equipos registrados.");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private void mostrarListaEquipos() {
        if (equipoRepository.listarTodos().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay equipos registrados.");
            return;
        }

        String[] columnas = { "Nombre", "País", "Motor", "Pilotos Asociados" };
        DefaultTableModel model = new DefaultTableModel(columnas, 0);

        for (Equipo e : equipoRepository.listarTodos()) {
            String pilotosStr = "Ninguno";
            if (e.getPilotos() != null && !e.getPilotos().isEmpty()) {
                pilotosStr = e.getPilotos().stream().map(Piloto::getNombre).collect(Collectors.joining(", "));
            }
            model.addRow(new Object[] { e.getNombre(), e.getPais(), e.getMotor(), pilotosStr });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        JOptionPane.showMessageDialog(null, scrollPane, "Lista de Equipos", JOptionPane.PLAIN_MESSAGE);
    }
}
