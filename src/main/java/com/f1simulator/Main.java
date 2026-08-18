package com.f1simulator;

import com.f1simulator.persistence.ConfiguracionSqlRepository;
import com.f1simulator.persistence.ResultadoClasificacionSqlRepository;
import com.f1simulator.repository.*;
import com.f1simulator.ui.*;

import javax.swing.JOptionPane;

/**
 * Menú principal completo: todos los módulos integrados, incluida la
 * simulación con concurrencia y el historial persistido en PostgreSQL.
 */
public class Main {
    public static void main(String[] args) {
        CircuitoRepository circuitoRepo = new CircuitoRepository();
        PilotoRepository pilotoRepo = new PilotoRepository();
        EquipoRepository equipoRepo = new EquipoRepository();
        VehiculoRepository vehiculoRepo = new VehiculoRepository();
        ConfiguracionSqlRepository configRepo = new ConfiguracionSqlRepository();
        ResultadoClasificacionSqlRepository resultadoRepo = new ResultadoClasificacionSqlRepository();

        CircuitoUI circuitoUI = new CircuitoUI(circuitoRepo);
        EquipoUI equipoUI = new EquipoUI(equipoRepo);
        PilotoUI pilotoUI = new PilotoUI(pilotoRepo, equipoRepo);
        VehiculoUI vehiculoUI = new VehiculoUI(vehiculoRepo, equipoRepo, pilotoRepo);
        ConfiguracionUI configUI = new ConfiguracionUI(configRepo, vehiculoRepo, pilotoRepo);
        SimulacionUI simulacionUI = new SimulacionUI(circuitoRepo, pilotoRepo, vehiculoRepo, configRepo, resultadoRepo);
        ReportesUI historialUI = new ReportesUI(resultadoRepo, configRepo);

        String[] opciones = {
                "Circuitos", "Equipos", "Pilotos", "Vehículos", "Configuración",
                "Simular Clasificación", "Historial y Comparación", "Salir"
        };
        int seleccion;
        do {
            seleccion = JOptionPane.showOptionDialog(null,
                    "F1 Simulator",
                    "F1 Simulator", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, opciones, opciones[0]);

            switch (seleccion) {
                case 0 -> circuitoUI.mostrarMenu();
                case 1 -> equipoUI.mostrarMenu();
                case 2 -> pilotoUI.mostrarMenu();
                case 3 -> vehiculoUI.mostrarMenu();
                case 4 -> configUI.mostrarMenu();
                case 5 -> simulacionUI.iniciarSimulacion();
                case 6 -> historialUI.mostrarMenu();
                default -> { /* Salir */ }
            }
        } while (seleccion != 7 && seleccion != -1);
    }
}