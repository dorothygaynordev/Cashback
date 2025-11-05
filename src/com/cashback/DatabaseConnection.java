package com.cashback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnection {
    private static Connection connection;
    private static Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .load();

    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");
    private static final String DRIVER = dotenv.get("DB_DRIVER");

    private DatabaseConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Conexión establecida correctamente con MySQL.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: No se encontró el driver JDBC.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con la base de datos.");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Conexión cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error al cerrar la conexión.");
            e.printStackTrace();
        }
    }
}
