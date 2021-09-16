package me.hypherionmc.jqlite;

import me.hypherionmc.jqlite.data.SQLiteTable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseEngine {

    private Connection connection;
    private String dbName;
    public static final Logger LOGGER = Logger.getLogger("JQLite");
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

        try (Connection conn = DriverManager.getConnection(DBCONNECTION)) {
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
    public Connection getConnection() {
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
            try {
                table.create(this);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.log(Level.SEVERE, "Failed to register table " + table.getClass().getName());
            }
        }
    }

}
