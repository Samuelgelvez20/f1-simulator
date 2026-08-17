package com.f1simulator.repository;

import com.f1simulator.model.Equipo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EquipoRepository implements Repository<Equipo, String> {

    private final Map<String, Equipo> equipos = new HashMap<>();

    @Override
    public Equipo guardar(Equipo equipo) {
        equipos.put(equipo.getNombre(), equipo);
        return equipo;
    }

    @Override
    public Optional<Equipo> buscarPorId(String nombre) {
        return Optional.ofNullable(equipos.get(nombre));
    }

    @Override
    public List<Equipo> listarTodos() {
        return new ArrayList<>(equipos.values());
    }

    @Override
    public boolean actualizar(String nombre, Equipo equipoActualizado) {
        if (equipos.containsKey(nombre)) {
            equipos.put(nombre, equipoActualizado);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(String nombre) {
        if (equipos.containsKey(nombre)) {
            equipos.remove(nombre);
            return true;
        }
        return false;
    }
}
