package com.f1simulator.model;

public class Equipo {
    private String nombre; // Actúa como ID
    private String pais;
    private String motor;
    private java.util.List<Piloto> pilotos;

    public Equipo(String nombre, String pais, String motor) {
        this.nombre = nombre;
        this.pais = pais;
        this.motor = motor;
        this.pilotos = new java.util.ArrayList<>();
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

    public String getMotor() {
        return motor;
    }

    public void setMotor(String motor) {
        this.motor = motor;
    }

    public java.util.List<Piloto> getPilotos() {
        return pilotos;
    }

    public void agregarPiloto(Piloto piloto) {
        if (!this.pilotos.contains(piloto)) {
            this.pilotos.add(piloto);
        }
    }

    public void removerPiloto(Piloto piloto) {
        this.pilotos.remove(piloto);
    }

    @Override
    public String toString() {
        return String.format("Equipo [Nombre=%s, País=%s, Motor=%s]", nombre, pais, motor);
    }
}
