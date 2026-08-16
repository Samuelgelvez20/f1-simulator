package com.f1simulator.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para repositorios de entidades.
 * @param <T>  tipo de la entidad
 * @param <ID> tipo de la clave (id numérico o nombre, según la entidad)
*/

public interface Repository<T, ID> {

    T guardar(T entidad);

    Optional<T> buscarPorId(ID id);

    List<T> listarTodos();

    boolean actualizar(ID id, T entidadActualizada);

    boolean eliminar(ID id);
}