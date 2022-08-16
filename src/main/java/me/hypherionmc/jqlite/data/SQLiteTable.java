package me.hypherionmc.jqlite.data;

import me.hypherionmc.jqlite.SQLiteDatabase;
import me.hypherionmc.jqlite.annotations.SQLColumn;
import me.hypherionmc.jqlite.util.DateUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SQLiteTable {

    /***
     * INTERNAL METHOD. NEVER CALL MANUALLY
     * Called to create a new table in the SQLITE Database. Called when you call @link {SQLiteDatabase.registerTable}
     * @throws Exception - Thrown when something went wrong
     */
    public void create(SQLiteDatabase engine) throws Exception {
        StringBuilder sql = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();

        if (
                fields[0] == null ||
                !fields[0].isAnnotationPresent(SQLColumn.class) ||
                fields[0].getAnnotation(SQLColumn.class).value() != SQLColumn.Type.PRIMARY
        ) {
            throw new Exception("Primary field not defined or not top of class");
        }

        sql.append("CREATE TABLE IF NOT EXISTS ").append(this.getClass().getSimpleName().toLowerCase()).append(" (");

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(SQLColumn.class)) {
                SQLColumn sqlcolumn = fields[i].getAnnotation(SQLColumn.class);

                if (sqlcolumn.value() == SQLColumn.Type.PRIMARY) {
                    sql.append(fields[i].getName().toLowerCase()).append(" INTEGER PRIMARY KEY AUTOINCREMENT");
                } else {
                    sql.append(fields[i].getName().toLowerCase()).append(" ")
                            .append(fields[i].getAnnotation(SQLColumn.class).value().toString().toUpperCase())
                            .append("(")
                            .append(fields[i].getAnnotation(SQLColumn.class).maxSize())
                            .append(")");
                }
            }
            if (i < (fields.length - 1)) {
                sql.append(", ");
            }
        }
        sql.append(");");

        try {
            Connection connection = engine.getConnection();
            final Statement statement = connection.createStatement();

            if (!statement.execute(sql.toString())) {
                SQLiteDatabase.LOGGER.info(this.getClass().getSimpleName() + " table created");
            }

            statement.close();
            connection.close();
        } catch (Exception ex) {
            SQLiteDatabase.LOGGER.error("Failed to create table {}", this.getClass().getSimpleName(), ex);
        }
    }

    /***
     * Used to insert the values of the table into the SQLite table
     * @return - True on success and false on failure
     */
    public boolean insert(SQLiteDatabase engine) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder sql = new StringBuilder();

        Field[] fields = this.getClass().getDeclaredFields();

        columns.append("(");
        values.append("(");
        sql.append("INSERT INTO ").append(this.getClass().getSimpleName());

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(SQLColumn.class) && fields[i].getAnnotation(SQLColumn.class).value() != SQLColumn.Type.PRIMARY) {
                columns.append(fields[i].getName().toLowerCase());
                try {
                    fields[i].setAccessible(true);

                    if (fields[i].isAnnotationPresent(SQLColumn.class) && fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.BOOLEAN) {
                        values.append("'").append(Boolean.parseBoolean(FieldUtils.readField(fields[i], this).toString()) ? 1 : 0).append("'");
                    } else if (fields[i].isAnnotationPresent(SQLColumn.class) && (fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.TEXT || fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.VARCHAR || fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.NVARCHAR)) {
                        values.append("'").append(FieldUtils.readField(fields[i], this).toString().replaceAll("'", "''")).append("'");
                    } else if (fields[i].isAnnotationPresent(SQLColumn.class) && (fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.TIMESTAMP || fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.UPDATESTAMP)) {
                        values.append("'").append(DateUtils.getTimestamp()).append("'");
                    } else if (fields[i].isAnnotationPresent(SQLColumn.class) && (fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.DATE || fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.DATETIME)) {
                        values.append("'").append(DateUtils.formatTimeStamp((Date) FieldUtils.readField(fields[i], this))).append("'");
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
        sql.append(" ").append(columns).append(" VALUES ").append(values);

        try {
            Connection connection = engine.getConnection();
            final Statement statement = connection.createStatement();

            if (!statement.execute(sql.toString())) {
                SQLiteDatabase.LOGGER.info(this.getClass().getSimpleName() + " inserted");
            }

            statement.close();
            connection.close();
            return true;
        } catch (Exception ex) {
            SQLiteDatabase.LOGGER.error("Failed to insert into table {}", this.getClass().getSimpleName(), ex);
        }
        return false;
    }

    /***
     * Update the values of the SQLite Table
     * @return - True on success, false on failure
     */
    public boolean update(SQLiteDatabase engine) {
        StringBuilder values = new StringBuilder();
        StringBuilder sql = new StringBuilder();

        Field[] fields = this.getClass().getDeclaredFields();

        sql.append("UPDATE ").append(this.getClass().getSimpleName()).append(" SET ");

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(SQLColumn.class) && fields[i].getAnnotation(SQLColumn.class).value() != SQLColumn.Type.PRIMARY) {
                try {
                    fields[i].setAccessible(true);

                    if (fields[i].isAnnotationPresent(SQLColumn.class) && fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.BOOLEAN) {
                        values.append(fields[i].getName().toLowerCase()).append(" = ").append("'").append(Boolean.parseBoolean(FieldUtils.readField(fields[i], this).toString()) ? 1 : 0).append("'");
                    } else if (fields[i].isAnnotationPresent(SQLColumn.class) && (fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.TEXT || fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.VARCHAR || fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.NVARCHAR)) {
                        values.append(fields[i].getName().toLowerCase()).append(" = ").append("'").append(FieldUtils.readField(fields[i], this).toString().replaceAll("'", "''")).append("'");
                    } else if (fields[i].isAnnotationPresent(SQLColumn.class) && fields[i].getAnnotation(SQLColumn.class).value() == SQLColumn.Type.UPDATESTAMP) {
                        values.append(fields[i].getName().toLowerCase()).append(" = ").append("'").append(DateUtils.getTimestamp()).append("'");
                    } else {
                        values.append(fields[i].getName().toLowerCase()).append(" = ").append("'").append(FieldUtils.readField(fields[i], this)).append("'");
                    }

                    if (i < (fields.length - 1)) {
                        values.append(", ");
                    }
                } catch (Exception ex) {
                    SQLiteDatabase.LOGGER.error("Failed to update table {}", this.getClass().getSimpleName(), ex);
                }
            }
        }

        try {
            fields[0].setAccessible(true);
            sql.append(values).append(" WHERE ").append(fields[0].getName().toLowerCase()).append(" = '").append(FieldUtils.readField(fields[0], this)).append("'");

            Connection connection = engine.getConnection();
            final Statement statement = connection.createStatement();

            if (!statement.execute(sql.toString())) {
                SQLiteDatabase.LOGGER.info(this.getClass().getSimpleName() + " updated");
            } else {
                return false;
            }

            statement.close();
            connection.close();
            return true;

        } catch (Exception ex) {
            SQLiteDatabase.LOGGER.error("Failed to update table {}", this.getClass().getSimpleName(), ex);
        }
        return false;
    }

    /***
     * Fetch all values from the table that matches the whereClause
     * @param whereClause - The data to search against. Example: id = 1 or name = 'John Doe'
     * @param <T> - Returns a list of your class filled with data
     * @return
     */
    public <T extends SQLiteTable> List<T> fetchAll(SQLiteDatabase engine, String whereClause)  {

        Field[] fields = this.getClass().getDeclaredFields();
        List<T> tables = new ArrayList<>();

        try {
            Connection connection = engine.getConnection();
            final Statement statement = connection.createStatement();

            String sql = "SELECT * FROM " + this.getClass().getSimpleName() + (!whereClause.isEmpty() ? " WHERE " + whereClause : "");
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                Object obj = this.getClass().newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);

                    if (field.isAnnotationPresent(SQLColumn.class)) {
                        switch (field.getAnnotation(SQLColumn.class).value()) {
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
                                FieldUtils.writeField(field, obj, rs.getString(field.getName()).replaceAll("''", "'"));
                                break;

                            case BOOLEAN:
                                FieldUtils.writeField(field, obj, rs.getBoolean(field.getName()));
                                break;

                            case DATE:
                            case DATETIME:
                            case TIMESTAMP:
                            case UPDATESTAMP:
                                FieldUtils.writeField(field, obj, rs.getDate(field.getName()));
                                break;
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
            SQLiteDatabase.LOGGER.error("Failed to fetch table {}", this.getClass().getSimpleName(), ex);
        }

        return new ArrayList<>();
    }


    /***
     * Same as FetchAll(whereClause), but fetches all data from the table
     * @param <T>
     * @return
     */
    public <T extends SQLiteTable> List<T> fetchAll(SQLiteDatabase engine) {
       return this.fetchAll(engine, "");
    }

    /***
     * Fetch a single row from the table filtered by a whereClause
     * @param whereClause - The data to search against. Example: id = 1 or name = John Doe
     */
    public void fetch(SQLiteDatabase engine, String whereClause) {
        Field[] fields = this.getClass().getDeclaredFields();

        try {
            Connection connection = engine.getConnection();
            final Statement statement = connection.createStatement();

            String sql = "SELECT * FROM " + this.getClass().getSimpleName() + " WHERE " + whereClause;

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(SQLColumn.class)) {
                        switch (field.getAnnotation(SQLColumn.class).value()) {
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
                                FieldUtils.writeField(field, this, rs.getString(field.getName()).replaceAll("''", "'"));
                                break;

                            case BOOLEAN:
                                FieldUtils.writeField(field, this, rs.getBoolean(field.getName()));
                                break;

                            case DATE:
                            case DATETIME:
                            case TIMESTAMP:
                            case UPDATESTAMP:
                                FieldUtils.writeField(field, this, rs.getDate(field.getName()));
                                break;
                        }
                    }
                }
            }

            rs.close();
            connection.close();
            statement.close();
        } catch (Exception ex) {
            SQLiteDatabase.LOGGER.error("Failed to fetch table {}", this.getClass().getSimpleName(), ex);
        }
    }

    /***
     * Deletes the current row from the table
     * @return
     */
    public boolean delete(SQLiteDatabase engine) {
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuilder sql = new StringBuilder("DELETE FROM " + this.getClass().getSimpleName() + " WHERE id = ");
        boolean res = false;

        try {
            Connection connection = engine.getConnection();
            final Statement statement = connection.createStatement();

            for (Field field : fields) {
                if (field.isAnnotationPresent(SQLColumn.class) && field.getAnnotation(SQLColumn.class).value() == SQLColumn.Type.PRIMARY) {
                    field.setAccessible(true);
                    sql.append(FieldUtils.readField(field, this).toString());
                }
            }

            res = statement.execute(sql.toString());
        } catch (Exception ex) {
            SQLiteDatabase.LOGGER.error("Failed to delete table {}", this.getClass().getSimpleName(), ex);
        }
        return !res;
    }

    /***
     * A wrapper around fetchAll and insert, to determine if a value already exists in the DB before inserting
     * @param filter - The columns to perform the check against
     * @return - True on success, false when entry already exists
     */
    public boolean insertUnique(SQLiteDatabase engine, String filter) {
        List<SQLiteTable> results = this.fetchAll(engine, filter);
        if (results.isEmpty()) {
            return this.insert(engine);
        }
        return false;
    }

    public boolean insertOrUpdate(SQLiteDatabase engine, String filter) {
        List<SQLiteTable> results = this.fetchAll(engine, filter);
        if (results.isEmpty()) {
            return this.insert(engine);
        } else {
            results.forEach(r -> r.update(engine));
            return true;
        }
    }
}
