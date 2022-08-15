package me.hypherionmc.jqlite;

import me.hypherionmc.jqlite.data.SQLiteTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase {

    public static final Logger LOGGER = LoggerFactory.getLogger("JQLite");
    private final String DBCONNECTION;

    /***
     * Create a new JQLite instance
     * @param dbName - The name of your database (excluding .db)
     */
    public SQLiteDatabase(String dbName) {
        DBCONNECTION = "jdbc:sqlite:" + dbName +".db";
        try {
            final File dbFile = new File(dbName + ".db");

            if (!dbFile.exists()) {
                if (dbFile.createNewFile()) {
                    LOGGER.info("Created database file");
                    createNewDatabase();
                } else {
                    LOGGER.error("Could not create database file");
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create database {}", dbName, e);
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
            LOGGER.error("Failed to create database", e);
        }
    }

    /***
     * Get a connection to the database - INTERNAL USAGE
     * @return - An instance of the connection
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(DBCONNECTION);
        } catch (SQLException e) {
            LOGGER.error("Could not retrieve database connection", e);
        }
        return null;
    }

    /***
     * Register a class extending {SQLiteTable} on the JQLite engine
     * @param tables - List of or Single Table to register
     */
    public void registerTable(SQLiteTable... tables) {
        for (SQLiteTable table : tables) {
            try {
                table.create(this);
            } catch (Exception e) {
                LOGGER.error("Failed to register table {}", table.getClass().getName(), e);
            }
        }
    }
}
