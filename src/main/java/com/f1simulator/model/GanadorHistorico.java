package com.f1simulator.model;

public class GanadorHistorico {
    private String temporada; // could be int 2023 or String "2023"
    private String piloto;

    public GanadorHistorico(String temporada, String piloto) {
        this.temporada = temporada;
        this.piloto = piloto;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public String getPiloto() {
        return piloto;
    }

    public void setPiloto(String piloto) {
        this.piloto = piloto;
    }

    @Override
    public String toString() {
        return String.format("Temporada %s: %s", temporada, piloto);
    }
}
