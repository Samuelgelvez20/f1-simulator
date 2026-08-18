package com.f1simulator.ui;

import com.f1simulator.model.Circuito;
import com.f1simulator.model.ConfiguracionVehiculo;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.Piloto;
import com.f1simulator.model.ResultadoClasificacion;
import com.f1simulator.model.TipoClima;
import com.f1simulator.model.Vehiculo;
import com.f1simulator.persistence.ConfiguracionSqlRepository;
import com.f1simulator.persistence.ResultadoClasificacionSqlRepository;
import com.f1simulator.repository.CircuitoRepository;
import com.f1simulator.repository.PilotoRepository;
import com.f1simulator.repository.VehiculoRepository;
import com.f1simulator.service.ParticipanteSimulacion;
import com.f1simulator.service.SimulacionService;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Menú de Simulación de Clasificación (Día 3): selección de circuito, clima
 * aleatorio, cálculo concurrente de tiempos de vuelta (un hilo por piloto) y
 * visualización de la clasificación final con JTable + JScrollPane.
 */
public class SimulacionUI {

    private final CircuitoRepository circuitoRepo;
    private final PilotoRepository pilotoRepo;
    private final VehiculoRepository vehiculoRepo;
    private final ConfiguracionSqlRepository configRepo;
    private final SimulacionService simulacionService;

    public SimulacionUI(CircuitoRepository circuitoRepo, PilotoRepository pilotoRepo,
                         VehiculoRepository vehiculoRepo, ConfiguracionSqlRepository configRepo,
                         ResultadoClasificacionSqlRepository resultadoRepo) {
        this.circuitoRepo = circuitoRepo;
        this.pilotoRepo = pilotoRepo;
        this.vehiculoRepo = vehiculoRepo;
        this.configRepo = configRepo;
        this.simulacionService = new SimulacionService(resultadoRepo);
    }

    public void iniciarSimulacion() {
        if (circuitoRepo.listarTodos().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay circuitos registrados. Registra uno primero.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Circuito circuito = seleccionarCircuito();
        if (circuito == null) return;

        List<ParticipanteSimulacion> participantes = construirParticipantes();
        if (participantes.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No hay pilotos con un vehículo asignado.\nAsigna pilotos a un vehículo en el módulo de Vehículos antes de simular.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TipoClima clima = simulacionService.generarClimaAleatorio();
        JOptionPane.showMessageDialog(null,
                "Circuito: " + circuito.getNombre()
                        + "\nClima de la sesión (generado aleatoriamente): " + clima
                        + "\n\nSe simularán " + participantes.size() + " pilotos en paralelo.",
                "Iniciando clasificación", JOptionPane.INFORMATION_MESSAGE);

        try {
            List<ResultadoClasificacion> resultados = simulacionService.ejecutarSimulacion(circuito, clima, participantes);
            mostrarClasificacion(resultados, circuito, clima);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(null, "La simulación fue interrumpida.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "Ocurrió un error al guardar los resultados en la base de datos:\n" + e.getMessage(),
                    "Error de persistencia", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Circuito seleccionarCircuito() {
        List<Circuito> circuitos = circuitoRepo.listarTodos();
        String[] nombres = circuitos.stream()
                .map(c -> c.getId() + " - " + c.getNombre())
                .toArray(String[]::new);
        String seleccion = (String) JOptionPane.showInputDialog(null, "Selecciona el circuito para la sesión:",
                "Circuito", JOptionPane.QUESTION_MESSAGE, null, nombres, nombres[0]);
        if (seleccion == null) return null;
        int id = Integer.parseInt(seleccion.split(" - ")[0]);
        return circuitoRepo.buscarPorId(id).orElse(null);
    }

    /** Arma la lista de pilotos que pueden participar: solo los que tienen un vehículo asignado. */
    private List<ParticipanteSimulacion> construirParticipantes() {
        List<ParticipanteSimulacion> participantes = new ArrayList<>();
        for (Piloto piloto : pilotoRepo.listarTodos()) {
            Vehiculo vehiculo = vehiculoRepo.buscarPorPiloto(piloto.getId()).orElse(null);
            if (vehiculo == null) continue;

            ModoConduccion modo = obtenerUltimoModo(piloto.getId());
            participantes.add(new ParticipanteSimulacion(piloto, vehiculo, modo));
        }
        return participantes;
    }

    /** Usa el último modo de conducción configurado por el piloto; NORMAL si no hay historial o falla la BD. */
    private ModoConduccion obtenerUltimoModo(int pilotoId) {
        try {
            List<ConfiguracionVehiculo> historial = configRepo.historialPorPiloto(pilotoId);
            if (!historial.isEmpty()) return historial.get(0).getModoConduccion();
        } catch (RuntimeException e) {
            // Sin conexión a BD o sin historial todavía: se usa el modo por defecto.
        }
        return ModoConduccion.NORMAL;
    }

    private void mostrarClasificacion(List<ResultadoClasificacion> resultados, Circuito circuito, TipoClima clima) {
        String[] columnas = {"Pos.", "Piloto", "Tiempo (s)", "Clima"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0);
        for (ResultadoClasificacion r : resultados) {
            model.addRow(new Object[]{r.getPosicion(), r.getPilotoNombre(),
                    String.format("%.3f", r.getTiempoVueltaSegundos()), r.getClimaSesion()});
        }
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        JOptionPane.showMessageDialog(null, scrollPane,
                "Clasificación - " + circuito.getNombre() + " (" + clima + ")", JOptionPane.PLAIN_MESSAGE);
    }
}