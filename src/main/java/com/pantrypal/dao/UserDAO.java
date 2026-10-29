package com.pantrypal.dao;

import com.pantrypal.model.User;
import java.util.List;
import java.util.Optional;

public class UserDAO extends BaseDAO {

    // ---------- CREATE ----------
    public Integer createUser(User user) {
        Integer id = executeInTransaction(session -> {
            // INSERT User, then its items through cascade.
            session.persist(user);
            return user.getId();
        });

        log.info("User created: id={}, email={}", id, user.getEmail());
        return id;
    }

    // ---------- READ ----------
    public Optional<User> findById(Integer id) {
        Optional<User> result = executeInTransaction(session ->
                session.createQuery(
                        "select u from User u left join fetch u.pantryItems "
                                + "where u.id = :id",
                        User.class)
                        .setParameter("id", id)
                        .uniqueResultOptional()
        );

        if (result.isEmpty()) {
            log.warn("No user found with id={}", id);
        }

        return result;
    }

    public Optional<User> findByEmail(String email) {
        Optional<User> result = executeInTransaction(session ->
                session.createQuery(
                        "from User u where u.email = :email",
                        User.class)
                        .setParameter("email", email)
                        .uniqueResultOptional()
        );

        log.debug("findByEmail({}) found a user: {}", email, result.isPresent());
        return result;
    }

    public List<User> findAll() {
        List<User> users = executeInTransaction(session ->
                session.createQuery(
                        "from User u order by u.id",
                        User.class)
                        .getResultList()
        );

        log.debug("findAll returned {} user(s)", users.size());
        return users;
    }

    // ---------- UPDATE ----------
    public void updateSettings(
            Integer userId,
            boolean notifications,
            boolean purchaseSuggestions) {

        runInTransaction(session -> {
            User user = session.get(User.class, userId);

            if (user == null) {
                throw new DataAccessException("No user with id " + userId);
            }

            // Hibernate detects these changes and issues the UPDATE.
            user.setNotificationsEnabled(notifications);
            user.setAllowPurchaseSuggestions(purchaseSuggestions);
        });

        log.info(
                "Settings updated for user id={} (notifications={}, suggestions={})",
                userId, notifications, purchaseSuggestions
        );
    }

    // ---------- DELETE ----------
    public boolean deleteUser(Integer userId) {
        boolean deleted = executeInTransaction(session -> {
            User user = session.get(User.class, userId);

            if (user == null) {
                return false;
            }

            // Cascade removes the user's pantry items first.
            session.remove(user);
            return true;
        });

        if (deleted) {
            log.info("User deleted: id={}", userId);
        } else {
            log.warn("Delete skipped: no user with id={}", userId);
        }

        return deleted;
    }
}