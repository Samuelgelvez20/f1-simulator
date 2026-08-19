# f1-simulator

Simulador de Fórmula 1 en Java 17 + Swing (FlatLaf), con persistencia en PostgreSQL 16 vía Docker.

## Requisitos

- JDK 17+
- Maven 3.8+
- Docker y Docker Compose

## Configuración inicial

1. Copia el archivo de ejemplo y edita los valores:

   ```bash
   cp .env.example .env
   ```

2. Levanta la base de datos:

   ```bash
   docker compose up -d
   ```

   La primera vez crea el usuario, la base de datos y el volumen `f1-postgres-data`.
   Los datos persisten aunque se detenga el contenedor.

3. Exporta las variables del `.env` en tu terminal (Linux/Mac):

   ```bash
   set -a
   source .env
   set +a
   ```

   O desde VS Code: la configuración de depuración `.vscode/launch.json` ya carga
   el `.env` automáticamente (`envFile`).

## Ejecutar

```bash
mvn compile exec:java
```

## Variables de entorno

| Variable          | Descripción                          | Default       |
|-------------------|--------------------------------------|---------------|
| `POSTGRES_DB`     | Nombre de la base de datos           | `f1simulator` |
| `POSTGRES_USER`   | Usuario de PostgreSQL                | `f1user`      |
| `POSTGRES_PASSWORD` | Contraseña del usuario             | *(requerida)* |
| `POSTGRES_PORT`   | Puerto publicado de PostgreSQL       | `5432`        |

## Seguridad

- El archivo `.env` **no debe subirse al repositorio** (está en `.gitignore`).
- Usa siempre `.env.example` con placeholders para documentar las variables
  necesarias.
- Si alguna credencial llegó a subirse al historial de Git, rótala/revócala y
  reescribe el historial antes de compartir el repositorio.