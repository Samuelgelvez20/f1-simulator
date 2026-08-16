package com.f1simulator.model;

/**
 * Tipo de neumático montado en el vehículo. Determina si el auto está
 * preparado o no para el clima de la sesión (regla de penalización del 10%
 * que se implementa en la simulación).
*/

public enum TipoNeumatico {
    SECO,
    INTERMEDIO,
    LLUVIA
}