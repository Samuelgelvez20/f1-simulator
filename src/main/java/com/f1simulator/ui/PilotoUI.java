package com.f1simulator.ui;

import com.f1simulator.model.Equipo;
import com.f1simulator.model.Piloto;
import com.f1simulator.repository.EquipoRepository;
import com.f1simulator.repository.PilotoRepository;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.Optional;

public class PilotoUI {

    private final PilotoRepository pilotoRepository;
    private final EquipoRepository equipoRepository;

    public PilotoUI(PilotoRepository pilotoRepository, EquipoRepository equipoRepository) {
        this.pilotoRepository = pilotoRepository;
        this.equipoRepository = equipoRepository;
    }

    public void mostrarMenu() {
        boolean salir = false;
        while (!salir) {
            String[] opciones = { "Registrar Piloto", "Editar Piloto", "Eliminar Piloto", "Listar Todos",
                    "Listar (JTable)", "Volver" };
            int seleccion = JOptionPane.showOptionDialog(null, "Menú de Pilotos", "Gestión de Pilotos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

            switch (seleccion) {
                case 0:
                    registrarPiloto();
                    break;
                case 1:
                    editarPiloto();
                    break;
                case 2:
                    eliminarPiloto();
                    break;
                case 3:
                    listarPilotos();
                    break;
                case 4:
                    mostrarListaPilotos();
                    break;
                case 5:
                case JOptionPane.CLOSED_OPTION:
                    salir = true;
                    break;
            }
        }
    }

    private void registrarPiloto() {
        try {
            String idStr = JOptionPane.showInputDialog("Ingrese el ID (número) del piloto:");
            if (idStr == null || idStr.trim().isEmpty())
                return;
            int id = Integer.parseInt(idStr);

            if (pilotoRepository.buscarPorId(id).isPresent()) {
                JOptionPane.showMessageDialog(null, "Ya existe un piloto con ese ID.");
                return;
            }

            String nombre = JOptionPane.showInputDialog("Ingrese el nombre del piloto:");
            if (nombre == null || nombre.trim().isEmpty())
                return;

            String[] roles = { "Líder", "Escudero" };
            String rol = (String) JOptionPane.showInputDialog(null, "Seleccione el rol del piloto:",
                    "Rol", JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
            if (rol == null)
                return;

            if (equipoRepository.listarTodos().isEmpty()) {
                JOptionPane.showMessageDialog(null, "No hay equipos registrados. Registre un equipo primero.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Equipo[] equiposDisp = equipoRepository.listarTodos().toArray(new Equipo[0]);
            String[] nombresEquipos = new String[equiposDisp.length];
            for (int i = 0; i < equiposDisp.length; i++) {
                nombresEquipos[i] = equiposDisp[i].getNombre();
            }

            String nombreEquipoSelec = (String) JOptionPane.showInputDialog(null, "Seleccione el equipo:",
                    "Equipo", JOptionPane.QUESTION_MESSAGE, null, nombresEquipos, nombresEquipos[0]);

            if (nombreEquipoSelec == null)
                return;

            Equipo equipo = equipoRepository.buscarPorId(nombreEquipoSelec).orElse(null);

            Piloto nuevoPiloto = new Piloto(id, nombre, equipo, rol);
            pilotoRepository.guardar(nuevoPiloto);

            if (equipo != null) {
                equipo.agregarPiloto(nuevoPiloto);
            }

            JOptionPane.showMessageDialog(null, "Piloto registrado exitosamente.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: El ID debe ser numérico.", "Error de entrada",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrió un error inesperado al registrar.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarPiloto() {
        String idStr = JOptionPane.showInputDialog("Ingrese el ID numérico del piloto a editar:");
        if (idStr == null || idStr.trim().isEmpty())
            return;

        try {
            int id = Integer.parseInt(idStr);
            Optional<Piloto> pilotoOpt = pilotoRepository.buscarPorId(id);
            if (pilotoOpt.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No se encontró el piloto.", "No encontrado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Piloto piloto = pilotoOpt.get();

            String nombre = JOptionPane.showInputDialog("Nombre actual: " + piloto.getNombre() + "\nNuevo nombre:",
                    piloto.getNombre());
            if (nombre != null && !nombre.trim().isEmpty())
                piloto.setNombre(nombre);

            String[] roles = { "Líder", "Escudero" };
            String rol = (String) JOptionPane.showInputDialog(null, "Rol actual: " + piloto.getRol(),
                    "Editar Rol", JOptionPane.QUESTION_MESSAGE, null, roles, piloto.getRol());
            if (rol != null)
                piloto.setRol(rol);

            if (!equipoRepository.listarTodos().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(null, "¿Desea cambiar el equipo del piloto?",
                        "Cambiar Equipo", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Equipo[] equiposDisp = equipoRepository.listarTodos().toArray(new Equipo[0]);
                    String[] nombresEquipos = new String[equiposDisp.length];
                    for (int i = 0; i < equiposDisp.length; i++) {
                        nombresEquipos[i] = equiposDisp[i].getNombre();
                    }
                    String nombreEquipoSelec = (String) JOptionPane.showInputDialog(null, "Seleccione el nuevo equipo:",
                            "Equipo", JOptionPane.QUESTION_MESSAGE, null, nombresEquipos, nombresEquipos[0]);

                    if (nombreEquipoSelec != null) {
                        Equipo equipoAnterior = piloto.getEquipo();
                        if (equipoAnterior != null) {
                            equipoAnterior.removerPiloto(piloto);
                        }
                        Equipo equipoNuevo = equipoRepository.buscarPorId(nombreEquipoSelec).orElse(null);
                        piloto.setEquipo(equipoNuevo);
                        if (equipoNuevo != null) {
                            equipoNuevo.agregarPiloto(piloto);
                        }
                    }
                }
            }

            pilotoRepository.actualizar(id, piloto);
            JOptionPane.showMessageDialog(null, "Piloto actualizado exitosamente.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: El ID debe ser numérico.", "Error de entrada",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPiloto() {
        String idStr = JOptionPane.showInputDialog("Ingrese el ID del piloto a eliminar:");
        if (idStr == null || idStr.trim().isEmpty())
            return;

        try {
            int id = Integer.parseInt(idStr);
            int confirmacion = JOptionPane.showConfirmDialog(null,
                    "¿Está seguro que desea eliminar el piloto con ID " + id + "?", "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean eliminado = pilotoRepository.eliminar(id);
                if (eliminado) {
                    JOptionPane.showMessageDialog(null, "Piloto eliminado.");
                } else {
                    JOptionPane.showMessageDialog(null, "No se encontró el piloto.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: El ID debe ser numérico.", "Error de entrada",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listarPilotos() {
        StringBuilder sb = new StringBuilder("Lista de Pilotos:\n\n");
        for (Piloto p : pilotoRepository.listarTodos()) {
            sb.append(p.toString()).append("\n");
        }
        if (pilotoRepository.listarTodos().isEmpty()) {
            sb.append("No hay pilotos registrados.");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private void mostrarListaPilotos() {
        if (pilotoRepository.listarTodos().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay pilotos registrados.", "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnas = { "ID", "Nombre", "Equipo", "Rol" };
        DefaultTableModel model = new DefaultTableModel(columnas, 0);

        for (Piloto p : pilotoRepository.listarTodos()) {
            String nombreEquipo = p.getEquipo() != null ? p.getEquipo().getNombre() : "Sin equipo";
            model.addRow(new Object[] { p.getId(), p.getNombre(), nombreEquipo, p.getRol() });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JOptionPane.showMessageDialog(null, scrollPane, "Lista de Pilotos", JOptionPane.PLAIN_MESSAGE);
    }
}