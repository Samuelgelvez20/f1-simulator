# F1 Simulator 🏎️

<p align="center">
  <img src="https://img.shields.io/badge/Status-Completado-success?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Version-v2.0.0-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaFX-21-007396?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Concurrency-Multithreading-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" />
</p>

Simulador de clasificación y telemetría de Fórmula 1 desarrollado en **Java 21**, con motor de **concurrencia multihilo** (`ExecutorService` y `Callable` en paralelo, 1 hilo por monoplaza), interfaz gráfica reactiva y deportiva construida con **JavaFX 21** (estética Carbon Black & F1 Racing Red), y persistencia de resultados y setups en **PostgreSQL**.

Desarrollado por — **Samuel David Gelvez Rodriguez** y **Maria Fernanda Quiñonez**.

---

## 📌 Descripción del Proyecto

El sistema permite gestionar escuderías, pilotos, bólidos y circuitos oficiales del Campeonato Mundial de Fórmula 1, configurar setups de telemetría (modo de conducción, downforce aerodinámico, presión de neumáticos y estrategia de combustible) y simular sesiones de clasificación en tiempo real:

1. **Simulación Concurrente:** Todos los bólidos corren su vuelta de clasificación simultáneamente en paralelo a través de un pool de subprocesos.
2. **Dinámica de Clima y Neumáticos:** Variación meteorológica (`SECO`, `LLUVIOSO`, `EXTREMO`), aplicando un **10% de penalización por vuelta** si el monoplaza monta compuestos inadecuados para el asfalto.
3. **Persistencia en PostgreSQL:** Almacenamiento seguro de historiales de clasificación y configuraciones de telemetría por piloto y sesión.
4. **Dashboard Deportivo Moderno:** Interfaz JavaFX 21 con tabla de tiempos estilo Formula 1 (Pole Position purple timing, sector gaps, delta times y telemetría).

---

## ✨ Funcionalidades Principales

- **Simulación Multihilo de Clasificación:** Cada piloto es ejecutado en su propio hilo concurrente (`TareaPiloto`), sincronizando tiempos sin bloqueos de interfaz mediante `CompletableFuture` y `Platform.runLater`.
- **Fábrica de Vehículos (`VehiculoFactory`):** Cálculo automatizado de velocidad punta, aceleración y curvas de rendimiento por modo de pilotaje.
- **Telemetría & Setup:** Ajuste de alerones, presión de neumáticos y estrategias de motor guardadas directamente en PostgreSQL.
- **Seeder Oficial de F1:** Precarga inicial de circuitos legendarios (Monaco, Silverstone, Spa-Francorchamps, Monza) y escuderías oficiales (Red Bull, Ferrari, Mercedes, McLaren).
- **Historial Completo en Base de Datos:** Consulta de tiempos históricos, pole positions y comparativas entre pilotos.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 (LTS)
- **Interfaz Gráfica:** JavaFX 21 (`javafx-controls`, `javafx-fxml`, `javafx-graphics`)
- **Gestor de Construcción:** Apache Maven con `maven-shade-plugin` para empaquetado Fat JAR
- **Concurrencia:** `java.util.concurrent` (`ExecutorService`, `Callable`, `synchronized`, `invokeAll`)
- **Base de Datos:** PostgreSQL 16 (vía JDBC con `PreparedStatement`)

---

## 🏗️ Arquitectura de Software

```
com.f1simulator
 ├── concurrency     → TareaPiloto (Runnable/Callable, simulación en paralelo por piloto)
 ├── factory         → VehiculoFactory (cálculo de rendimiento dinámico)
 ├── model           → Circuito, Piloto, Equipo, Vehiculo, ConfiguracionVehiculo, ResultadoClasificacion...
 ├── persistence     → ConexionPostgreSQL (Singleton) + Repositorios SQL
 ├── repository      → Repositorios en memoria para entidades base
 ├── service         → SimulacionService, F1DataSeeder, ParticipanteSimulacion
 ├── ui.javafx       → F1App, F1DashboardView (interfaz gráfica completa en JavaFX)
 └── F1Launcher.java → Bootstrap universal compatible con módulos JavaFX
```

---

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos Previos
- Java 21 o superior
- Maven 3.8+
- Servidor PostgreSQL activo

### 1. Base de Datos PostgreSQL
Crea la base de datos `f1simulator` y ejecuta el script de inicialización:
```bash
psql -U postgres -d f1simulator -f src/main/resources/schema.sql
```

### 2. Compilación y Empaquetado
```bash
mvn clean package -DskipTests
```

### 3. Ejecución de la Aplicación
```bash
java -jar target/f1-simulator.jar
```
*O ejecutando directamente con el plugin de JavaFX:*
```bash
mvn javafx:run
```

---

## 👥 Autores y Flujo de Trabajo

- **Samuel David Gelvez Rodriguez** & **Maria Fernanda Quiñonez**.
- **Flujo de Trabajo:** Git Flow con commits bajo la especificación [Conventional Commits](https://www.conventionalcommits.org/).

---

## 📄 Licencia

Este proyecto está bajo la Licencia **MIT** — consulta el archivo [LICENSE](LICENSE) para más detalles.
