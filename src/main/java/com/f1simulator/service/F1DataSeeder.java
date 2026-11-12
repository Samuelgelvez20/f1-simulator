package com.f1simulator.service;

import com.f1simulator.factory.VehiculoFactory;
import com.f1simulator.model.*;
import com.f1simulator.repository.*;

/**
 * Precarga de datos oficiales de Fórmula 1 para circuitos, escuderías, pilotos y vehículos.
 * Permite que el simulador esté listo para correr desde el primer segundo.
 */
public class F1DataSeeder {

    public static void seedIfEmpty(CircuitoRepository circuitoRepo, EquipoRepository equipoRepo,
                                  PilotoRepository pilotoRepo, VehiculoRepository vehiculoRepo) {
        if (!circuitoRepo.listarTodos().isEmpty()) {
            return;
        }

        // 1. Circuitos de Fórmula 1
        Circuito monaco = new Circuito(0, "Circuit de Monaco", "Mónaco", 3.337, 78,
                "Trazado urbano legendario y estrecho, máxima precisión técnica", TipoClima.SECO);
        Circuito silverstone = new Circuito(0, "Silverstone Circuit", "Reino Unido", 5.891, 52,
                "La cuna del automovilismo, curvas rápidas Maggotts y Becketts", TipoClima.SECO);
        Circuito spa = new Circuito(0, "Circuit de Spa-Francorchamps", "Bélgica", 7.004, 44,
                "El mítico trazado en las Ardenas con Eau Rouge y clima impredecible", TipoClima.LLUVIOSO);
        Circuito monza = new Circuito(0, "Autodromo Nazionale Monza", "Italia", 5.793, 53,
                "El Templo de la Velocidad, rectas extensas y curvas parabólicas", TipoClima.SECO);

        circuitoRepo.guardar(monaco);
        circuitoRepo.guardar(silverstone);
        circuitoRepo.guardar(spa);
        circuitoRepo.guardar(monza);

        // 2. Equipos
        Equipo redBull = new Equipo("Red Bull Racing", "Austria", "Honda RBPT");
        Equipo ferrari = new Equipo("Scuderia Ferrari", "Italia", "Ferrari");
        Equipo mercedes = new Equipo("Mercedes-AMG Petronas", "Alemania", "Mercedes");
        Equipo mclaren = new Equipo("McLaren F1 Team", "Reino Unido", "Mercedes");

        equipoRepo.guardar(redBull);
        equipoRepo.guardar(ferrari);
        equipoRepo.guardar(mercedes);
        equipoRepo.guardar(mclaren);

        // 3. Pilotos
        Piloto verstappen = new Piloto(0, "Max Verstappen", redBull, "Líder");
        Piloto perez = new Piloto(0, "Sergio Pérez", redBull, "Escudero");
        Piloto leclerc = new Piloto(0, "Charles Leclerc", ferrari, "Líder");
        Piloto sainz = new Piloto(0, "Carlos Sainz", ferrari, "Escudero");
        Piloto hamilton = new Piloto(0, "Lewis Hamilton", mercedes, "Líder");
        Piloto russell = new Piloto(0, "George Russell", mercedes, "Escudero");
        Piloto norris = new Piloto(0, "Lando Norris", mclaren, "Líder");
        Piloto piastri = new Piloto(0, "Oscar Piastri", mclaren, "Escudero");

        pilotoRepo.guardar(verstappen);
        pilotoRepo.guardar(perez);
        pilotoRepo.guardar(leclerc);
        pilotoRepo.guardar(sainz);
        pilotoRepo.guardar(hamilton);
        pilotoRepo.guardar(russell);
        pilotoRepo.guardar(norris);
        pilotoRepo.guardar(piastri);

        redBull.agregarPiloto(verstappen);
        redBull.agregarPiloto(perez);
        ferrari.agregarPiloto(leclerc);
        ferrari.agregarPiloto(sainz);
        mercedes.agregarPiloto(hamilton);
        mercedes.agregarPiloto(russell);
        mclaren.agregarPiloto(norris);
        mclaren.agregarPiloto(piastri);

        // 4. Vehículos con Factory y asignación
        VehiculoFactory factory = new VehiculoFactory();

        Vehiculo rb20 = factory.crearVehiculo("Red Bull Racing", "RB20", "Honda RBPT", 355.0, 2.4, TipoNeumatico.SECO);
        rb20.asignarPiloto(verstappen.getId());
        rb20.asignarPiloto(perez.getId());
        vehiculoRepo.guardar(rb20);

        Vehiculo sf24 = factory.crearVehiculo("Scuderia Ferrari", "SF-24", "Ferrari", 353.0, 2.45, TipoNeumatico.SECO);
        sf24.asignarPiloto(leclerc.getId());
        sf24.asignarPiloto(sainz.getId());
        vehiculoRepo.guardar(sf24);

        Vehiculo w15 = factory.crearVehiculo("Mercedes-AMG Petronas", "W15", "Mercedes", 350.0, 2.5, TipoNeumatico.INTERMEDIO);
        w15.asignarPiloto(hamilton.getId());
        w15.asignarPiloto(russell.getId());
        vehiculoRepo.guardar(w15);

        Vehiculo mcl38 = factory.crearVehiculo("McLaren F1 Team", "MCL38", "Mercedes", 352.0, 2.48, TipoNeumatico.SECO);
        mcl38.asignarPiloto(norris.getId());
        mcl38.asignarPiloto(piastri.getId());
        vehiculoRepo.guardar(mcl38);
    }
}
