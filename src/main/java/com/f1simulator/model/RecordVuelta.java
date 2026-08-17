package com.f1simulator.model;

public class RecordVuelta {
    private String tiempoStr; // e.g., "1:15.584"
    private String piloto;
    private int ano;

    public RecordVuelta(String tiempoStr, String piloto, int ano) {
        this.tiempoStr = tiempoStr;
        this.piloto = piloto;
        this.ano = ano;
    }

    public String getTiempoStr() {
        return tiempoStr;
    }

    public void setTiempoStr(String tiempoStr) {
        this.tiempoStr = tiempoStr;
    }

    public String getPiloto() {
        return piloto;
    }

    public void setPiloto(String piloto) {
        this.piloto = piloto;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %d)", tiempoStr, piloto, ano);
    }
}
