package com.f1simulator.ui.javafx;

import com.f1simulator.persistence.ConfiguracionSqlRepository;
import com.f1simulator.persistence.ResultadoClasificacionSqlRepository;
import com.f1simulator.repository.CircuitoRepository;
import com.f1simulator.repository.EquipoRepository;
import com.f1simulator.repository.PilotoRepository;
import com.f1simulator.repository.VehiculoRepository;
import com.f1simulator.service.F1DataSeeder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class F1App extends Application {

    @Override
    public void start(Stage primaryStage) {
        CircuitoRepository circuitoRepo = new CircuitoRepository();
        EquipoRepository equipoRepo = new EquipoRepository();
        PilotoRepository pilotoRepo = new PilotoRepository();
        VehiculoRepository vehiculoRepo = new VehiculoRepository();
        ConfiguracionSqlRepository configRepo = new ConfiguracionSqlRepository();
        ResultadoClasificacionSqlRepository resultadoRepo = new ResultadoClasificacionSqlRepository();

        // Precarga de datos oficiales de F1 si la memoria está limpia
        F1DataSeeder.seedIfEmpty(circuitoRepo, equipoRepo, pilotoRepo, vehiculoRepo);

        F1DashboardView dashboardView = new F1DashboardView(
                circuitoRepo, equipoRepo, pilotoRepo, vehiculoRepo, configRepo, resultadoRepo
        );

        Scene scene = new Scene(dashboardView, 1220, 780);
        String css = getClass().getResource("/styles/f1-theme.css") != null
                ? getClass().getResource("/styles/f1-theme.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        primaryStage.setTitle("F1 Racing Simulator • High-Performance Concurrency & Telemetry Engine");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

