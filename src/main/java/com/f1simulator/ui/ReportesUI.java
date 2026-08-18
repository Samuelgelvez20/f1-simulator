package com.f1simulator.ui;

import com.f1simulator.model.ConfiguracionVehiculo;
import com.f1simulator.model.ResultadoClasificacion;
import com.f1simulator.persistence.ConfiguracionSqlRepository;
import com.f1simulator.persistence.ResultadoClasificacionSqlRepository;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ReportesUI {

    private final ResultadoClasificacionSqlRepository resultadosRepo;
    private final ConfiguracionSqlRepository configuracionRepo;

    public ReportesUI(ResultadoClasificacionSqlRepository resultadosRepo,
            ConfiguracionSqlRepository configuracionRepo) {
        this.resultadosRepo = resultadosRepo;
        this.configuracionRepo = configuracionRepo;
    }

    public void mostrarMenu() {
        boolean salir = false;
        while (!salir) {
            String[] opciones = { "Comparación de Tiempos (Por Circuito)", "Historial de Configuraciones", "Volver" };
            int seleccion = JOptionPane.showOptionDialog(null, "Menú de Reportes F1", "Gestión de Reportes",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

            switch (seleccion) {
                case 0:
                    mostrarComparacionTiempos();
                    break;
                case 1:
                    mostrarHistorialConfiguraciones();
                    break;
                case 2:
                case JOptionPane.CLOSED_OPTION:
                    salir = true;
                    break;
            }
        }
    }

    private void mostrarComparacionTiempos() {
        String circuitoNombre = JOptionPane.showInputDialog("Ingrese el nombre del circuito a buscar:");
        if (circuitoNombre == null || circuitoNombre.trim().isEmpty()) {
            return;
        }

        List<ResultadoClasificacion> resultados;
        try {
            resultados = resultadosRepo.obtenerPorCircuito(circuitoNombre);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al consultar la base de datos: " + e.getMessage(), "Error SQL",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay resultados registrados para el circuito indicado.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnas = { "Posición", "Piloto", "Vehículo", "Tiempo", "Clima" };
        DefaultTableModel model = new DefaultTableModel(columnas, 0);

        for (ResultadoClasificacion r : resultados) {
            // Posicion, Piloto, Vehículo, Tiempo, Clima
            model.addRow(new Object[] {
                    r.getPosicion(),
                    r.getPilotoNombre(),
                    r.getVehiculoModelo() != null ? r.getVehiculoModelo() : "N/A",
                    String.format("%.3f s", r.getTiempoVueltaSegundos()),
                    r.getClimaSesion()
            });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JOptionPane.showMessageDialog(null, scrollPane, "Comparación de Tiempos en " + circuitoNombre,
                JOptionPane.PLAIN_MESSAGE);
    }

    private void mostrarHistorialConfiguraciones() {
        List<ConfiguracionVehiculo> configuraciones;
        try {
            configuraciones = configuracionRepo.obtenerHistorial();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al consultar la base de datos: " + e.getMessage(), "Error SQL",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (configuraciones.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay configuraciones en el historial.", "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnas = { "ID", "Vehículo ID", "Piloto ID", "Modo Conducción", "Aero", "Presión Neumáticos",
                "Combustible", "Fecha" };
        DefaultTableModel model = new DefaultTableModel(columnas, 0);

        for (ConfiguracionVehiculo c : configuraciones) {
            model.addRow(new Object[] {
                    c.getId(),
                    c.getVehiculoId(),
                    c.getPilotoId(),
                    c.getModoConduccion(),
                    c.getCargaAerodinamica(),
                    c.getPresionNeumaticos(),
                    c.getEstrategiaCombustible(),
                    c.getFechaCreacion()
            });
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JOptionPane.showMessageDialog(null, scrollPane, "Historial Global de Configuraciones",
                JOptionPane.PLAIN_MESSAGE);
    }
}
