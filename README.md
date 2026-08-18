# 🏎️ Simulador de Fórmula 1

Bienvenido al **Simulador de Fórmula 1**, un proyecto interactivo desarrollado en Java que permite gestionar circuitos, escuderías, pilotos y vehículos, así como simular sesiones de clasificación completas bajo condiciones climáticas variables.

Este proyecto fue construido como un sprint intensivo de 3 días enfocado en aplicar conceptos avanzados de Programación Orientada a Objetos (POO), Concurrencia y Persistencia de Datos.

## 🚀 Características Principales

- **Gestión (CRUD) en Memoria:** Administración de Circuitos, Equipos, Pilotos y Vehículos utilizando `HashMap` para una ejecución rápida.
- **Configuración Estratégica:** Ajuste de modos de conducción, carga aerodinámica, presión de neumáticos y estrategias de combustible antes de salir a la pista.
- **Sistema de Clima Dinámico:** El clima (Seco, Lluvioso, Extremo) se genera aleatoriamente por sesión y afecta directamente el consumo de combustible, el desgaste de neumáticos y el tiempo de vuelta. Incluye penalizaciones estrictas del 10% por usar neumáticos incorrectos bajo lluvia.
- **Simulación Concurrente:** Cálculo de tiempos de vuelta simultáneos para todos los pilotos en pista utilizando `Runnable` y `ExecutorService`.
- **Persistencia de Datos SQL:** Almacenamiento permanente del historial de configuraciones y resultados de clasificación en PostgreSQL, haciendo uso de estructuras `JSONB`.
- **Interfaz Gráfica Interactiva:** Construida integralmente con `JOptionPane`, integrando componentes como `JTable` y `JScrollPane` para la visualización de listas y clasificaciones.

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje Principal:** Java
- **Base de Datos:** PostgreSQL (mediante JDBC)
- **Gestión de Proyecto:** Maven
- **Patrones de Diseño Aplicados:**
  - *Singleton*: Para centralizar la conexión a la base de datos (`ConexionPostgreSQL`).
  - *Factory*: Para la instanciación de vehículos según el equipo y motor (`VehiculoFactory`).
  - *Repository*: Separación del acceso a datos, implementado tanto para memoria (`HashMap`) como para SQL (`PreparedStatement`).
- **Control de Versiones:** Git & GitHub mediante un flujo de trabajo ágil basado en ramas de características.

## 📋 Requisitos Previos

- [Java JDK 17 o superior](https://www.oracle.com/java/technologies/javase-downloads.html)
- [PostgreSQL](https://www.postgresql.org/) (En ejecución local o vía contenedor Docker)
- IDE recomendado: Visual Studio Code, IntelliJ IDEA o Eclipse.

## ⚙️ Instalación y Configuración

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Samuelgelvez20/f1-simulator.git
   cd f1-simulator
   ```

2. **Configurar la Base de Datos:**
   - Asegúrate de tener PostgreSQL ejecutándose.
   - Ejecuta el script SQL incluido en `src/main/resources/schema.sql` para generar las tablas `resultados_clasificacion` e `historial_configuraciones`.
   - Modifica las credenciales de conexión (usuario y contraseña) dentro de tu clase `ConexionPostgreSQL.java` o en tu archivo `.env` para que coincidan con las de tu equipo local.

3. **Compilar y Ejecutar:**
   Si utilizas Maven por terminal:
   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="com.f1simulator.Main"
   ```
   *Nota: También puedes simplemente ejecutar el método `main` de la clase `com.f1simulator.Main` directamente desde la interfaz de tu IDE.*

## 👥 Equipo de Desarrollo

- **María Fernanda Quiñonez Moreno** - Modelo de Circuitos/Pilotos/Equipos, Cálculos lógicos, y Persistencia SQL/UI Reportes.
- **Samuel Gélvez** - Modelo de Vehículos, Patrones de creación, Concurrencia y Lógica de Simulación de Clasificación.

---
*Proyecto de carácter académico para el análisis y diseño de sistemas interactivos en Java.*
