package com.f1simulator.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión a PostgreSQL con patrón Singleton (5.15.4 del temario): se abre
 * una sola conexión y se reutiliza en toda la app, en vez de crear una nueva
 * cada vez que se necesita.
 *
 * Mismo patrón DriverManager que ConexionMySQL visto en clase — solo cambia
 * el driver y la URL de conexión.
 *
 * Las credenciales se leen de variables de entorno (mismos nombres que el
 * .env del proyecto), nunca quedan escritas en el código.
 */
public class ConexionPostgreSQL {

    private static ConexionPostgreSQL instancia;
    private Connection conexion;

    private static final String HOST = "localhost";

    private ConexionPostgreSQL() {
        String db = obtenerVariable("POSTGRES_DB", "f1simulator");
        String usuario = obtenerVariable("POSTGRES_USER", "f1user");
        String password = obtenerVariable("POSTGRES_PASSWORD", null);
        String puerto = obtenerVariable("POSTGRES_PORT", "5432");

        if (password == null) {
            throw new IllegalStateException(
                "No se encontró la variable de entorno POSTGRES_PASSWORD. "
                + "Exporta las variables de tu .env antes de ejecutar la app "
                + "(ej: 'export $(cat .env | xargs)' en Linux/Mac).");
        }

        String url = String.format("jdbc:postgresql://%s:%s/%s", HOST, puerto, db);

        try {
            Class.forName("org.postgresql.Driver");
            this.conexion = DriverManager.getConnection(url, usuario, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de PostgreSQL en el classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a PostgreSQL en " + url, e);
        }
    }

    /** Punto de acceso único al Singleton. */
    public static synchronized ConexionPostgreSQL getInstancia() {
        if (instancia == null) {
            instancia = new ConexionPostgreSQL();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    private String obtenerVariable(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return (valor != null && !valor.isBlank()) ? valor : porDefecto;
    }
}