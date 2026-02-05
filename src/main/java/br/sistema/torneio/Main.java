package br.sistema.torneio;

import br.sistema.torneio.connection.ConnectionFactory;
import net.datafaker.Faker;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        Faker faker = new Faker();

        System.out.println("Tentando conectar ao banco...");

        ConnectionFactory factory = new ConnectionFactory();

        try (Connection conexao = factory.getConnection()) {
            System.out.println("SUCESSO! Conexão aberta na porta 3306.");
            System.out.println("Banco de dados conectado: torneio_db");
        } catch (Exception e) {
            System.out.println("ERRO: Não foi possível conectar.");
            e.printStackTrace();
        }

    }
}