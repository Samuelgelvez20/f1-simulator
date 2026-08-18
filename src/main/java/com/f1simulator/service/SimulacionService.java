package com.f1simulator.service;

import com.f1simulator.concurrency.TareaPiloto;
import com.f1simulator.model.Circuito;
import com.f1simulator.model.ResultadoClasificacion;
import com.f1simulator.model.TipoClima;
import com.f1simulator.persistence.ResultadoClasificacionSqlRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordina una sesión de simulación de clasificación: genera el clima
 * aleatorio, lanza un hilo (Runnable) por piloto a través de un
 * ExecutorService, espera a que todos terminen con invokeAll, ordena los
 * resultados por tiempo de vuelta y los persiste en PostgreSQL.
 */
public class SimulacionService {

    // Agrupa todos los resultados de una misma corrida de simulación (sesion_id).
    private static final AtomicInteger contadorSesiones = new AtomicInteger(1);

    private final ResultadoClasificacionSqlRepository resultadoRepo;

    public SimulacionService(ResultadoClasificacionSqlRepository resultadoRepo) {
        this.resultadoRepo = resultadoRepo;
    }

    /** Genera un clima aleatorio para la sesión (historia de usuario "condiciones climáticas aleatorias"). */
    public TipoClima generarClimaAleatorio() {
        TipoClima[] climas = TipoClima.values();
        return climas[ThreadLocalRandom.current().nextInt(climas.length)];
    }

    public List<ResultadoClasificacion> ejecutarSimulacion(Circuito circuito, TipoClima clima, List<ParticipanteSimulacion> participantes)
            throws InterruptedException {

        int sesionId = contadorSesiones.getAndIncrement();
        List<ResultadoClasificacion> resultadosCompartidos = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(participantes.size());
        try {
            List<Callable<Object>> tareas = new ArrayList<>();
            for (ParticipanteSimulacion p : participantes) {
                Runnable tarea = new TareaPiloto(p.getPiloto(), p.getVehiculo(), p.getModo(),
                        circuito, clima, sesionId, resultadosCompartidos);
                tareas.add(Executors.callable(tarea));
            }

            executor.invokeAll(tareas); // espera a que TODOS los hilos terminen antes de continuar
        } finally {
            executor.shutdown();
        }

        // Ordenar por tiempo de vuelta ascendente y asignar posiciones (pole position = posición 1)
        List<ResultadoClasificacion> ordenados = new ArrayList<>(resultadosCompartidos);
        ordenados.sort(Comparator.comparingDouble(ResultadoClasificacion::getTiempoVueltaSegundos));
        for (int i = 0; i < ordenados.size(); i++) {
            ordenados.get(i).setPosicion(i + 1);
        }

        // Persistencia de datos de clasificación (historia de usuario, en PostgreSQL)
        for (ResultadoClasificacion r : ordenados) {
            resultadoRepo.guardar(r);
        }

        return ordenados;
    }
}