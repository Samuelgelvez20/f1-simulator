package com.f1simulator.repository;

import com.f1simulator.model.Circuito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CircuitoRepository implements Repository<Circuito, String> {

    private final Map<String, Circuito> circuitos = new HashMap<>();

    @Override
    public Circuito guardar(Circuito circuito) {
        circuitos.put(circuito.getId(), circuito);
        return circuito;
    }

    @Override
    public Optional<Circuito> buscarPorId(String id) {
        return Optional.ofNullable(circuitos.get(id));
    }

    @Override
    public List<Circuito> listarTodos() {
        return new ArrayList<>(circuitos.values());
    }

    @Override
    public boolean actualizar(String id, Circuito circuitoActualizado) {
        if (circuitos.containsKey(id)) {
            circuitos.put(id, circuitoActualizado);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(String id) {
        if (circuitos.containsKey(id)) {
            circuitos.remove(id);
            return true;
        }
        return false;
    }
}
