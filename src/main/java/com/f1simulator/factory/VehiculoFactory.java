package com.f1simulator.factory;

import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.RendimientoPorModo;
import com.f1simulator.model.TipoClima;
import com.f1simulator.model.TipoNeumatico;
import com.f1simulator.model.Vehiculo;

/**
 * Patrón Factory: crea un Vehiculo completo (incluyendo el rendimiento por
 * modo de conducción y clima) a partir de solo sus datos base.
 *
 * Por qué existe: pedir 18 valores de rendimiento por JOptionPane sería una
 * pésima experiencia de usuario. La fábrica los calcula con proporciones
 * razonables a partir de velocidad máxima y aceleración: a mayor velocidad
 * en modo agresivo, mayor consumo y desgaste; en modo ahorro, menos.
 */
public class VehiculoFactory {

    public Vehiculo crearVehiculo(String equipoNombre, String modelo, String motor,
                                   double velocidadMaximaKmh, double aceleracion0100,
                                   TipoNeumatico tipoNeumatico) {

        Vehiculo vehiculo = new Vehiculo(0, equipoNombre, modelo, motor,
                velocidadMaximaKmh, aceleracion0100, tipoNeumatico);

        vehiculo.setRendimientoModo(ModoConduccion.NORMAL,
                calcularRendimiento(velocidadMaximaKmh, 0.85, 2.0, 1.5));
        vehiculo.setRendimientoModo(ModoConduccion.AGRESIVA,
                calcularRendimiento(velocidadMaximaKmh, 0.95, 2.5, 2.2));
        vehiculo.setRendimientoModo(ModoConduccion.AHORRO_COMBUSTIBLE,
                calcularRendimiento(velocidadMaximaKmh, 0.78, 1.6, 1.0));

        return vehiculo;
    }

    /**
     * @param factorVelocidad  proporción de la velocidad máxima que se alcanza en promedio en ese modo
     * @param consumoBaseSeco  litros/vuelta de referencia en clima seco para ese modo
     * @param desgasteBaseSeco desgaste de referencia en clima seco para ese modo
     */
    private RendimientoPorModo calcularRendimiento(double velocidadMaximaKmh, double factorVelocidad,
                                                     double consumoBaseSeco, double desgasteBaseSeco) {
        RendimientoPorModo r = new RendimientoPorModo(velocidadMaximaKmh * factorVelocidad);

        r.setConsumo(TipoClima.SECO, consumoBaseSeco);
        r.setConsumo(TipoClima.LLUVIOSO, consumoBaseSeco * 1.10);
        r.setConsumo(TipoClima.EXTREMO, consumoBaseSeco * 1.25);

        r.setDesgaste(TipoClima.SECO, desgasteBaseSeco);
        r.setDesgaste(TipoClima.LLUVIOSO, desgasteBaseSeco * 0.55); // menos desgaste con lluvia moderada
        r.setDesgaste(TipoClima.EXTREMO, desgasteBaseSeco * 1.65);

        return r;
    }
}