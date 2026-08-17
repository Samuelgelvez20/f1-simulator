package com.f1simulator.model;

import java.time.LocalDateTime;

/**
 * Resultado de un piloto en una sesión de clasificación simulada.
 * Se calcula de forma concurrente el Día 3 (un hilo por piloto) y se
 * persiste en PostgreSQL.
 */
public class ResultadoClasificacion {

    private int id; // se asigna al persistir en BD (Día 3)
    private int sesionId; // agrupa todos los resultados de una misma simulación
    private int pilotoId;
    private String pilotoNombre;
    private int circuitoId;
    private double tiempoVueltaSegundos;
    private TipoClima climaSesion;
    private int posicion; // se calcula al ordenar todos los resultados
    private LocalDateTime fecha;

    public ResultadoClasificacion(int sesionId, int pilotoId, String pilotoNombre, int circuitoId,
                                   double tiempoVueltaSegundos, TipoClima climaSesion) {
        this.sesionId = sesionId;
        this.pilotoId = pilotoId;
        this.pilotoNombre = pilotoNombre;
        this.circuitoId = circuitoId;
        this.tiempoVueltaSegundos = tiempoVueltaSegundos;
        this.climaSesion = climaSesion;
        this.fecha = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSesionId() { return sesionId; }
    public int getPilotoId() { return pilotoId; }
    public String getPilotoNombre() { return pilotoNombre; }
    public int getCircuitoId() { return circuitoId; }
    public double getTiempoVueltaSegundos() { return tiempoVueltaSegundos; }
    public TipoClima getClimaSesion() { return climaSesion; }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    @Override
    public String toString() {
        return String.format("P%d - %s - %.3fs (%s)", posicion, pilotoNombre, tiempoVueltaSegundos, climaSesion);
    }
}