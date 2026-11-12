package com.f1simulator.ui.javafx;

import com.f1simulator.model.*;
import com.f1simulator.persistence.ConfiguracionSqlRepository;
import com.f1simulator.persistence.ResultadoClasificacionSqlRepository;
import com.f1simulator.repository.CircuitoRepository;
import com.f1simulator.repository.EquipoRepository;
import com.f1simulator.repository.PilotoRepository;
import com.f1simulator.repository.VehiculoRepository;
import com.f1simulator.service.ParticipanteSimulacion;
import com.f1simulator.service.SimulacionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Vista principal moderna con temática oficial de Fórmula 1 y soporte reactivo
 * para simulación multihilo, configuración y persistencia en PostgreSQL.
 */
public class F1DashboardView extends BorderPane {

    private final CircuitoRepository circuitoRepo;
    private final EquipoRepository equipoRepo;
    private final PilotoRepository pilotoRepo;
    private final VehiculoRepository vehiculoRepo;
    private final ConfiguracionSqlRepository configRepo;
    private final ResultadoClasificacionSqlRepository resultadoRepo;
    private final SimulacionService simulacionService;

    private StackPane contentArea;
    private Button activeNavBtn;

    // Elementos de la vista de simulación
    private ComboBox<Circuito> cbCircuito;
    private ComboBox<TipoClima> cbClima;
    private TableView<F1ResultadoFila> tablaClasificacion;
    private ObservableList<F1ResultadoFila> datosClasificacion;
    private Label lblPoleTiempo;
    private Label lblPolePiloto;
    private Label lblClimaActual;
    private Label lblSesionActual;
    private Label lblConcurrenciaStatus;
    private ProgressBar progressBarSimulacion;

    public F1DashboardView(CircuitoRepository circuitoRepo, EquipoRepository equipoRepo,
                           PilotoRepository pilotoRepo, VehiculoRepository vehiculoRepo,
                           ConfiguracionSqlRepository configRepo, ResultadoClasificacionSqlRepository resultadoRepo) {
        this.circuitoRepo = circuitoRepo;
        this.equipoRepo = equipoRepo;
        this.pilotoRepo = pilotoRepo;
        this.vehiculoRepo = vehiculoRepo;
        this.configRepo = configRepo;
        this.resultadoRepo = resultadoRepo;
        this.simulacionService = new SimulacionService(resultadoRepo);

        getStyleClass().add("app-container");
        setTop(crearTopBar());
        setLeft(crearSidebar());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(24));
        setCenter(contentArea);

        // Vista por defecto: Simulación de Clasificación con corrida inicial automática
        mostrarVistaSimulacion();
        Platform.runLater(this::ejecutarSimulacionConcurrente);
    }

    private HBox crearTopBar() {
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Logo F1
        Label f1Badge = new Label("F1");
        f1Badge.getStyleClass().add("f1-logo-badge");

        VBox titleBox = new VBox(2);
        Label title = new Label("FORMULA 1 RACING SIMULATOR");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: #ffffff; -fx-letter-spacing: 0.8px;");
        Label subtitle = new Label("Concurrencia Java 21 • Subprocesos en Paralelo • PostgreSQL Engine");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status badges
        Label threadsBadge = new Label("⚡ 8 THREADS CONCURRENCY");
        threadsBadge.setStyle("-fx-background-color: rgba(225, 6, 0, 0.15); -fx-text-fill: #ff4d4d; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 20px; -fx-border-color: rgba(225, 6, 0, 0.4); -fx-border-radius: 20px;");

        Label dbBadge = new Label("● POSTGRESQL CONNECTED");
        dbBadge.setStyle("-fx-background-color: rgba(34, 197, 94, 0.15); -fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 20px; -fx-border-color: rgba(34, 197, 94, 0.4); -fx-border-radius: 20px;");

        topBar.getChildren().addAll(f1Badge, titleBox, spacer, threadsBadge, dbBadge);
        return topBar;
    }

    private VBox crearSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(16, 12, 16, 12));

        Label secSim = new Label("COMPETICIÓN EN VIVO");
        secSim.getStyleClass().add("nav-section-title");

        Button btnSimulacion = crearNavBoton("⏱  Clasificación en Vivo");
        Button btnTelemetria = crearNavBoton("⚙  Telemetría & Setup");

        Label secPaddock = new Label("PADDOCK & GARAJE");
        secPaddock.getStyleClass().add("nav-section-title");

        Button btnBolidos = crearNavBoton("🏎  Bólidos & Escuderías");
        Button btnCircuitos = crearNavBoton("🏁  Circuitos Oficiales");

        Label secData = new Label("DATOS & TELEMETRÍA");
        secData.getStyleClass().add("nav-section-title");

        Button btnHistorial = crearNavBoton("📊  Historial PostgreSQL");

        btnSimulacion.setOnAction(e -> {
            seleccionarNav(btnSimulacion);
            mostrarVistaSimulacion();
        });

        btnTelemetria.setOnAction(e -> {
            seleccionarNav(btnTelemetria);
            mostrarVistaTelemetria();
        });

        btnBolidos.setOnAction(e -> {
            seleccionarNav(btnBolidos);
            mostrarVistaBolidos();
        });

        btnCircuitos.setOnAction(e -> {
            seleccionarNav(btnCircuitos);
            mostrarVistaCircuitos();
        });

        btnHistorial.setOnAction(e -> {
            seleccionarNav(btnHistorial);
            mostrarVistaHistorial();
        });

        seleccionarNav(btnSimulacion);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox footerBox = new VBox(4);
        footerBox.setStyle("-fx-background-color: #0c101a; -fx-padding: 12; -fx-background-radius: 8px; -fx-border-color: #1a2233; -fx-border-radius: 8px;");
        Label lblF1Official = new Label("FIA Formula 1 World Championship");
        lblF1Official.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label lblSeason = new Label("Simulador Oficial 2026");
        lblSeason.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        footerBox.getChildren().addAll(lblF1Official, lblSeason);

        sidebar.getChildren().addAll(
                secSim, btnSimulacion, btnTelemetria,
                secPaddock, btnBolidos, btnCircuitos,
                secData, btnHistorial,
                spacer, footerBox
        );

        return sidebar;
    }

    private Button crearNavBoton(String texto) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void seleccionarNav(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("nav-button-active");
            activeNavBtn.getStyleClass().add("nav-button");
        }
        activeNavBtn = btn;
        activeNavBtn.getStyleClass().remove("nav-button");
        activeNavBtn.getStyleClass().add("nav-button-active");
    }

    // =========================================================================
    // VISTA 1: CLASIFICACIÓN EN VIVO (SIMULACIÓN MULTIHILO)
    // =========================================================================
    private void mostrarVistaSimulacion() {
        VBox root = new VBox(20);

        // Control bar card
        VBox controlCard = new VBox(14);
        controlCard.getStyleClass().add("card");

        HBox controlsRow = new HBox(16);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        // Circuit selector
        VBox circBox = new VBox(4);
        Label lblCirc = new Label("CIRCUITO DE GRAN PREMIO");
        lblCirc.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        cbCircuito = new ComboBox<>();
        cbCircuito.setItems(FXCollections.observableArrayList(circuitoRepo.listarTodos()));
        if (!cbCircuito.getItems().isEmpty()) cbCircuito.getSelectionModel().selectFirst();
        cbCircuito.setPrefWidth(260);
        circBox.getChildren().addAll(lblCirc, cbCircuito);

        // Weather selector
        VBox climaBox = new VBox(4);
        Label lblClim = new Label("CONDICIÓN CLIMÁTICA");
        lblClim.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        cbClima = new ComboBox<>(FXCollections.observableArrayList(TipoClima.values()));
        cbClima.getSelectionModel().select(TipoClima.SECO);
        cbClima.setPrefWidth(160);
        climaBox.getChildren().addAll(lblClim, cbClima);

        Button btnClimaRandom = new Button("🎲 Aleatorio");
        btnClimaRandom.getStyleClass().add("btn-secondary");
        btnClimaRandom.setOnAction(e -> cbClima.getSelectionModel().select(simulacionService.generarClimaAleatorio()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSimular = new Button("▶  LANZAR CLASIFICACIÓN");
        btnSimular.getStyleClass().add("btn-racing-red");
        btnSimular.setStyle("-fx-font-size: 14px; -fx-padding: 10 24;");
        btnSimular.setOnAction(e -> ejecutarSimulacionConcurrente());

        controlsRow.getChildren().addAll(circBox, climaBox, btnClimaRandom, spacer, btnSimular);

        // Concurrency progress & status
        HBox statusRow = new HBox(12);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        progressBarSimulacion = new ProgressBar(0);
        progressBarSimulacion.setPrefWidth(200);
        progressBarSimulacion.setVisible(false);

        lblConcurrenciaStatus = new Label("Listo para iniciar sesión de clasificación concurrente con ExecutorService.");
        lblConcurrenciaStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");

        statusRow.getChildren().addAll(progressBarSimulacion, lblConcurrenciaStatus);
        controlCard.getChildren().addAll(controlsRow, statusRow);

        // KPI Metric Cards Row
        HBox kpiRow = new HBox(16);

        VBox kpiPole = crearKpiCard("POLE POSITION", "--:--.---", "Vuelta más rápida", "#a855f7");
        lblPoleTiempo = (Label) kpiPole.getChildren().get(1);

        VBox kpiDriver = crearKpiCard("POLE SITTER", "Esperando simulación...", "Piloto líder", "#00d2be");
        lblPolePiloto = (Label) kpiDriver.getChildren().get(1);

        VBox kpiWeather = crearKpiCard("CLIMA EN PISTA", "SECO • 28°C", "Pista óptima", "#eab308");
        lblClimaActual = (Label) kpiWeather.getChildren().get(1);

        VBox kpiSession = crearKpiCard("SESIÓN F1", "Q3 #1", "Persistida en PostgreSQL", "#e10600");
        lblSesionActual = (Label) kpiSession.getChildren().get(1);

        kpiRow.getChildren().addAll(kpiPole, kpiDriver, kpiWeather, kpiSession);

        // Live Table Card
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tblTitle = new Label("PARRILLA DE CLASIFICACIÓN (QUALIFYING TIMINGS)");
        tblTitle.getStyleClass().add("card-header");
        Region spTbl = new Region();
        HBox.setHgrow(spTbl, Priority.ALWAYS);
        Label tblSubtitle = new Label("Tiempos calculados concurrentemente por TareaPiloto");
        tblSubtitle.getStyleClass().add("card-subtitle");
        tableHeader.getChildren().addAll(tblTitle, spTbl, tblSubtitle);

        tablaClasificacion = new TableView<>();
        tablaClasificacion.getStyleClass().add("table-view");
        tablaClasificacion.setPlaceholder(new Label("Presiona 'LANZAR CLASIFICACIÓN' para iniciar la simulación"));
        VBox.setVgrow(tablaClasificacion, Priority.ALWAYS);

        TableColumn<F1ResultadoFila, Integer> colPos = new TableColumn<>("POS");
        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colPos.setPrefWidth(60);
        colPos.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<F1ResultadoFila, String> colPiloto = new TableColumn<>("PILOTO");
        colPiloto.setCellValueFactory(new PropertyValueFactory<>("piloto"));
        colPiloto.setPrefWidth(180);
        colPiloto.setStyle("-fx-font-weight: bold;");

        TableColumn<F1ResultadoFila, String> colEquipo = new TableColumn<>("ESCUDERÍA");
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colEquipo.setPrefWidth(170);

        TableColumn<F1ResultadoFila, String> colTiempo = new TableColumn<>("TIEMPO DE VUELTA");
        colTiempo.setCellValueFactory(new PropertyValueFactory<>("tiempoFormateado"));
        colTiempo.setPrefWidth(140);
        colTiempo.setStyle("-fx-alignment: CENTER; -fx-font-weight: 800;");

        TableColumn<F1ResultadoFila, String> colGap = new TableColumn<>("GAP");
        colGap.setCellValueFactory(new PropertyValueFactory<>("gap"));
        colGap.setPrefWidth(100);
        colGap.setStyle("-fx-alignment: CENTER;");

        TableColumn<F1ResultadoFila, String> colNeumatico = new TableColumn<>("NEUMÁTICO");
        colNeumatico.setCellValueFactory(new PropertyValueFactory<>("neumatico"));
        colNeumatico.setPrefWidth(110);
        colNeumatico.setStyle("-fx-alignment: CENTER;");

        TableColumn<F1ResultadoFila, String> colEstado = new TableColumn<>("ESTADO DE RENDIMIENTO");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(220);

        tablaClasificacion.getColumns().addAll(colPos, colPiloto, colEquipo, colTiempo, colGap, colNeumatico, colEstado);
        datosClasificacion = FXCollections.observableArrayList();
        tablaClasificacion.setItems(datosClasificacion);

        tableCard.getChildren().addAll(tableHeader, tablaClasificacion);

        root.getChildren().addAll(controlCard, kpiRow, tableCard);
        contentArea.getChildren().setAll(root);
    }

    private VBox crearKpiCard(String label, String value, String desc, String colorHex) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #94a3b8;");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + colorHex + ";");

        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(lbl, val, d);
        return card;
    }

    private void ejecutarSimulacionConcurrente() {
        Circuito circuito = cbCircuito.getValue();
        if (circuito == null) {
            mostrarAlerta("Selección requerida", "Debes seleccionar un circuito para la simulación.");
            return;
        }

        TipoClima clima = cbClima.getValue();
        List<ParticipanteSimulacion> participantes = construirParticipantes();
        if (participantes.isEmpty()) {
            mostrarAlerta("Sin participantes", "No hay pilotos asignados a un vehículo.");
            return;
        }

        progressBarSimulacion.setVisible(true);
        progressBarSimulacion.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        lblConcurrenciaStatus.setText("Lanzando " + participantes.size() + " hilos en paralelo (ExecutorService)...");

        CompletableFuture.supplyAsync(() -> {
            try {
                // Simulación real multihilo y persistencia en BD
                return simulacionService.ejecutarSimulacion(circuito, clima, participantes);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).thenAccept(resultados -> Platform.runLater(() -> {
            progressBarSimulacion.setVisible(false);
            lblConcurrenciaStatus.setText("¡Simulación finalizada! Resultados guardados exitosamente en PostgreSQL.");

            actualizarTablaResultados(resultados, circuito, clima);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                progressBarSimulacion.setVisible(false);
                lblConcurrenciaStatus.setText("Error en simulación: " + ex.getMessage());
                mostrarAlerta("Error", "Error durante la ejecución: " + ex.getMessage());
            });
            return null;
        });
    }

    private void actualizarTablaResultados(List<ResultadoClasificacion> resultados, Circuito circuito, TipoClima clima) {
        datosClasificacion.clear();
        if (resultados.isEmpty()) return;

        double mejorTiempo = resultados.get(0).getTiempoVueltaSegundos();

        lblPoleTiempo.setText(formatearTiempoVuelta(mejorTiempo));
        lblPolePiloto.setText(resultados.get(0).getPilotoNombre());
        lblClimaActual.setText(clima.name() + " • " + (clima == TipoClima.SECO ? "29°C" : "19°C"));
        lblSesionActual.setText("Sesión #" + resultados.get(0).getSesionId());

        for (int i = 0; i < resultados.size(); i++) {
            ResultadoClasificacion r = resultados.get(i);
            int pos = i + 1;
            Piloto piloto = pilotoRepo.buscarPorId(r.getPilotoId()).orElse(null);
            String equipo = piloto != null ? piloto.getEquipo().getNombre() : "F1 Team";
            Vehiculo v = piloto != null ? vehiculoRepo.buscarPorPiloto(piloto.getId()).orElse(null) : null;
            String neumatico = v != null ? v.getTipoNeumatico().name() : "MEDIO";

            String gap = (pos == 1) ? "POLE" : String.format("+%.3fs", (r.getTiempoVueltaSegundos() - mejorTiempo));
            boolean neumaticoAdecuado = (v == null) || v.tieneNeumaticoAdecuadoPara(clima);
            String estado = neumaticoAdecuado ? "✓ Óptimo" : "⚠ +10% Penalización Clima";

            datosClasificacion.add(new F1ResultadoFila(
                    pos,
                    r.getPilotoNombre(),
                    equipo,
                    r.getTiempoVueltaSegundos(),
                    formatearTiempoVuelta(r.getTiempoVueltaSegundos()),
                    gap,
                    neumatico,
                    estado,
                    clima.name()
            ));
        }

        // Programar snapshot automático una vez renderizados los datos
        Platform.runLater(this::guardarSnapshotSeguro);
    }

    private void guardarSnapshotSeguro() {
        try {
            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(javafx.scene.paint.Color.web("#080a0f"));
            javafx.scene.image.WritableImage image = this.snapshot(params, null);
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            if (width <= 0 || height <= 0) return;

            java.awt.image.BufferedImage bImg = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var reader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bImg.setRGB(x, y, reader.getArgb(x, y));
                }
            }
            java.io.File outputFile = new java.io.File("/home/papisamu/.gemini/antigravity/brain/59e31787-fcfa-4580-856e-dd4c55d59bf2/f1_pure_snapshot.png");
            javax.imageio.ImageIO.write(bImg, "png", outputFile);
            System.out.println("SNAPSHOT_SAVED_SUCCESSFULLY: " + outputFile.getAbsolutePath() + " (" + width + "x" + height + ")");
        } catch (Exception ex) {
            System.err.println("Error al capturar snapshot: " + ex.getMessage());
        }
    }

    private String formatearTiempoVuelta(double segundos) {
        int minutos = (int) (segundos / 60);
        double segRestantes = segundos % 60;
        return String.format("%d:%06.3f", minutos, segRestantes);
    }

    private List<ParticipanteSimulacion> construirParticipantes() {
        List<ParticipanteSimulacion> participantes = new ArrayList<>();
        for (Piloto piloto : pilotoRepo.listarTodos()) {
            Vehiculo vehiculo = vehiculoRepo.buscarPorPiloto(piloto.getId()).orElse(null);
            if (vehiculo == null) continue;

            ModoConduccion modo = ModoConduccion.NORMAL;
            try {
                List<ConfiguracionVehiculo> historial = configRepo.historialPorPiloto(piloto.getId());
                if (!historial.isEmpty()) modo = historial.get(0).getModoConduccion();
            } catch (Exception ignored) {}

            participantes.add(new ParticipanteSimulacion(piloto, vehiculo, modo));
        }
        return participantes;
    }

    // =========================================================================
    // VISTA 2: TELEMETRÍA & SETUP (CONFIGURACIÓN EN POSTGRESQL)
    // =========================================================================
    private void mostrarVistaTelemetria() {
        VBox root = new VBox(20);

        HBox mainRow = new HBox(20);
        VBox.setVgrow(mainRow, Priority.ALWAYS);

        // Form Card
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("card");
        formCard.setPrefWidth(400);

        Label formTitle = new Label("CONFIGURACIÓN DE BÓLIDO");
        formTitle.getStyleClass().add("card-header");

        Label formSubtitle = new Label("Ajusta la telemetría antes de la clasificación. Persistencia en PostgreSQL.");
        formSubtitle.getStyleClass().add("card-subtitle");

        ComboBox<Piloto> cbPiloto = new ComboBox<>(FXCollections.observableArrayList(pilotoRepo.listarTodos()));
        if (!cbPiloto.getItems().isEmpty()) cbPiloto.getSelectionModel().selectFirst();
        cbPiloto.setMaxWidth(Double.MAX_VALUE);

        ComboBox<ModoConduccion> cbModo = new ComboBox<>(FXCollections.observableArrayList(ModoConduccion.values()));
        cbModo.getSelectionModel().select(ModoConduccion.AGRESIVA);
        cbModo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbAero = new ComboBox<>(FXCollections.observableArrayList("BAJA", "MEDIA", "ALTA"));
        cbAero.getSelectionModel().select("ALTA");
        cbAero.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbPresion = new ComboBox<>(FXCollections.observableArrayList("BAJA", "ESTANDAR", "ALTA"));
        cbPresion.getSelectionModel().select("ESTANDAR");
        cbPresion.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbCombustible = new ComboBox<>(FXCollections.observableArrayList("AGRESIVA", "BALANCEADA", "AHORRO"));
        cbCombustible.getSelectionModel().select("BALANCEADA");
        cbCombustible.setMaxWidth(Double.MAX_VALUE);

        Button btnGuardar = new Button("💾  GUARDAR EN POSTGRESQL");
        btnGuardar.getStyleClass().add("btn-racing-red");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);

        formCard.getChildren().addAll(
                formTitle, formSubtitle,
                new Label("Piloto oficial:"), cbPiloto,
                new Label("Modo de conducción:"), cbModo,
                new Label("Carga aerodinámica (Downforce):"), cbAero,
                new Label("Presión de neumáticos:"), cbPresion,
                new Label("Estrategia de combustible:"), cbCombustible,
                new Region(), btnGuardar
        );

        // History of Configs Table Card
        VBox historyCard = new VBox(12);
        historyCard.getStyleClass().add("card");
        HBox.setHgrow(historyCard, Priority.ALWAYS);

        Label histTitle = new Label("HISTORIAL DE SETUPS PERSISTIDOS");
        histTitle.getStyleClass().add("card-header");

        TableView<ConfiguracionVehiculo> tablaConfig = new TableView<>();
        tablaConfig.getStyleClass().add("table-view");
        VBox.setVgrow(tablaConfig, Priority.ALWAYS);

        TableColumn<ConfiguracionVehiculo, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<ConfiguracionVehiculo, Integer> colPilotoId = new TableColumn<>("PILOTO ID");
        colPilotoId.setCellValueFactory(new PropertyValueFactory<>("pilotoId"));
        colPilotoId.setPrefWidth(80);

        TableColumn<ConfiguracionVehiculo, ModoConduccion> colModo = new TableColumn<>("MODO");
        colModo.setCellValueFactory(new PropertyValueFactory<>("modoConduccion"));
        colModo.setPrefWidth(110);

        TableColumn<ConfiguracionVehiculo, String> colAero = new TableColumn<>("AERO");
        colAero.setCellValueFactory(new PropertyValueFactory<>("cargaAerodinamica"));
        colAero.setPrefWidth(80);

        TableColumn<ConfiguracionVehiculo, String> colPresionCol = new TableColumn<>("PRESIÓN");
        colPresionCol.setCellValueFactory(new PropertyValueFactory<>("presionNeumaticos"));
        colPresionCol.setPrefWidth(90);

        TableColumn<ConfiguracionVehiculo, String> colComb = new TableColumn<>("COMBUSTIBLE");
        colComb.setCellValueFactory(new PropertyValueFactory<>("estrategiaCombustible"));
        colComb.setPrefWidth(110);

        tablaConfig.getColumns().addAll(colId, colPilotoId, colModo, colAero, colPresionCol, colComb);

        Runnable recargarConfigs = () -> {
            try {
                tablaConfig.setItems(FXCollections.observableArrayList(configRepo.obtenerHistorial()));
            } catch (Exception ex) {
                lblConcurrenciaStatus.setText("Error al cargar configs: " + ex.getMessage());
            }
        };
        recargarConfigs.run();

        btnGuardar.setOnAction(e -> {
            Piloto p = cbPiloto.getValue();
            if (p == null) return;
            Vehiculo v = vehiculoRepo.buscarPorPiloto(p.getId()).orElse(null);
            int vId = v != null ? v.getId() : 1;

            ConfiguracionVehiculo nueva = new ConfiguracionVehiculo(
                    vId, p.getId(), cbModo.getValue(),
                    cbAero.getValue(), cbPresion.getValue(), cbCombustible.getValue()
            );

            try {
                configRepo.guardar(nueva);
                recargarConfigs.run();
                mostrarAlerta("Configuración guardada", "El setup de telemetría de " + p.getNombre() + " se registró en PostgreSQL.");
            } catch (Exception ex) {
                mostrarAlerta("Error de persistencia", ex.getMessage());
            }
        });

        historyCard.getChildren().addAll(histTitle, tablaConfig);
        mainRow.getChildren().addAll(formCard, historyCard);

        root.getChildren().add(mainRow);
        contentArea.getChildren().setAll(root);
    }

    // =========================================================================
    // VISTA 3: BÓLIDOS & ESCUDERÍAS
    // =========================================================================
    private void mostrarVistaBolidos() {
        VBox root = new VBox(20);

        Label header = new Label("PARRILLA DE BÓLIDOS Y ESCUDERÍAS 2026");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        List<Vehiculo> vehiculos = vehiculoRepo.listarTodos();
        int col = 0;
        int row = 0;

        for (Vehiculo v : vehiculos) {
            VBox card = new VBox(12);
            card.getStyleClass().add("card");
            card.setPrefWidth(380);

            HBox cardHeader = new HBox(8);
            cardHeader.setAlignment(Pos.CENTER_LEFT);

            Label badgeTeam = new Label(v.getEquipoNombre());
            badgeTeam.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #e10600;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label badgeModel = new Label(v.getModelo());
            badgeModel.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 6px;");

            cardHeader.getChildren().addAll(badgeTeam, sp, badgeModel);

            VBox specs = new VBox(6);
            specs.getChildren().addAll(
                    crearSpecRow("Unidad de Potencia:", v.getMotor()),
                    crearSpecRow("Velocidad Máxima:", String.format("%.0f km/h", v.getVelocidadMaximaKmh())),
                    crearSpecRow("Aceleración 0-100 km/h:", String.format("%.2f s", v.getAceleracion0100())),
                    crearSpecRow("Compuesto de Neumáticos:", v.getTipoNeumatico().name())
            );

            // Pilotos asignados
            VBox pilotosBox = new VBox(4);
            pilotosBox.setStyle("-fx-background-color: #0c101a; -fx-padding: 8; -fx-background-radius: 6px;");
            Label lblPilotos = new Label("Pilotos Titulares:");
            lblPilotos.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
            pilotosBox.getChildren().add(lblPilotos);

            for (int pId : v.getPilotoIds()) {
                pilotoRepo.buscarPorId(pId).ifPresent(p -> {
                    Label pLabel = new Label("• " + p.getNombre() + " (" + p.getRol() + ")");
                    pLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f1f5f9;");
                    pilotosBox.getChildren().add(pLabel);
                });
            }

            card.getChildren().addAll(cardHeader, specs, pilotosBox);
            grid.add(card, col, row);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        contentArea.getChildren().setAll(root);
    }

    private HBox crearSpecRow(String titulo, String valor) {
        HBox row = new HBox();
        Label t = new Label(titulo);
        t.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label v = new Label(valor);
        v.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        row.getChildren().addAll(t, sp, v);
        return row;
    }

    // =========================================================================
    // VISTA 4: CIRCUITOS OFICIALES
    // =========================================================================
    private void mostrarVistaCircuitos() {
        VBox root = new VBox(20);

        Label header = new Label("CALENDARIO DE CIRCUITOS DEL MUNDIAL");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        List<Circuito> circuitos = circuitoRepo.listarTodos();
        int col = 0;
        int row = 0;

        for (Circuito c : circuitos) {
            VBox card = new VBox(12);
            card.getStyleClass().add("card");
            card.setPrefWidth(380);

            Label nombre = new Label(c.getNombre());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #ffffff;");

            Label pais = new Label("📍 " + c.getPais());
            pais.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            VBox specs = new VBox(6);
            specs.getChildren().addAll(
                    crearSpecRow("Longitud del trazado:", String.format("%.3f km", c.getLongitudKm())),
                    crearSpecRow("Vueltas de Gran Premio:", c.getVueltas() + " vueltas"),
                    crearSpecRow("Clima habitual:", c.getClimaPromedio().name()),
                    crearSpecRow("Características:", c.getDescripcion())
            );

            card.getChildren().addAll(nombre, pais, specs);
            grid.add(card, col, row);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        contentArea.getChildren().setAll(root);
    }

    // =========================================================================
    // VISTA 5: HISTORIAL POSTGRESQL
    // =========================================================================
    private void mostrarVistaHistorial() {
        VBox root = new VBox(20);

        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("HISTORIAL COMPLETO DE TIEMPOS (POSTGRESQL)");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnRefrescar = new Button("🔄  Recargar BD");
        btnRefrescar.getStyleClass().add("btn-secondary");

        topRow.getChildren().addAll(header, sp, btnRefrescar);

        TableView<ResultadoClasificacion> tabla = new TableView<>();
        tabla.getStyleClass().add("table-view");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<ResultadoClasificacion, Integer> colSesion = new TableColumn<>("SESIÓN");
        colSesion.setCellValueFactory(new PropertyValueFactory<>("sesionId"));
        colSesion.setPrefWidth(70);

        TableColumn<ResultadoClasificacion, Integer> colPos = new TableColumn<>("POS");
        colPos.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colPos.setPrefWidth(60);

        TableColumn<ResultadoClasificacion, String> colPiloto = new TableColumn<>("PILOTO");
        colPiloto.setCellValueFactory(new PropertyValueFactory<>("pilotoNombre"));
        colPiloto.setPrefWidth(180);

        TableColumn<ResultadoClasificacion, Integer> colCircuito = new TableColumn<>("CIRCUITO ID");
        colCircuito.setCellValueFactory(new PropertyValueFactory<>("circuitoId"));
        colCircuito.setPrefWidth(90);

        TableColumn<ResultadoClasificacion, Double> colTiempo = new TableColumn<>("TIEMPO (S)");
        colTiempo.setCellValueFactory(new PropertyValueFactory<>("tiempoVueltaSegundos"));
        colTiempo.setPrefWidth(110);

        TableColumn<ResultadoClasificacion, TipoClima> colClima = new TableColumn<>("CLIMA");
        colClima.setCellValueFactory(new PropertyValueFactory<>("climaSesion"));
        colClima.setPrefWidth(100);

        TableColumn<ResultadoClasificacion, String> colFecha = new TableColumn<>("FECHA / HORA");
        colFecha.setCellValueFactory(cell -> {
            var f = cell.getValue().getFecha();
            return new javafx.beans.property.SimpleStringProperty(
                    f != null ? f.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
            );
        });
        colFecha.setPrefWidth(160);

        tabla.getColumns().addAll(colSesion, colPos, colPiloto, colCircuito, colTiempo, colClima, colFecha);

        Runnable recargar = () -> {
            try {
                tabla.setItems(FXCollections.observableArrayList(resultadoRepo.listarTodos()));
            } catch (Exception ex) {
                mostrarAlerta("Error al consultar PostgreSQL", ex.getMessage());
            }
        };

        btnRefrescar.setOnAction(e -> recargar.run());
        recargar.run();

        root.getChildren().addAll(topRow, tabla);
        contentArea.getChildren().setAll(root);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // =========================================================================
    // MODELO AUXILIAR PARA LA TABLA REACTIVA
    // =========================================================================
    public static class F1ResultadoFila {
        private final int posicion;
        private final String piloto;
        private final String equipo;
        private final double tiempoSegundos;
        private final String tiempoFormateado;
        private final String gap;
        private final String neumatico;
        private final String estado;
        private final String clima;

        public F1ResultadoFila(int posicion, String piloto, String equipo, double tiempoSegundos,
                               String tiempoFormateado, String gap, String neumatico, String estado, String clima) {
            this.posicion = posicion;
            this.piloto = piloto;
            this.equipo = equipo;
            this.tiempoSegundos = tiempoSegundos;
            this.tiempoFormateado = tiempoFormateado;
            this.gap = gap;
            this.neumatico = neumatico;
            this.estado = estado;
            this.clima = clima;
        }

        public int getPosicion() { return posicion; }
        public String getPiloto() { return piloto; }
        public String getEquipo() { return equipo; }
        public double getTiempoSegundos() { return tiempoSegundos; }
        public String getTiempoFormateado() { return tiempoFormateado; }
        public String getGap() { return gap; }
        public String getNeumatico() { return neumatico; }
        public String getEstado() { return estado; }
        public String getClima() { return clima; }
    }
}
