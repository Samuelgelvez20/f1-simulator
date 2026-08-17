package com.f1simulator.ui;

import com.f1simulator.model.Circuito;
import com.f1simulator.model.TipoClima;
import com.f1simulator.repository.CircuitoRepository;

import javax.swing.JOptionPane;
import java.util.Optional;

public class CircuitoUI {

    private final CircuitoRepository circuitoRepository;

    public CircuitoUI(CircuitoRepository circuitoRepository) {
        this.circuitoRepository = circuitoRepository;
    }

    public void mostrarMenu() {
        boolean salir = false;
        while (!salir) {
            String[] opciones = { "Agregar Circuito", "Editar Circuito", "Eliminar Circuito", "Buscar Circuito",
                    "Listar Todos", "Volver" };
            int seleccion = JOptionPane.showOptionDialog(null, "Menú de Circuitos", "Gestión de Circuitos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

            switch (seleccion) {
                case 0:
                    agregarCircuito();
                    break;
                case 1:
                    editarCircuito();
                    break;
                case 2:
                    eliminarCircuito();
                    break;
                case 3:
                    buscarCircuito();
                    break;
                case 4:
                    listarCircuitos();
                    break;
                case 5:
                case JOptionPane.CLOSED_OPTION:
                    salir = true;
                    break;
            }
        }
    }

    private void agregarCircuito() {
        try {
            String id = JOptionPane.showInputDialog("Ingrese el ID del circuito:");
            if (id == null || id.trim().isEmpty())
                return;

            if (circuitoRepository.buscarPorId(id).isPresent()) {
                JOptionPane.showMessageDialog(null, "Ya existe un circuito con ese ID.");
                return;
            }

            String nombre = JOptionPane.showInputDialog("Ingrese el nombre del circuito:");
            if (nombre == null || nombre.trim().isEmpty())
                return;

            String pais = JOptionPane.showInputDialog("Ingrese el país del circuito:");
            if (pais == null || pais.trim().isEmpty())
                return;

            String longitudStr = JOptionPane.showInputDialog("Ingrese la longitud en km:");
            if (longitudStr == null)
                return;
            double longitudKm = Double.parseDouble(longitudStr.replace(",", "."));

            String vueltasStr = JOptionPane.showInputDialog("Ingrese el número de vueltas:");
            if (vueltasStr == null)
                return;
            int vueltas = Integer.parseInt(vueltasStr);

            String descripcion = JOptionPane.showInputDialog("Ingrese una descripción:");
            if (descripcion == null)
                return;

            TipoClima[] climas = TipoClima.values();
            TipoClima climaPromedio = (TipoClima) JOptionPane.showInputDialog(null, "Seleccione el clima promedio:",
                    "Clima", JOptionPane.QUESTION_MESSAGE, null, climas, climas[0]);

            if (climaPromedio == null)
                return;

            Circuito nuevoCircuito = new Circuito(id, nombre, pais, longitudKm, vueltas, descripcion, climaPromedio);
            circuitoRepository.guardar(nuevoCircuito);
            JOptionPane.showMessageDialog(null, "Circuito agregado exitosamente.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: Ingrese un valor numérico válido para longitud o vueltas.",
                    "Error de entrada", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrió un error inesperado al agregar.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarCircuito() {
        String id = JOptionPane.showInputDialog("Ingrese el ID del circuito a editar:");
        if (id == null || id.trim().isEmpty())
            return;

        Optional<Circuito> circuitoOpt = circuitoRepository.buscarPorId(id);
        if (circuitoOpt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontró un circuito con ese ID.", "No encontrado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Circuito circuito = circuitoOpt.get();

        try {
            String nombre = JOptionPane.showInputDialog("Nombre actual: " + circuito.getNombre() + "\nNuevo nombre:",
                    circuito.getNombre());
            if (nombre != null && !nombre.trim().isEmpty())
                circuito.setNombre(nombre);

            String pais = JOptionPane.showInputDialog("País actual: " + circuito.getPais() + "\nNuevo país:",
                    circuito.getPais());
            if (pais != null && !pais.trim().isEmpty())
                circuito.setPais(pais);

            String longitudStr = JOptionPane.showInputDialog(
                    "Longitud actual: " + circuito.getLongitudKm() + "\nNueva longitud (km):",
                    String.valueOf(circuito.getLongitudKm()));
            if (longitudStr != null && !longitudStr.trim().isEmpty())
                circuito.setLongitudKm(Double.parseDouble(longitudStr.replace(",", ".")));

            String vueltasStr = JOptionPane.showInputDialog(
                    "Vueltas actuales: " + circuito.getVueltas() + "\nNuevas vueltas:",
                    String.valueOf(circuito.getVueltas()));
            if (vueltasStr != null && !vueltasStr.trim().isEmpty())
                circuito.setVueltas(Integer.parseInt(vueltasStr));

            String descripcion = JOptionPane.showInputDialog(
                    "Descripción actual: " + circuito.getDescripcion() + "\nNueva descripción:",
                    circuito.getDescripcion());
            if (descripcion != null)
                circuito.setDescripcion(descripcion);

            TipoClima[] climas = TipoClima.values();
            TipoClima clima = (TipoClima) JOptionPane.showInputDialog(null,
                    "Clima actual: " + circuito.getClimaPromedio(),
                    "Editar Clima", JOptionPane.QUESTION_MESSAGE, null, climas, circuito.getClimaPromedio());
            if (clima != null)
                circuito.setClimaPromedio(clima);

            circuitoRepository.actualizar(id, circuito);
            JOptionPane.showMessageDialog(null, "Circuito actualizado exitosamente.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: Ingrese un valor numérico válido.", "Error de entrada",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCircuito() {
        String id = JOptionPane.showInputDialog("Ingrese el ID del circuito a eliminar:");
        if (id == null || id.trim().isEmpty())
            return;

        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Está seguro que desea eliminar el circuito " + id + "?", "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = circuitoRepository.eliminar(id);
            if (eliminado) {
                JOptionPane.showMessageDialog(null, "Circuito eliminado.");
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el circuito.");
            }
        }
    }

    private void buscarCircuito() {
        String id = JOptionPane
                .showInputDialog("Ingrese el ID del circuito a buscar (o cancele para buscar por nombre/país):");

        if (id != null && !id.trim().isEmpty()) {
            Optional<Circuito> c = circuitoRepository.buscarPorId(id);
            if (c.isPresent()) {
                JOptionPane.showMessageDialog(null, c.get().toString());
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró un circuito con el ID " + id);
            }
        } else {
            // Busqueda por nombre o pais como piden las reglas (opcionalmente)
            String query = JOptionPane.showInputDialog("Ingrese el nombre o país del circuito a buscar:");
            if (query == null || query.trim().isEmpty())
                return;

            StringBuilder resultados = new StringBuilder();
            for (Circuito c : circuitoRepository.listarTodos()) {
                if (c.getNombre().toLowerCase().contains(query.toLowerCase())
                        || c.getPais().toLowerCase().contains(query.toLowerCase())) {
                    resultados.append(c.toString()).append("\n");
                }
            }

            if (resultados.length() > 0) {
                JOptionPane.showMessageDialog(null, resultados.toString());
            } else {
                JOptionPane.showMessageDialog(null, "No se encontraron coincidencias.");
            }
        }
    }

    private void listarCircuitos() {
        StringBuilder sb = new StringBuilder("Lista de Circuitos:\n\n");
        for (Circuito c : circuitoRepository.listarTodos()) {
            sb.append(c.toString()).append("\n");
        }
        if (circuitoRepository.listarTodos().isEmpty()) {
            sb.append("No hay circuitos registrados.");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }
}
