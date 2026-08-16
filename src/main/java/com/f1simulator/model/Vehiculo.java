package com.f1simulator.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Vehiculo {

    private int id;
    private String equipoNombre;
    private String modelo;
    private String motor;
    private double velocidadMaximaKmh;
    private double aceleracion0100;
    private TipoNeumatico tipoNeumatico;
    private List<Integer> pilotoIds;
    private final Map<ModoConduccion, RendimientoPorModo> rendimiento = new EnumMap<>(ModoConduccion.class);

    public Vehiculo(int id, String equipoNombre, String modelo, String motor, double velocidadMaximaKmh, double aceleracion0100, TipoNeumatico tipoNeumatico) {
        this.id = id;
        this.equipoNombre = equipoNombre;
        this.modelo = modelo;
        this.motor = motor;
        this.velocidadMaximaKmh = velocidadMaximaKmh;
        this.aceleracion0100 = aceleracion0100;
        this.tipoNeumatico = tipoNeumatico;
        this.pilotoIds = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMotor() { return motor; }
    public void setMotor(String motor) { this.motor = motor; }

    public double getVelocidadMaximaKmh() { return velocidadMaximaKmh; }
    public void setVelocidadMaximaKmh(double v) { this.velocidadMaximaKmh = v; }

    public double getAceleracion0100() { return aceleracion0100; }
    public void setAceleracion0100(double a) { this.aceleracion0100 = a; }

    public TipoNeumatico getTipoNeumatico() { return tipoNeumatico; }
    public void setTipoNeumatico(TipoNeumatico tipoNeumatico) { this.tipoNeumatico = tipoNeumatico; }

    public List<Integer> getPilotoIds() { return pilotoIds; }

    public void asignarPiloto(int pilotoId) {
        if (!pilotoIds.contains(pilotoId)) pilotoIds.add(pilotoId);
    }

    public Map<ModoConduccion, RendimientoPorModo> getRendimiento() { return rendimiento; }

    public void setRendimientoModo(ModoConduccion modo, RendimientoPorModo r) {
        rendimiento.put(modo, r);
    }

    /**
     * true si el neumático montado sirve para el clima dado.
     * En SECO cualquier neumático sirve; en LLUVIOSO/EXTREMO se necesita
     * INTERMEDIO o LLUVIA. La usa la simulación (Día 3) para aplicar
     * la penalización del 10%.
     */
    public boolean tieneNeumaticoAdecuadoPara(TipoClima clima) {
        if (clima == TipoClima.SECO) return true;
        return tipoNeumatico == TipoNeumatico.INTERMEDIO || tipoNeumatico == TipoNeumatico.LLUVIA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehiculo)) return false;
        Vehiculo vehiculo = (Vehiculo) o;
        return id == vehiculo.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("#%d %s %s | Motor: %s | Vel. máx: %.0f km/h | 0-100: %.1fs | Neumático: %s",
                id, equipoNombre, modelo, motor, velocidadMaximaKmh, aceleracion0100, tipoNeumatico);
    }
}