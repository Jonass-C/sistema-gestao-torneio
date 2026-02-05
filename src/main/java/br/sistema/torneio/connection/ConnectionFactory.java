package br.sistema.torneio.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private final String URL = "jdbc:mysql://localhost:3306/torneio_db";
    private final String USER = "root";

    public Connection getConnection() {

        try {
            String PASSWORD = System.getenv("DB_PASSWORD");

            if (PASSWORD == null) {
                throw new RuntimeException("A variável de ambiente DB_PASSWORD não foi encontrada!");
            }

            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar no banco de dados: " + e.getMessage(), e);
        }
    }

}
