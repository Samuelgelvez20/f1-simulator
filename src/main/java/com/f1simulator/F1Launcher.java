package com.f1simulator;

import com.f1simulator.ui.javafx.F1App;

/**
 * Punto de entrada estándar para el ejecutable JAR con JavaFX.
 * Evita el conflicto de módulos de JavaFX en entornos Java 11+.
 */
public class F1Launcher {
    public static void main(String[] args) {
        F1App.main(args);
    }
}
