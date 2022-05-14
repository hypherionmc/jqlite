package me.hypherionmc.jqlite;

import me.hypherionmc.jqlite.data.SQLiteTable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseEngine {

    public static final Logger LOGGER = Logger.getLogger("JQLite");

    private static final HashMap<SQLiteTable, String> connectionMappings = new HashMap<>();

    private final String DBCONNECTION;

    /***
     * Create a new JQLite instance
     * @param dbName - The name of your database (excluding .db)
     */
    public DatabaseEngine(String dbName) {
        DBCONNECTION = "jdbc:sqlite:" + dbName +".db";
        try {
            final File dbFile = new File(dbName + ".db");

            if (!dbFile.exists()) {
                if (dbFile.createNewFile()) {
                    LOGGER.info("Created database file");
                    createNewDatabase();
                } else {
                    LOGGER.info("Could not create database file");
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private void createNewDatabase() {
        try (Connection conn = getInternalConnection()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                LOGGER.info("The driver name is " + meta.getDriverName());
                LOGGER.info("A new database has been created.");
                conn.close();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    /***
     * Get a connection to the database
     * @return
     */
    public static Connection getConnection(SQLiteTable table) {
        if (!connectionMappings.containsKey(table)) {
            LOGGER.log(Level.SEVERE, "Fatal: Cannot find connection mapping for :" + table.getClass());
            return null;
        }
        try {
            return DriverManager.getConnection(connectionMappings.get(table));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    private Connection getInternalConnection() {
        try {
            return DriverManager.getConnection(DBCONNECTION);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    /***
     * Register a class extending {SQLiteTable} on the JQLite engine
     * @param tables - A list of or single class extending SQLiteTable
     */
    public void registerTable(SQLiteTable... tables) {
        for (SQLiteTable table : tables) {
            connectionMappings.putIfAbsent(table, DBCONNECTION);
            try {
                table.create();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.log(Level.SEVERE, "Failed to register table " + table.getClass().getName());
            }
        }
    }

}
