package me.hypherionmc.jqlite.data;

import me.hypherionmc.jqlite.DatabaseEngine;
import me.hypherionmc.jqlite.annotations.*;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SQLiteTable {

    /***
     * Called to create a new table in the SqlLite Database. Called when you call @link {DatabaseEngine.registerTable}
     * @param connection - The connection to the database
     * @throws Exception - Thrown when something went wrong
     */
    @Deprecated
    public void create(Connection connection) throws Exception {
        StringBuilder sql = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();

        if (fields[0] == null || !fields[0].isAnnotationPresent(SQLCOLUMN.class) || fields[0].getAnnotation(SQLCOLUMN.class).type() != SQLCOLUMN.Type.PRIMARY) {
            throw new Exception("Primary field not defined or not top of class");
        }

        sql.append("CREATE TABLE IF NOT EXISTS ").append(this.getClass().getSimpleName().toLowerCase()).append(" (");

        for (int i = 0; i < fields.length; i++) {

            if (fields[i].isAnnotationPresent(SQLCOLUMN.class)) {
                SQLCOLUMN sqlcolumn = fields[i].getAnnotation(SQLCOLUMN.class);

                if (sqlcolumn.type() == SQLCOLUMN.Type.PRIMARY) {
                    sql.append(fields[i].getName().toLowerCase()).append(" INTEGER PRIMARY KEY AUTOINCREMENT");
                } else {
                    sql.append(fields[i].getName().toLowerCase()).append(" ")
                            .append(fields[i].getAnnotation(SQLCOLUMN.class).type().toString().toUpperCase())
                            .append("(")
                            .append(fields[i].getAnnotation(SQLCOLUMN.class).maxSize())
                            .append(")");
                }
            }

            if (i < (fields.length - 1)) {
                sql.append(", ");
            }
        }
        sql.append(");");

        try {
            final Statement statement = connection.createStatement();

            if (!statement.execute(sql.toString())) {
                DatabaseEngine.LOGGER.info(this.getClass().getSimpleName() + " table created");
            }

            statement.close();
            connection.close();
        } catch (Exception ex) {
            DatabaseEngine.LOGGER.log(Level.SEVERE, "Failed to create table " + this.getClass().getSimpleName());
            ex.printStackTrace();
        }
    }

    /***
     * Used to insert the values of the table into the SqlLite table
     * @return - True on success and false on failure
     */
    public boolean insert() {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder sql = new StringBuilder();

        Field[] fields = this.getClass().getDeclaredFields();

        columns.append("(");
        values.append("(");
        sql.append("INSERT INTO ").append(this.getClass().getSimpleName());

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(SQLCOLUMN.class) && fields[i].getAnnotation(SQLCOLUMN.class).type() != SQLCOLUMN.Type.PRIMARY) {
                columns.append(fields[i].getName().toLowerCase());
                try {
                    fields[i].setAccessible(true);

                    if (fields[i].isAnnotationPresent(SQLCOLUMN.class) && fields[i].getAnnotation(SQLCOLUMN.class).type() == SQLCOLUMN.Type.BOOLEAN) {
                        values.append("'").append(Boolean.parseBoolean(FieldUtils.readField(fields[i], this).toString()) ? 1 : 0).append("'");
                    } else {
                        values.append("'").append(FieldUtils.readField(fields[i], this)).append("'");
                    }

                    if (i < (fields.length - 1)) {
                        columns.append(", ");
                        values.append(", ");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        columns.append(")");
        values.append(")");
        sql.append(" ").append(columns.toString()).append(" VALUES ").append(values.toString());

        try {
            Connection connection = DatabaseEngine.getConnection();
            final Statement statement = connection.createStatement();

            if (!statement.execute(sql.toString())) {
                DatabaseEngine.LOGGER.info(this.getClass().getSimpleName() + " inserted");
            }

            statement.close();
            connection.close();
            return true;
        } catch (Exception ex) {
            DatabaseEngine.LOGGER.log(Level.SEVERE, "Failed to insert into table " + this.getClass().getSimpleName());
            ex.printStackTrace();
        }
        return false;
    }

    /***
     * Update the values of the SQLite Table
     * @return - True on success, false on failure
     */
    public boolean update() {
       // StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder sql = new StringBuilder();

        Field[] fields = this.getClass().getDeclaredFields();

        //columns.append("(");
        //values.append("(");
        sql.append("UPDATE ").append(this.getClass().getSimpleName()).append(" SET ");

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(SQLCOLUMN.class) && fields[i].getAnnotation(SQLCOLUMN.class).type() != SQLCOLUMN.Type.PRIMARY) {
                //columns.append(fields[i].getName().toLowerCase());
                try {
                    fields[i].setAccessible(true);

                    if (fields[i].isAnnotationPresent(SQLCOLUMN.class) && fields[i].getAnnotation(SQLCOLUMN.class).type() == SQLCOLUMN.Type.BOOLEAN) {
                        values.append(fields[i].getName().toLowerCase()).append(" = ").append("'").append(Boolean.parseBoolean(FieldUtils.readField(fields[i], this).toString()) ? 1 : 0).append("'");
                    } else {
                        values.append(fields[i].getName().toLowerCase()).append(" = ").append("'").append(FieldUtils.readField(fields[i], this)).append("'");
                    }

                    if (i < (fields.length - 1)) {
                        //columns.append(", ");
                        values.append(", ");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            fields[0].setAccessible(true);
            sql.append(values.toString()).append(" WHERE ").append(fields[0].getName().toLowerCase()).append(" = '").append(FieldUtils.readField(fields[0], this)).append("'");

            Connection connection = DatabaseEngine.getConnection();
            final Statement statement = connection.createStatement();

            if (!statement.execute(sql.toString())) {
                DatabaseEngine.LOGGER.info(this.getClass().getSimpleName() + " updated");
            } else {
                return false;
            }

            statement.close();
            connection.close();
            return true;

        } catch (Exception ex) {
            DatabaseEngine.LOGGER.log(Level.SEVERE, "Failed to update table " + this.getClass().getSimpleName());
            ex.printStackTrace();
        }
        return false;
    }

    /***
     * Fetch all values from the table that matches the whereClause
     * @param whereClause - The data to search against. Example: id = 1 or name = 'John Doe'
     * @param <T> - Returns a list of your class filled with data
     * @return
     */
    public <T extends SQLiteTable> List<T> fetchAll(String whereClause)  {

        Field[] fields = this.getClass().getDeclaredFields();
        List<T> tables = new ArrayList<>();

        try {
            Connection connection = DatabaseEngine.getConnection();
            final Statement statement = connection.createStatement();

            String sql = "SELECT * FROM " + this.getClass().getSimpleName() + (!whereClause.isEmpty() ? " WHERE " + whereClause : "");

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {

                Object obj = this.getClass().newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);

                    if (field.isAnnotationPresent(SQLCOLUMN.class)) {

                        switch (field.getAnnotation(SQLCOLUMN.class).type()) {

                            case PRIMARY:
                            case INT:
                            case INTEGER:
                            case TINYINT:
                            case SMALLINT:
                            case NUMERIC:
                                FieldUtils.writeField(field, obj, rs.getInt(field.getName()));
                                break;

                            case MEDIUMINT:
                            case BIGINT:
                                FieldUtils.writeField(field, obj, rs.getLong(field.getName()));
                                break;

                            case DECIMAL:
                            case DOUBLE:
                            case REAL:
                                FieldUtils.writeField(field, obj, rs.getDouble(field.getName()));
                                break;

                            case FLOAT:
                                FieldUtils.writeField(field, obj, rs.getFloat(field.getName()));
                                break;

                            case VARCHAR:
                            case NVARCHAR:
                            case TEXT:
                                FieldUtils.writeField(field, obj, rs.getString(field.getName()));
                                break;

                            case BOOLEAN:
                                FieldUtils.writeField(field, obj, rs.getBoolean(field.getName()));
                                break;

                            case DATE:
                            case DATETIME:
                                FieldUtils.writeField(field, obj, rs.getDate(field.getName()));
                                break;

                            /*case NCHAR:
                            case CHARACTER:
                                fields[i].setChar(this, rs.getCharacterStream(fields[i].getName().toLowerCase()));*/
                        }
                    }
                    field.setAccessible(false);
                }

                tables.add((T) obj);
            }

            rs.close();
            connection.close();
            statement.close();
            return tables;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }


    /***
     * Same as FetchAll(whereClause), but fetches all data from the table
     * @param <T>
     * @return
     */
    public <T extends SQLiteTable> List<T> fetchAll() {
       return this.fetchAll("");
    }

    /***
     * Fetch a single row from the table filtered by a whereClause
     * @param whereClause - The data to search against. Example: id = 1 or name = John Doe
     */
    public void fetch(String whereClause) {

        Field[] fields = this.getClass().getDeclaredFields();

        try {
            Connection connection = DatabaseEngine.getConnection();
            final Statement statement = connection.createStatement();

            String sql = "SELECT * FROM " + this.getClass().getSimpleName() + " WHERE " + whereClause;

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                //Object obj = this.getClass().newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);

                    if (field.isAnnotationPresent(SQLCOLUMN.class)) {

                        switch (field.getAnnotation(SQLCOLUMN.class).type()) {

                            case PRIMARY:
                            case INT:
                            case INTEGER:
                            case TINYINT:
                            case SMALLINT:
                            case NUMERIC:
                                FieldUtils.writeField(field, this, rs.getInt(field.getName()));
                                break;

                            case MEDIUMINT:
                            case BIGINT:
                                FieldUtils.writeField(field, this, rs.getLong(field.getName()));
                                break;

                            case DECIMAL:
                            case DOUBLE:
                            case REAL:
                                FieldUtils.writeField(field, this, rs.getDouble(field.getName()));
                                break;

                            case FLOAT:
                                FieldUtils.writeField(field, this, rs.getFloat(field.getName()));
                                break;

                            case VARCHAR:
                            case NVARCHAR:
                            case TEXT:
                                FieldUtils.writeField(field, this, rs.getString(field.getName()));
                                break;

                            case BOOLEAN:
                                FieldUtils.writeField(field, this, rs.getBoolean(field.getName()));
                                break;

                            case DATE:
                            case DATETIME:
                                FieldUtils.writeField(field, this, rs.getDate(field.getName()));
                                break;

                            /*case NCHAR:
                            case CHARACTER:
                                fields[i].setChar(this, rs.getCharacterStream(fields[i].getName().toLowerCase()));*/
                        }
                    }
                }

            }

            rs.close();
            connection.close();
            statement.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /***
     * Deletes the current row from the table
     * @return
     */
    public boolean delete() {
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuilder sql = new StringBuilder("DELETE FROM " + this.getClass().getSimpleName() + " WHERE id = ");
        boolean res = false;

        try {
            Connection connection = DatabaseEngine.getConnection();
            final Statement statement = connection.createStatement();

            for (Field field : fields) {
                if (field.isAnnotationPresent(SQLCOLUMN.class) && field.getAnnotation(SQLCOLUMN.class).type() == SQLCOLUMN.Type.PRIMARY) {
                    field.setAccessible(true);
                    sql.append(FieldUtils.readField(field, this).toString());
                }
            }

            res = statement.execute(sql.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return !res;
    }
}
