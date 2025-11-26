package org.example.diariodehumor.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    public Connection conectaBD() {
        Connection conn = null;

        // MUDAR CONFIG CONFORME O NECESSÁRIO

        // Config do banco
        String url = "jdbc:mysql://localhost:3307/diario_de_humor"; //  jdbc:mysql://localhost:[número_da_port]/[nome_do_banco]
        String username = "root"; //    [usuário]
        String password = ""; //    [senha]

        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Conexão estabelecida com sucesso!");
        } catch (SQLException e) {
            System.out.println("❌ Erro ao conectar: " + e.getMessage());
        }
        return conn;
    }
}