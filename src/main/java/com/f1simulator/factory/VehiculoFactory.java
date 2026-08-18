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

        // Factor de "intensidad" del vehículo: autos más veloces y con aceleración
        // más agresiva (menos segundos en 0-100) consumen y desgastan más.
        // Referencia base: 350 km/h y 2.5s de aceleración (valores típicos de F1).
        double factorVelocidad = velocidadMaximaKmh / 350.0;
        double factorAceleracion = 2.5 / aceleracion0100; // menos segundos -> factor más alto

        vehiculo.setRendimientoModo(ModoConduccion.NORMAL,
                calcularRendimiento(velocidadMaximaKmh, 0.85, 2.0, 1.5, factorVelocidad, factorAceleracion));
        vehiculo.setRendimientoModo(ModoConduccion.AGRESIVA,
                calcularRendimiento(velocidadMaximaKmh, 0.95, 2.5, 2.2, factorVelocidad, factorAceleracion));
        vehiculo.setRendimientoModo(ModoConduccion.AHORRO_COMBUSTIBLE,
                calcularRendimiento(velocidadMaximaKmh, 0.78, 1.6, 1.0, factorVelocidad, factorAceleracion));

        return vehiculo;
    }

    /**
     * @param factorVelocidadPromedio proporción de la velocidad máxima que se alcanza en promedio en ese modo
     * @param consumoBaseSeco         litros/vuelta de referencia en clima seco para ese modo
     * @param desgasteBaseSeco        desgaste de referencia en clima seco para ese modo
     * @param factorVelocidad         qué tan por encima/debajo está este auto del promedio de referencia (350 km/h)
     * @param factorAceleracion       qué tan por encima/debajo está este auto del promedio de referencia (2.5s)
     */
    private RendimientoPorModo calcularRendimiento(double velocidadMaximaKmh, double factorVelocidadPromedio,
                                                     double consumoBaseSeco, double desgasteBaseSeco,
                                                     double factorVelocidad, double factorAceleracion) {
        RendimientoPorModo r = new RendimientoPorModo(velocidadMaximaKmh * factorVelocidadPromedio);

        // El consumo y desgaste base se ajustan según qué tan veloz/agresivo es ESTE vehículo
        // en particular, en vez de ser un valor fijo igual para todos.
        double consumoAjustado = consumoBaseSeco * ((factorVelocidad + factorAceleracion) / 2.0);
        double desgasteAjustado = desgasteBaseSeco * ((factorVelocidad + factorAceleracion) / 2.0);

        r.setConsumo(TipoClima.SECO, consumoAjustado);
        r.setConsumo(TipoClima.LLUVIOSO, consumoAjustado * 1.10);
        r.setConsumo(TipoClima.EXTREMO, consumoAjustado * 1.25);

        r.setDesgaste(TipoClima.SECO, desgasteAjustado);
        r.setDesgaste(TipoClima.LLUVIOSO, desgasteAjustado * 0.55); // menos desgaste con lluvia moderada
        r.setDesgaste(TipoClima.EXTREMO, desgasteAjustado * 1.65);

        return r;
    }
}