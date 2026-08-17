package com.f1simulator.repository;

import com.f1simulator.model.ConfiguracionVehiculo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repositorio en memoria del historial de configuraciones de vehículo.
 * En el Día 3 esto se reemplaza (o se complementa) con persistencia real en
 * PostgreSQL, según lo acordado: el enunciado exige que las configuraciones
 * se puedan revisar después, y un HashMap no sobrevive a cerrar la app.
 * Por ahora, en memoria, para no bloquear el desarrollo de la funcionalidad.
 */
public class ConfiguracionRepository implements Repository<ConfiguracionVehiculo, Integer> {

    private final Map<Integer, ConfiguracionVehiculo> configuraciones = new HashMap<>();
    private final AtomicInteger secuenciaId = new AtomicInteger(1);

    @Override
    public ConfiguracionVehiculo guardar(ConfiguracionVehiculo config) {
        config.setId(secuenciaId.getAndIncrement());
        configuraciones.put(config.getId(), config);
        return config;
    }

    @Override
    public Optional<ConfiguracionVehiculo> buscarPorId(Integer id) {
        return Optional.ofNullable(configuraciones.get(id));
    }

    @Override
    public List<ConfiguracionVehiculo> listarTodos() {
        return new ArrayList<>(configuraciones.values());
    }

    @Override
    public boolean actualizar(Integer id, ConfiguracionVehiculo actualizado) {
        if (!configuraciones.containsKey(id)) return false;
        actualizado.setId(id);
        configuraciones.put(id, actualizado);
        return true;
    }

    @Override
    public boolean eliminar(Integer id) {
        return configuraciones.remove(id) != null;
    }

    /** Historial de configuraciones aplicadas a un vehículo específico (historia de usuario). */
    public List<ConfiguracionVehiculo> historialPorVehiculo(int vehiculoId) {
        List<ConfiguracionVehiculo> resultado = new ArrayList<>();
        for (ConfiguracionVehiculo c : configuraciones.values()) {
            if (c.getVehiculoId() == vehiculoId) resultado.add(c);
        }
        return resultado;
    }

    /** Historial de configuraciones aplicadas por un piloto específico. */
    public List<ConfiguracionVehiculo> historialPorPiloto(int pilotoId) {
        List<ConfiguracionVehiculo> resultado = new ArrayList<>();
        for (ConfiguracionVehiculo c : configuraciones.values()) {
            if (c.getPilotoId() == pilotoId) resultado.add(c);
        }
        return resultado;
    }
}