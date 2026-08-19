package com.f1simulator;

import com.f1simulator.persistence.ConfiguracionSqlRepository;
import com.f1simulator.persistence.ResultadoClasificacionSqlRepository;
import com.f1simulator.repository.*;
import com.f1simulator.ui.*;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.Color;

/**
 * Menú principal completo: todos los módulos integrados, incluida la
 * simulación con concurrencia y el historial persistido en PostgreSQL.
 */
public class Main {
    public static void main(String[] args) {
        // Inicializar tema oscuro con FlatDarkLaf
        FlatDarkLaf.setup();

        // Configuración de interfaz con acentuado rojo (Estilo F1)
        Color f1RedAccent = new Color(225, 6, 0); // Rojo F1
        Color f1DarkRed = new Color(153, 0, 0);   // Rojo Oscuro
        
        UIManager.put("Button.background", f1DarkRed);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.selectedBackground", f1RedAccent);
        UIManager.put("Button.focusedBackground", f1RedAccent);
        UIManager.put("Button.hoverBackground", f1RedAccent);

        UIManager.put("Table.selectionBackground", f1DarkRed);
        UIManager.put("Table.selectionForeground", Color.WHITE);

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
        ReportesUI reportesUI = new ReportesUI(resultadoRepo, configRepo);

        // Lanzar interfaz gráfica en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            MenuPrincipalUI dashboard = new MenuPrincipalUI(
                    circuitoUI, equipoUI, pilotoUI, vehiculoUI, configUI, simulacionUI, reportesUI
            );
            dashboard.setVisible(true);
        });
    }
}