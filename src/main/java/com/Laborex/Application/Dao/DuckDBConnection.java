package com.Laborex.Application.Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Gerer la connexion avec DuckDB
public class DuckDBConnection {
	//Adresse de DuckDB avec pilote
    private static final String DB_URL = "jdbc:duckdb:laborex.duckdb";

    //Etablir et retourner la connexion
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    // Méthode utilitaire pour vérifier la connexion
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion DuckDB: " + e.getMessage());
            return false;
        }
    }
}