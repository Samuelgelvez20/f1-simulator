package com.f1simulator.concurrency;

import com.f1simulator.model.Circuito;
import com.f1simulator.model.ModoConduccion;
import com.f1simulator.model.Piloto;
import com.f1simulator.model.RendimientoPorModo;
import com.f1simulator.model.ResultadoClasificacion;
import com.f1simulator.model.TipoClima;
import com.f1simulator.model.Vehiculo;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tarea de simulación para UN piloto: calcula su tiempo de vuelta según
 * vehículo, modo de conducción, circuito y clima de la sesión, y agrega el
 * resultado a la lista compartida.
 *
 * Se ejecuta en paralelo (un hilo por piloto), coordinada por
 * SimulacionService a través de un ExecutorService. El acceso a la lista
 * compartida está protegido con `synchronized` para evitar condiciones de
 * carrera al escribir resultados simultáneamente desde varios hilos.
 */
public class TareaPiloto implements Runnable {

    private final Piloto piloto;
    private final Vehiculo vehiculo;
    private final ModoConduccion modo;
    private final Circuito circuito;
    private final TipoClima clima;
    private final int sesionId;
    private final List<ResultadoClasificacion> resultadosCompartidos;

    public TareaPiloto(Piloto piloto, Vehiculo vehiculo, ModoConduccion modo, Circuito circuito,
                        TipoClima clima, int sesionId, List<ResultadoClasificacion> resultadosCompartidos) {
        this.piloto = piloto;
        this.vehiculo = vehiculo;
        this.modo = modo;
        this.circuito = circuito;
        this.clima = clima;
        this.sesionId = sesionId;
        this.resultadosCompartidos = resultadosCompartidos;
    }

    @Override
    public void run() {
        double tiempoVuelta = calcularTiempoVuelta();
        ResultadoClasificacion resultado = new ResultadoClasificacion(
                sesionId, piloto.getId(), piloto.getNombre(), circuito.getId(), tiempoVuelta, clima);

        synchronized (resultadosCompartidos) {
            resultadosCompartidos.add(resultado);
        }
    }

    private double calcularTiempoVuelta() {
        RendimientoPorModo rendimiento = vehiculo.getRendimiento().get(modo);
        double velocidadEfectivaKmh = (rendimiento != null)
                ? rendimiento.getVelocidadPromedioKmh()
                : vehiculo.getVelocidadMaximaKmh() * 0.85; // respaldo si el modo no tiene datos calculados

        // Tiempo base: distancia de una vuelta / velocidad promedio del vehículo en ese modo y clima
        double tiempoBaseSegundos = (circuito.getLongitudKm() / velocidadEfectivaKmh) * 3600.0;

        // Ajuste por aceleración del vehículo (referencia: 2.5s en 0-100, valor típico de F1)
        double factorAceleracion = vehiculo.getAceleracion0100() / 2.5;
        tiempoBaseSegundos *= (0.9 + 0.1 * factorAceleracion);

        // Regla de clima/neumáticos (Día 3): penalización del 10% si el vehículo no
        // tiene montado un neumático adecuado para el clima de la sesión
        if (!vehiculo.tieneNeumaticoAdecuadoPara(clima)) {
            tiempoBaseSegundos *= 1.10;
        }

        // Pequeña variación por piloto: los líderes son más consistentes (menor
        // variación) que los escuderos, simulando la diferencia de experiencia
        boolean esLider = "Líder".equalsIgnoreCase(piloto.getRol());
        double variacion = esLider
                ? ThreadLocalRandom.current().nextDouble(-0.4, 0.3)
                : ThreadLocalRandom.current().nextDouble(-0.3, 0.6);

        return Math.max(1.0, tiempoBaseSegundos + variacion);
    }
}