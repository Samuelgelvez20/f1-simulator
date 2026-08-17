package com.f1simulator.repository;

import com.f1simulator.model.Piloto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PilotoRepository implements Repository<Piloto, Integer> {

    private final Map<Integer, Piloto> pilotos = new HashMap<>();

    @Override
    public Piloto guardar(Piloto piloto) {
        pilotos.put(piloto.getId(), piloto);
        return piloto;
    }

    @Override
    public Optional<Piloto> buscarPorId(Integer id) {
        return Optional.ofNullable(pilotos.get(id));
    }

    @Override
    public List<Piloto> listarTodos() {
        return new ArrayList<>(pilotos.values());
    }

    @Override
    public boolean actualizar(Integer id, Piloto pilotoActualizado) {
        if (pilotos.containsKey(id)) {
            pilotos.put(id, pilotoActualizado);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        if (pilotos.containsKey(id)) {
            pilotos.remove(id);
            return true;
        }
        return false;
    }
}
