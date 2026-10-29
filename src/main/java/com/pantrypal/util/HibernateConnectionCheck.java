package com.pantrypal.util;

import com.pantrypal.dao.DataAccessException;
import com.pantrypal.dao.UserDAO;
import com.pantrypal.model.User;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HibernateConnectionCheck {

    private static final Logger LOG =
            LogManager.getLogger(HibernateConnectionCheck.class);

    public static void main(String[] args) {
        try {
            UserDAO users = new UserDAO();

            List<User> existingUsers = users.findAll();

            LOG.info("Hibernate user query succeeded: {} user(s)",
                    existingUsers.size());

            // Check WARN logging for an ID that does not exist.
            users.findById(-1);

            // Check ERROR logging using an existing email.
            if (existingUsers.isEmpty()) {
                LOG.warn("Duplicate-email check skipped: no users available");
            } else {
                User existing = existingUsers.get(0);

                try {
                    Integer createdId = users.createUser(new User(
                            "Duplicate Email Check",
                            existing.getEmail(),
                            "test-only-placeholder"
                    ));

                    LOG.warn(
                            "Duplicate email was accepted unexpectedly; "
                                    + "removing check user id={}",
                            createdId
                    );

                    users.deleteUser(createdId);

                } catch (DataAccessException ex) {
                    LOG.info("Expected duplicate-email failure was caught");
                }
            }

        } finally {
            HibernateUtil.shutdown();
        }
    }
}