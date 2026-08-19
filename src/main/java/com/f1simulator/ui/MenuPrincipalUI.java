package com.f1simulator.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MenuPrincipalUI extends JFrame {

    private final CircuitoUI circuitoUI;
    private final EquipoUI equipoUI;
    private final PilotoUI pilotoUI;
    private final VehiculoUI vehiculoUI;
    private final ConfiguracionUI configuracionUI;
    private final SimulacionUI simulacionUI;
    private final ReportesUI reportesUI;

    public MenuPrincipalUI(CircuitoUI circuitoUI, EquipoUI equipoUI, PilotoUI pilotoUI,
            VehiculoUI vehiculoUI, ConfiguracionUI configuracionUI,
            SimulacionUI simulacionUI, ReportesUI reportesUI) {
        this.circuitoUI = circuitoUI;
        this.equipoUI = equipoUI;
        this.pilotoUI = pilotoUI;
        this.vehiculoUI = vehiculoUI;
        this.configuracionUI = configuracionUI;
        this.simulacionUI = simulacionUI;
        this.reportesUI = reportesUI;

        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("Simulador de Fórmula 1 - Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Confirmar antes de salir al cerrar el frame
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salirSistema();
            }
        });

        // Configurar layout principal
        setLayout(new BorderLayout(10, 10));

        // Panel de Cabecera (Banner)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(153, 0, 0)); // Rojo oscuro F1
        headerPanel.setBorder(new EmptyBorder(25, 10, 25, 10));
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel titleLabel = new JLabel("🏁 FÓRMULA 1 SIMULATOR 🏁");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Panel de Botones
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new EmptyBorder(20, 40, 30, 40));
        buttonsPanel.setLayout(new GridLayout(4, 2, 15, 15));

        // Iniciar Botones del Dashboard
        JButton btnCircuitos = crearBotonDashboard("Gestión de Circuitos", "🏎️");
        JButton btnEquiposPilotos = crearBotonDashboard("Gestión de Equipos y Pilotos", "👥");
        JButton btnVehiculos = crearBotonDashboard("Gestión de Vehículos", "🔧");
        JButton btnConfigurar = crearBotonDashboard("Configurar Vehículo", "⚙️");
        JButton btnSimular = crearBotonDashboard("Simular Clasificación", "🚦");
        JButton btnReportes = crearBotonDashboard("Ver Reportes e Historial", "📊");
        JButton btnSalir = crearBotonDashboard("Salir", "🚪");

        // Panel decorativo para simetría 4x2
        JPanel widgetPanel = new JPanel();
        widgetPanel.setLayout(new BorderLayout());
        widgetPanel.setBackground(new Color(40, 40, 40));
        widgetPanel.setBorder(BorderFactory.createLineBorder(new Color(153, 0, 0), 1));

        JLabel widgetText = new JLabel("Paddock Dashboard Activo", SwingConstants.CENTER);
        widgetText.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        widgetText.setForeground(Color.LIGHT_GRAY);
        widgetPanel.add(widgetText, BorderLayout.CENTER);

        // Eventos / Acciones
        btnCircuitos.addActionListener(e -> {
            setVisible(false);
            circuitoUI.mostrarMenu();
            setVisible(true);
        });

        btnEquiposPilotos.addActionListener(e -> {
            setVisible(false);
            gestionarEquiposOPilotos();
            setVisible(true);
        });

        btnVehiculos.addActionListener(e -> {
            setVisible(false);
            vehiculoUI.mostrarMenu();
            setVisible(true);
        });

        btnConfigurar.addActionListener(e -> {
            setVisible(false);
            configuracionUI.mostrarMenu();
            setVisible(true);
        });

        btnSimular.addActionListener(e -> {
            setVisible(false);
            simulacionUI.iniciarSimulacion();
            setVisible(true);
        });

        btnReportes.addActionListener(e -> {
            setVisible(false);
            reportesUI.mostrarMenu();
            setVisible(true);
        });

        btnSalir.addActionListener(e -> salirSistema());

        // Agregar elementos al panel
        buttonsPanel.add(btnCircuitos);
        buttonsPanel.add(btnEquiposPilotos);
        buttonsPanel.add(btnVehiculos);
        buttonsPanel.add(btnConfigurar);
        buttonsPanel.add(btnSimular);
        buttonsPanel.add(btnReportes);
        buttonsPanel.add(btnSalir);
        buttonsPanel.add(widgetPanel);

        add(buttonsPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(new EmptyBorder(5, 5, 10, 5));
        JLabel footerLabel = new JLabel("F1 Simulator © 2026. Made with FlatLaf.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);

        // Tamaño y ubicación
        setSize(700, 520);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JButton crearBotonDashboard(String texto, String emoji) {
        JButton btn = new JButton(
                "<html><div style='text-align: center;'>" + emoji + " <b>" + texto + "</b></div></html>");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        return btn;
    }

    private void gestionarEquiposOPilotos() {
        String[] opciones = { "Gestión de Equipos", "Gestión de Pilotos", "Volver" };
        int seleccion = JOptionPane.showOptionDialog(
                this,
                "Seleccione el módulo a cargar:",
                "Gestión de Equipos y Pilotos",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (seleccion == 0) {
            equipoUI.mostrarMenu();
        } else if (seleccion == 1) {
            pilotoUI.mostrarMenu();
        }
    }

    private void salirSistema() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea salir del Simulador de Fórmula 1?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
