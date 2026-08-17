package com.f1simulator.service;

import com.f1simulator.model.Circuito;
import com.f1simulator.model.Vehiculo;
import com.f1simulator.model.TipoClima;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.RendimientoPorModo;

public class ImpactoCircuitoService {

    public static String calcularImpacto(Circuito circuito, Vehiculo vehiculo, TipoClima clima, ModoConduccion modo) {
        if (circuito == null || vehiculo == null || clima == null || modo == null) {
            return "Faltan datos para realizar el cálculo.";
        }

        if (vehiculo.getRendimiento() == null || vehiculo.getRendimiento().get(modo) == null) {
            return "Información de rendimiento no disponible para el vehículo y modo de conducción especificados.";
        }

        RendimientoPorModo rendimiento = vehiculo.getRendimiento().get(modo);

        double distanciaTotal = circuito.getLongitudKm() * circuito.getVueltas();
        double consumoTotalCombustible = distanciaTotal * rendimiento.getConsumo(clima);
        double desgasteTotalNeumaticos = distanciaTotal * rendimiento.getDesgaste(clima);

        return String.format(
                "Cálculo de Impacto en %s\n" +
                        "Vehículo: %s\n" +
                        "Clima: %s | Modo de Conducción: %s\n" +
                        "---------------------------------------------------\n" +
                        "Distancia Total: %.2f km\n" +
                        "Consumo de Combustible: %.2f unidades\n" +
                        "Desgaste de Neumáticos: %.2f unidades\n",
                circuito.getNombre(), vehiculo.getModelo(), clima, modo,
                distanciaTotal, consumoTotalCombustible, desgasteTotalNeumaticos);
    }
}
