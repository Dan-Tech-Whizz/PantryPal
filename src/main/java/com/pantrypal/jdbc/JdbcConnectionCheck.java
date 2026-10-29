package com.pantrypal.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JdbcConnectionCheck {

    private static final Logger LOG =
            LogManager.getLogger(JdbcConnectionCheck.class);

    public static void main(String[] args) throws SQLException {
        try (Connection con = JdbcUtil.getConnection()) {
            LOG.info("Database connection valid: {}", con.isValid(5));
        }

        UserJdbcDAO users = new UserJdbcDAO();
        LOG.info("User query succeeded: {} user(s)", users.findAll().size());
    }
}