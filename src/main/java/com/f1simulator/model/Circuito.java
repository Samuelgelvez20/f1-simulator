package com.f1simulator.model;

public class Circuito {
    private int id;
    private String nombre;
    private String pais;
    private double longitudKm;
    private int vueltas;
    private String descripcion;
    private TipoClima climaPromedio;
    private RecordVuelta recordVuelta;
    private java.util.List<GanadorHistorico> ganadores;

    public Circuito(int id, String nombre, String pais, double longitudKm, int vueltas, String descripcion,
            TipoClima climaPromedio) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.longitudKm = longitudKm;
        this.vueltas = vueltas;
        this.descripcion = descripcion;
        this.climaPromedio = climaPromedio;
        this.ganadores = new java.util.ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public double getLongitudKm() {
        return longitudKm;
    }

    public void setLongitudKm(double longitudKm) {
        this.longitudKm = longitudKm;
    }

    public int getVueltas() {
        return vueltas;
    }

    public void setVueltas(int vueltas) {
        this.vueltas = vueltas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoClima getClimaPromedio() {
        return climaPromedio;
    }

    public void setClimaPromedio(TipoClima climaPromedio) {
        this.climaPromedio = climaPromedio;
    }

    public RecordVuelta getRecordVuelta() {
        return recordVuelta;
    }

    public void setRecordVuelta(RecordVuelta recordVuelta) {
        this.recordVuelta = recordVuelta;
    }

    public java.util.List<GanadorHistorico> getGanadores() {
        return ganadores;
    }

    public void setGanadores(java.util.List<GanadorHistorico> ganadores) {
        this.ganadores = ganadores;
    }

    public void agregarGanador(GanadorHistorico ganador) {
        this.ganadores.add(ganador);
    }

    @Override
    public String toString() {
        return String.format("Circuito [ID=%d, Nombre=%s, País=%s, Longitud=%.2f km, Vueltas=%d, Clima=%s]",
                id, nombre, pais, longitudKm, vueltas, climaPromedio);
    }
}
