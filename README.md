# F1 Simulator 🏎️

Simulador de clasificación de Fórmula 1 desarrollado en Java, con concurrencia real (un hilo por piloto), interfaz gráfica `JOptionPane` y persistencia parcial en PostgreSQL.

Desarrollado por — **Samuel David Gelvez Rodriguez** y **Maria Fernanda Quiñonez**.

## 📌 Descripción

El sistema permite gestionar circuitos, pilotos, equipos y vehículos de F1, configurar cada vehículo (modo de conducción, carga aerodinámica, presión de neumáticos, estrategia de combustible), y simular una sesión de clasificación completa: todos los pilotos registrados corren su vuelta rápida **en paralelo**, bajo un clima aleatorio generado para la sesión, con penalización si el neumático montado no es adecuado para ese clima. Los resultados se ordenan, se muestran en una tabla, y quedan guardados para poder compararlos después.

## ✨ Funcionalidades

- **CRUD de Circuitos, Pilotos, Equipos y Vehículos**, con búsquedas y detalle de cada entidad.
- **Fábrica de vehículos** (`VehiculoFactory`) que calcula el rendimiento por modo de conducción según el equipo/motor.
- **Asignación de pilotos a vehículos** por equipo.
- **Comparación de vehículos** (velocidad, consumo, desgaste).
- **Configuración de vehículo**: modo de conducción, carga aerodinámica, presión de neumáticos, estrategia de combustible.
- **Simulación de clasificación**: clima aleatorio por sesión, cálculo concurrente del tiempo de vuelta de cada piloto, penalización del 10% por neumático inadecuado, clasificación final ordenada y mostrada en `JTable`.
- **Historial y comparación**: resultados de clasificación por circuito, e historial de configuraciones guardadas — ambos consultados desde PostgreSQL.

## 🌦️ Regla de clima y neumáticos

Si el vehículo no tiene montado un neumático adecuado para el clima de la sesión (`LLUVIOSO` o `EXTREMO` sin llantas de lluvia), su tiempo de vuelta recibe una **penalización del 10%**.

## 🛠️ Tecnologías

- **Java 17**, gestionado con **Maven**
- **Swing / JOptionPane** (interfaz gráfica) + `JTable`/`JScrollPane` para tablas
- **PostgreSQL** (vía **JDBC**, `PreparedStatement`) para persistencia
- **Docker Compose** para levantar PostgreSQL localmente
- `java.util.concurrent`: `ExecutorService`, `Runnable`, `synchronized`, `invokeAll`

## 🏗️ Arquitectura

```
com.f1simulator
 ├── model         → Piloto, Equipo, Vehiculo, Circuito, ConfiguracionVehiculo, ResultadoClasificacion...
 ├── factory        → VehiculoFactory (patrón Factory)
 ├── repository     → Repositorios en memoria (HashMap): Circuito, Piloto, Equipo, Vehiculo
 ├── persistence     → ConexionPostgreSQL (patrón Singleton) + repos SQL de Resultados y Configuraciones
 ├── service         → SimulacionService, ImpactoCircuitoService, ParticipanteSimulacion
 ├── concurrency     → TareaPiloto (Runnable, una instancia por piloto en la simulación)
 ├── ui              → Menús JOptionPane por módulo (Circuitos, Pilotos, Equipos, Vehículos, Configuración, Simulación, Reportes)
 └── Main.java       → Punto de entrada, conecta todos los módulos
```

### Concurrencia en la simulación

Cada piloto se simula en su propio hilo (`TareaPiloto implements Runnable`), coordinados por un `ExecutorService` (`Executors.newFixedThreadPool`). La lista de resultados es compartida entre todos los hilos, así que el acceso a ella está protegido con `synchronized` para evitar condiciones de carrera al escribir al mismo tiempo. `invokeAll()` bloquea la ejecución hasta que **todos** los pilotos terminaron de calcular su tiempo, y solo entonces se ordena la clasificación final.

## 💾 Persistencia: qué quedó en PostgreSQL y qué en memoria

**Decisión original** (y la que sigue vigente en el proyecto final): el enunciado pide explícitamente `HashMap`/`Map` como almacenamiento de Circuitos, Pilotos, Equipos y Vehículos, y pide base de datos real solo para los resultados de clasificación y el historial de configuraciones — por eso la arquitectura se dividió así desde el Día 1.

| Entidad | Almacenamiento |
|---|---|
| Circuitos, Pilotos, Equipos, Vehículos | `HashMap` en memoria (se pierden al cerrar la app) |
| `resultados_clasificacion` | **PostgreSQL** |
| `historial_configuraciones` | **PostgreSQL** |

**Intento de migración completa**: se abrió la rama `feature/migracion-postgresql-completa` con la intención de mover también Circuitos/Pilotos/Equipos/Vehículos a PostgreSQL, pero **no se alcanzó a completar por falta de tiempo** dentro del plazo de 3 días — la rama quedó sin avance funcional y no se mergeó. El sistema entrega exactamente lo que pedía el enunciado (persistencia real donde era obligatoria), con esa migración adicional como trabajo futuro pendiente.

### Limitaciones conocidas

- Circuitos, Pilotos, Equipos y Vehículos **no persisten** entre ejecuciones — cada vez que se cierra el programa, hay que volver a registrarlos.
- No hay una validación mínima de "equipos completos" (por ejemplo, al menos 2 equipos con 2 pilotos cada uno) antes de lanzar la simulación completa; si faltan pilotos con vehículo asignado, simplemente se excluyen de la clasificación con un aviso.

## 🔀 Flujo de Git usado

GitFlow liviano con 2 personas: una rama larga por persona/frente de trabajo (`feature/samuel-vehiculos-simulacion`, `feature/mafe-circuitos-pilotos-equipos`, `feature/persistencia-postgresql`), mergeando frecuentemente a `develop` en vez de acumular al final. `main` solo recibe merges desde `develop` al cerrar el proyecto. Commits siguiendo **Conventional Commits** (`feat:`, `fix:`, `chore:`) en todo el historial.

## 🚀 Cómo ejecutar el proyecto

### Requisitos
- JDK 17+
- Maven
- Docker (para levantar PostgreSQL) — o una instancia propia de PostgreSQL

### 1. Levantar PostgreSQL

```bash
cp .env.example .env
# edita .env si quieres cambiar usuario/clave/puerto
docker compose up -d
```

### 2. Ejecutar el esquema SQL

Ejecuta el script `src/main/resources/schema.sql` contra la base de datos levantada (crea las tablas `resultados_clasificacion` e `historial_configuraciones`).

### 3. Compilar y correr

```bash
mvn compile
mvn exec:java
```

## 🖥️ Módulos del menú principal

1. Circuitos
2. Equipos
3. Pilotos
4. Vehículos
5. Configuración
6. Simular Clasificación
7. Historial y Comparación
8. Salir

## 📦 Control de versiones

Convención de **[Conventional Commits](https://www.conventionalcommits.org/)**: `feat:` nueva funcionalidad, `fix:` corrección de errores, `chore:` tareas de configuración/mantenimiento.

## 👥 Autores

- Samuel David Gelvez Rodriguez
- Maria Fernanda Quiñonez
