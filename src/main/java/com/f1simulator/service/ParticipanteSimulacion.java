package com.f1simulator.service;

import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.Piloto;
import com.f1simulator.model.Vehiculo;

/** Agrupa a un piloto con el vehículo y el modo de conducción que usará en la simulación. */
public class ParticipanteSimulacion {

    private final Piloto piloto;
    private final Vehiculo vehiculo;
    private final ModoConduccion modo;

    public ParticipanteSimulacion(Piloto piloto, Vehiculo vehiculo, ModoConduccion modo) {
        this.piloto = piloto;
        this.vehiculo = vehiculo;
        this.modo = modo;
    }

    public Piloto getPiloto() { return piloto; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public ModoConduccion getModo() { return modo; }
}