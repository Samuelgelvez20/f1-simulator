package com.f1simulator.repository;

import com.f1simulator.model.Circuito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitoRepository implements Repository<Circuito, Integer> {

    private final Map<Integer, Circuito> circuitos = new HashMap<>();
    private final AtomicInteger secuenciaId = new AtomicInteger(1);

    @Override
    public Circuito guardar(Circuito circuito) {
        if (circuito.getId() == 0) {
            circuito.setId(secuenciaId.getAndIncrement());
        }
        circuitos.put(circuito.getId(), circuito);
        return circuito;
    }

    @Override
    public Optional<Circuito> buscarPorId(Integer id) {
        return Optional.ofNullable(circuitos.get(id));
    }

    @Override
    public List<Circuito> listarTodos() {
        return new ArrayList<>(circuitos.values());
    }

    @Override
    public boolean actualizar(Integer id, Circuito circuitoActualizado) {
        if (circuitos.containsKey(id)) {
            circuitoActualizado.setId(id);
            circuitos.put(id, circuitoActualizado);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        if (circuitos.containsKey(id)) {
            circuitos.remove(id);
            return true;
        }
        return false;
    }
}