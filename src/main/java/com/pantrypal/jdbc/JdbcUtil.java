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

public final class JdbcUtil {

    private static final Logger LOG =
            LogManager.getLogger(JdbcUtil.class);

    private static final Properties PROPS = loadProperties();

    private JdbcUtil() {
    }

    private static Properties loadProperties() {
        try (InputStream in = JdbcUtil.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (in == null) {
                throw new IOException(
                        "db.properties not found on the classpath");
            }

            Properties props = new Properties();
            props.load(in);

            LOG.info("JDBC settings loaded from db.properties");
            return props;

        } catch (IOException ex) {
            LOG.fatal("Cannot load db.properties", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Connection getConnection() {
        return getConnection(
                PROPS.getProperty("jdbc.url"),
                PROPS.getProperty("jdbc.user"),
                PROPS.getProperty("jdbc.password")
        );
    }

    public static Connection getConnection(
            String url, String user, String password) {

        try {
            Connection con =
                    DriverManager.getConnection(url, user, password);

            LOG.debug("JDBC connection opened");
            return con;

        } catch (SQLException ex) {
            LOG.fatal("Cannot connect to the database at {}", url, ex);

            throw new DataAccessException(
                    "Cannot connect to the database: " + ex.getMessage(),
                    ex
            );
        }
    }
}