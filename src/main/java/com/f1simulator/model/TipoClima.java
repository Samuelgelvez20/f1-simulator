package com.f1simulator.model;

/**
 * Condición climática de una sesión. La usa tanto Circuito (clima promedio,
 * lo maneja Mafe) como Vehiculo (rendimiento por clima, lo maneja Samuel).
*/

public enum TipoClima {
    SECO,
    LLUVIOSO,
    EXTREMO
}