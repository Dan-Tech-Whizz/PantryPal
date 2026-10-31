package com.pantrypal.jdbc;

import com.pantrypal.dao.DataAccessException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JdbcUtil {
    private static final Logger LOG = LogManager.getLogger(JdbcUtil.class);
    private static final Properties PROPS = loadProperties();

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            // your existing code to load - keep it
            InputStream in2 = in;
            if (in2 == null) {
                in2 = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties");
            }
            if (in2 == null) {
                throw new IllegalStateException("db.properties not found");
            }
            try (InputStream closeable = in2) {
                props.load(closeable);
            }
        } catch (IOException e) {
            LOG.fatal("Cannot connect to the database", e);
            throw new ExceptionInInitializerError(e);
        }
        return props;
    }

    public static Connection getConnection() throws DataAccessException
    {
        return getConnection(
            PROPS.getProperty("jdbc.url"),
            PROPS.getProperty("jdbc.user"),
            PROPS.getProperty("jdbc.password")
        );
    }

    public static Connection getConnection(String url, String user, String password) throws DataAccessException{
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) 
        {
            LOG.fatal("Cannot connect to the database", e);
            throw new DataAccessException("Cannot connect to the database",e);
        }
    }
}