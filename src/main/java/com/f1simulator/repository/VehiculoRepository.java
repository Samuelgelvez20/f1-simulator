package com.f1simulator.repository;

import com.f1simulator.model.Vehiculo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repositorio en memoria de Vehículos, usando HashMap (según el enunciado
 * del proyecto: "Gestión de Datos" -> Map, HashMap para persistencia temporal).
 */
public class VehiculoRepository implements Repository<Vehiculo, Integer> {

    private final Map<Integer, Vehiculo> vehiculos = new HashMap<>();
    private final AtomicInteger secuenciaId = new AtomicInteger(1);

    @Override
    public Vehiculo guardar(Vehiculo vehiculo) {
        if (vehiculo.getId() == 0) {
            vehiculo.setId(secuenciaId.getAndIncrement());
        }
        vehiculos.put(vehiculo.getId(), vehiculo);
        return vehiculo;
    }

    @Override
    public Optional<Vehiculo> buscarPorId(Integer id) {
        return Optional.ofNullable(vehiculos.get(id));
    }

    @Override
    public List<Vehiculo> listarTodos() {
        return new ArrayList<>(vehiculos.values());
    }

    @Override
    public boolean actualizar(Integer id, Vehiculo actualizado) {
        if (!vehiculos.containsKey(id)) return false;
        actualizado.setId(id);
        vehiculos.put(id, actualizado);
        return true;
    }

    @Override
    public boolean eliminar(Integer id) {
        return vehiculos.remove(id) != null;
    }

    /** Búsqueda por características: modelo, motor o equipo (historia de usuario). */
    public List<Vehiculo> buscarPorCaracteristica(String texto) {
        String filtro = texto.toLowerCase();
        List<Vehiculo> resultado = new ArrayList<>();
        for (Vehiculo v : vehiculos.values()) {
            if (v.getModelo().toLowerCase().contains(filtro)
                    || v.getMotor().toLowerCase().contains(filtro)
                    || v.getEquipoNombre().toLowerCase().contains(filtro)) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    /** Vehículos de un equipo específico (lo vamos a usar en el Paso de asignación piloto-vehículo, Día 2). */
    public List<Vehiculo> listarPorEquipo(String equipoNombre) {
        List<Vehiculo> resultado = new ArrayList<>();
        for (Vehiculo v : vehiculos.values()) {
            if (v.getEquipoNombre().equalsIgnoreCase(equipoNombre)) resultado.add(v);
        }
        return resultado;
    }

    /** Vehículo asignado a un piloto (según Vehiculo.pilotoIds), si existe. Lo usa la simulación (Día 3). */
    public Optional<Vehiculo> buscarPorPiloto(int pilotoId) {
        for (Vehiculo v : vehiculos.values()) {
            if (v.getPilotoIds().contains(pilotoId)) return Optional.of(v);
        }
        return Optional.empty();
    }
}