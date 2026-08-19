package com.f1simulator.ui;

import com.f1simulator.model.Circuito;
import com.f1simulator.model.ConfiguracionVehiculo;
import com.f1simulator.model.GanadorHistorico;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.Piloto;
import com.f1simulator.model.RecordVuelta;
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
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Menú de Simulación de Clasificación (Día 3): selección de circuito, clima
 * aleatorio, cálculo concurrente de tiempos de vuelta (un hilo por piloto),
 * actualización del récord de vuelta del circuito si corresponde, y
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

            registrarGanadorSiCorresponde(circuito, resultados);
            boolean nuevoRecord = actualizarRecordVueltaSiCorresponde(circuito, resultados);

            mostrarClasificacion(resultados, circuito, clima, nuevoRecord);
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

    /**
     * Registra al ganador de la sesión (posición 1) en el historial de
     * ganadores del circuito. Se persiste en la tabla ganadores_historicos
     * como un apéndice: el historial conserva todos los ganadores, no solo
     * el último. La "temporada" se identifica con el sesion_id de la sesión.
     */
    private void registrarGanadorSiCorresponde(Circuito circuito, List<ResultadoClasificacion> resultados) {
        resultados.stream()
                .filter(r -> r.getPosicion() == 1)
                .findFirst()
                .ifPresent(ganador -> circuitoRepo.registrarGanador(circuito.getId(),
                        new GanadorHistorico(String.valueOf(ganador.getSesionId()),
                                ganador.getPilotoNombre())));
    }

    /**
     * Compara el tiempo del ganador de la sesión contra el récord de vuelta
     * actual del circuito. Si es más rápido (o si el circuito no tenía
     * récord todavía), lo actualiza y lo persiste. Devuelve true si hubo
     * un nuevo récord, para poder avisarlo en pantalla.
     */
    private boolean actualizarRecordVueltaSiCorresponde(Circuito circuito, List<ResultadoClasificacion> resultados) {
        ResultadoClasificacion ganador = resultados.stream()
                .filter(r -> r.getPosicion() == 1)
                .findFirst()
                .orElse(null);
        if (ganador == null) return false;

        double tiempoNuevo = ganador.getTiempoVueltaSegundos();
        RecordVuelta recordActual = circuito.getRecordVuelta();

        boolean esNuevoRecord = (recordActual == null) || (tiempoNuevo < parsearTiempo(recordActual.getTiempoStr()));

        if (esNuevoRecord) {
            RecordVuelta nuevoRecord = new RecordVuelta(
                    formatearTiempo(tiempoNuevo), ganador.getPilotoNombre(), Year.now().getValue());
            circuito.setRecordVuelta(nuevoRecord);
            circuitoRepo.actualizar(circuito.getId(), circuito);
        }
        return esNuevoRecord;
    }

    /** Convierte "M:SS.mmm" a segundos totales, para poder comparar tiempos. */
    private double parsearTiempo(String tiempoStr) {
        try {
            String[] partes = tiempoStr.split(":");
            double minutos = Double.parseDouble(partes[0]);
            // El tiempo guardado puede usar coma decimal si el locale del sistema
            // es español ("1:07,769"): se normaliza a punto antes de convertir.
            double segundos = Double.parseDouble(partes[1].replace(',', '.'));
            return minutos * 60 + segundos;
        } catch (Exception e) {
            // Si el formato guardado no es el esperado, se trata como "sin récord válido"
            // para no bloquear la actualización con un nuevo récord bien formado.
            return Double.MAX_VALUE;
        }
    }

    /** Convierte segundos totales al formato "M:SS.mmm" usado por RecordVuelta. */
    private String formatearTiempo(double segundosTotales) {
        int minutos = (int) (segundosTotales / 60);
        double segundosRestantes = segundosTotales - (minutos * 60);
        return String.format("%d:%06.3f", minutos, segundosRestantes);
    }

    private void mostrarClasificacion(List<ResultadoClasificacion> resultados, Circuito circuito,
                                       TipoClima clima, boolean nuevoRecord) {
        String[] columnas = {"Pos.", "Piloto", "Tiempo (s)", "Clima"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0);
        for (ResultadoClasificacion r : resultados) {
            model.addRow(new Object[]{r.getPosicion(), r.getPilotoNombre(),
                    String.format("%.3f", r.getTiempoVueltaSegundos()), r.getClimaSesion()});
        }
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        String titulo = "Clasificación - " + circuito.getNombre() + " (" + clima + ")";
        if (nuevoRecord) {
            titulo += " — ¡NUEVO RÉCORD DE VUELTA!";
        }

        JOptionPane.showMessageDialog(null, scrollPane, titulo, JOptionPane.PLAIN_MESSAGE);
    }
}