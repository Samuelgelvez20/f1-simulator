package com.f1simulator.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Rendimiento de un vehículo para UN modo de conducción específico:
 * velocidad promedio, y consumo/desgaste por cada tipo de clima.
 * Replica la estructura del JSON de ejemplo del proyecto.
 */
public class RendimientoPorModo {

    private double velocidadPromedioKmh;
    private final Map<TipoClima, Double> consumoCombustible = new EnumMap<>(TipoClima.class);
    private final Map<TipoClima, Double> desgasteNeumaticos = new EnumMap<>(TipoClima.class);

    public RendimientoPorModo(double velocidadPromedioKmh) {
        this.velocidadPromedioKmh = velocidadPromedioKmh;
    }

    public double getVelocidadPromedioKmh() { return velocidadPromedioKmh; }
    public void setVelocidadPromedioKmh(double v) { this.velocidadPromedioKmh = v; }

    public void setConsumo(TipoClima clima, double valor) { consumoCombustible.put(clima, valor); }
    public double getConsumo(TipoClima clima) { return consumoCombustible.getOrDefault(clima, 0.0); }

    public void setDesgaste(TipoClima clima, double valor) { desgasteNeumaticos.put(clima, valor); }
    public double getDesgaste(TipoClima clima) { return desgasteNeumaticos.getOrDefault(clima, 0.0); }

    @Override
    public String toString() {
        return String.format("Vel. prom: %.0f km/h | Consumo seco/lluvioso/extremo: %.1f/%.1f/%.1f | "
                        + "Desgaste seco/lluvioso/extremo: %.1f/%.1f/%.1f",
                velocidadPromedioKmh,
                getConsumo(TipoClima.SECO), getConsumo(TipoClima.LLUVIOSO), getConsumo(TipoClima.EXTREMO),
                getDesgaste(TipoClima.SECO), getDesgaste(TipoClima.LLUVIOSO), getDesgaste(TipoClima.EXTREMO));
    }
}