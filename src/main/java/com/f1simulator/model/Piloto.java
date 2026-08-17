package com.f1simulator.model;

public class Piloto {
    private int id;
    private String nombre;
    private Equipo equipo; // Referencia al objeto Equipo
    private String rol; // "Líder" o "Escudero"

    public Piloto(int id, String nombre, Equipo equipo, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.equipo = equipo;
        this.rol = rol;
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

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return String.format("Piloto [ID=%d, Nombre=%s, Equipo=%s, Rol=%s]",
                id, nombre, (equipo != null ? equipo.getNombre() : "Sin equipo"), rol);
    }
}
