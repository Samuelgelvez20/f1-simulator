package com.f1simulator.ui;

import com.f1simulator.model.ConfiguracionVehiculo;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.Vehiculo;
import com.f1simulator.repository.ConfiguracionRepository;
import com.f1simulator.repository.VehiculoRepository;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Optional;

/**
 * Menú de configuración de vehículo con JOptionPane: aplicar configuración
 * (se guarda automáticamente) y ver historial de configuraciones previas.
 *
 * NOTA (Día 1): el ID de piloto se pide como número libre por ahora, sin
 * validar contra PilotoRepository (aún no existe). Se integra en el Día 2.
 */
public class ConfiguracionUI {

    private final ConfiguracionRepository configRepo;
    private final VehiculoRepository vehiculoRepo;

    public ConfiguracionUI(ConfiguracionRepository configRepo, VehiculoRepository vehiculoRepo) {
        this.configRepo = configRepo;
        this.vehiculoRepo = vehiculoRepo;
    }

    public void mostrarMenu() {
        String[] opciones = {"Configurar vehículo", "Ver historial por vehículo", "Ver historial por piloto", "Volver"};
        int seleccion;
        do {
            seleccion = JOptionPane.showOptionDialog(null, "Configuración de Vehículo",
                    "Configuración", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, opciones, opciones[0]);

            switch (seleccion) {
                case 0 -> configurarVehiculo();
                case 1 -> verHistorialPorVehiculo();
                case 2 -> verHistorialPorPiloto();
                default -> { /* Volver o cerrar */ }
            }
        } while (seleccion != 3 && seleccion != -1);
    }

    private void configurarVehiculo() {
        Integer vehiculoId = pedirEntero("ID del vehículo a configurar:");
        if (vehiculoId == null) return;

        Optional<Vehiculo> vehiculo = vehiculoRepo.buscarPorId(vehiculoId);
        if (vehiculo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No existe un vehículo con ese ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer pilotoId = pedirEntero("ID del piloto que configura el vehículo:");
        if (pilotoId == null) return;

        ModoConduccion modo = pedirOpcion("Modo de conducción:", ModoConduccion.values());
        if (modo == null) return;

        String aero = pedirOpcionTexto("Carga aerodinámica:", new String[]{"BAJA", "MEDIA", "ALTA"});
        if (aero == null) return;

        String presion = pedirOpcionTexto("Presión de neumáticos:", new String[]{"BAJA", "ESTANDAR", "ALTA"});
        if (presion == null) return;

        String combustible = pedirOpcionTexto("Estrategia de combustible:", new String[]{"AGRESIVA", "BALANCEADA", "AHORRO"});
        if (combustible == null) return;

        ConfiguracionVehiculo config = new ConfiguracionVehiculo(vehiculoId, pilotoId, modo, aero, presion, combustible);
        configRepo.guardar(config);

        JOptionPane.showMessageDialog(null,
                "Configuración aplicada y guardada automáticamente en el historial.\n\n" + config,
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void verHistorialPorVehiculo() {
        Integer vehiculoId = pedirEntero("ID del vehículo:");
        if (vehiculoId == null) return;

        List<ConfiguracionVehiculo> historial = configRepo.historialPorVehiculo(vehiculoId);
        mostrarHistorial(historial, "Historial de configuraciones - Vehículo #" + vehiculoId);
    }

    private void verHistorialPorPiloto() {
        Integer pilotoId = pedirEntero("ID del piloto:");
        if (pilotoId == null) return;

        List<ConfiguracionVehiculo> historial = configRepo.historialPorPiloto(pilotoId);
        mostrarHistorial(historial, "Historial de configuraciones - Piloto #" + pilotoId);
    }

    private void mostrarHistorial(List<ConfiguracionVehiculo> historial, String titulo) {
        if (historial.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay configuraciones registradas.", titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        historial.forEach(c -> sb.append(c).append("\n"));
        JOptionPane.showMessageDialog(null, sb.toString(), titulo, JOptionPane.PLAIN_MESSAGE);
    }

    // ---- Helpers de entrada ----

    private Integer pedirEntero(String mensaje) {
        String valor = JOptionPane.showInputDialog(null, mensaje);
        if (valor == null) return null;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Debes ingresar un número válido.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            return pedirEntero(mensaje);
        }
    }

    private ModoConduccion pedirOpcion(String mensaje, ModoConduccion[] opciones) {
        String[] nombres = new String[opciones.length];
        for (int i = 0; i < opciones.length; i++) nombres[i] = opciones[i].name();
        String seleccion = (String) JOptionPane.showInputDialog(null, mensaje,
                "Selección", JOptionPane.QUESTION_MESSAGE, null, nombres, nombres[0]);
        if (seleccion == null) return null;
        return ModoConduccion.valueOf(seleccion);
    }

    private String pedirOpcionTexto(String mensaje, String[] opciones) {
        return (String) JOptionPane.showInputDialog(null, mensaje,
                "Selección", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
    }
}