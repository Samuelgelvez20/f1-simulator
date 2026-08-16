package com.f1simulator.model;

import java.time.LocalDateTime;

/**
 * Configuración aplicada a un vehículo antes de una simulación.
 * Se guarda automáticamente (historia de usuario "Guardar configuración") y,
 * a partir del Día 3, persiste en PostgreSQL como historial.
 */
public class ConfiguracionVehiculo {

    private int id; // se asigna al persistir en BD (Día 3), por ahora queda en 0
    private int vehiculoId;
    private int pilotoId;
    private ModoConduccion modoConduccion;
    private String cargaAerodinamica;      // "BAJA", "MEDIA", "ALTA"
    private String presionNeumaticos;      // "BAJA", "ESTANDAR", "ALTA"
    private String estrategiaCombustible;  // "AGRESIVA", "BALANCEADA", "AHORRO"
    private LocalDateTime fechaCreacion;

    public ConfiguracionVehiculo(int vehiculoId, int pilotoId, ModoConduccion modoConduccion,
                                  String cargaAerodinamica, String presionNeumaticos,
                                  String estrategiaCombustible) {
        this.vehiculoId = vehiculoId;
        this.pilotoId = pilotoId;
        this.modoConduccion = modoConduccion;
        this.cargaAerodinamica = cargaAerodinamica;
        this.presionNeumaticos = presionNeumaticos;
        this.estrategiaCombustible = estrategiaCombustible;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVehiculoId() { return vehiculoId; }
    public int getPilotoId() { return pilotoId; }
    public ModoConduccion getModoConduccion() { return modoConduccion; }
    public String getCargaAerodinamica() { return cargaAerodinamica; }
    public String getPresionNeumaticos() { return presionNeumaticos; }
    public String getEstrategiaCombustible() { return estrategiaCombustible; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    @Override
    public String toString() {
        return String.format("[%s] Vehículo #%d | Modo: %s | Aero: %s | Presión: %s | Combustible: %s",
                fechaCreacion.toLocalDate(), vehiculoId, modoConduccion,
                cargaAerodinamica, presionNeumaticos, estrategiaCombustible);
    }
}