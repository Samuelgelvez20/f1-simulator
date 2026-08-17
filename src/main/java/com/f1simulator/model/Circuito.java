package com.f1simulator.model;

public class Circuito {
    private String id;
    private String nombre;
    private String pais;
    private double longitudKm;
    private int vueltas;
    private String descripcion;
    private TipoClima climaPromedio;

    public Circuito(String id, String nombre, String pais, double longitudKm, int vueltas, String descripcion,
            TipoClima climaPromedio) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.longitudKm = longitudKm;
        this.vueltas = vueltas;
        this.descripcion = descripcion;
        this.climaPromedio = climaPromedio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Override
    public String toString() {
        return String.format("Circuito [ID=%s, Nombre=%s, País=%s, Longitud=%.2f km, Vueltas=%d, Clima=%s]",
                id, nombre, pais, longitudKm, vueltas, climaPromedio);
    }
}
