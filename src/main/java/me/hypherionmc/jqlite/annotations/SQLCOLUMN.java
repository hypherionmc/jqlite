package me.hypherionmc.jqlite.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SQLCOLUMN {

    public enum Type {
        PRIMARY,
        INT,
        INTEGER,
        TINYINT,
        SMALLINT,
        MEDIUMINT,
        BIGINT,
        BLOB,
        NUMERIC,
        DECIMAL,
        BOOLEAN,
        DATE,
        DATETIME,
        REAL,
        DOUBLE,
        FLOAT,
        CHARACTER,
        VARCHAR,
        NCHAR,
        NVARCHAR,
        TEXT,
        CLOB
    }

    Type type();
    int maxSize() default 10;
}
