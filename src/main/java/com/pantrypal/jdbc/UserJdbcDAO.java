package com.pantrypal.jdbc;

import com.pantrypal.dao.DataAccessException;
import com.pantrypal.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserJdbcDAO {

    private static final Logger LOG = LogManager.getLogger(UserJdbcDAO.class);

    private static final String SELECT =
            "SELECT user_id, name, email, password_hash, "
            + "notifications_enabled, allow_purchase_suggestions FROM USERS ";

    // ---------- CREATE ----------
    public int create(User user) {
        String sql =
                "INSERT INTO USERS (name, email, password_hash, "
                + "notifications_enabled, allow_purchase_suggestions) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setBoolean(4, user.getNotificationsEnabled());
            ps.setBoolean(5, user.getAllowPurchaseSuggestions());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated user ID returned");
                }
                user.setId(keys.getInt(1));
            }

            LOG.info("[JDBC] User created: id={}, email={}",
                    user.getId(), user.getEmail());

            return user.getId();

        } catch (SQLException ex) {
            LOG.error("[JDBC] Could not create user {}", user.getEmail(), ex);
            throw new DataAccessException(
                    "Could not create user: " + ex.getMessage(), ex);
        }
    }

    // ---------- READ ----------
    public Optional<User> findById(int id) {
        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT + "WHERE user_id = ?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }

            LOG.warn("[JDBC] No user found with id={}", id);
            return Optional.empty();

        } catch (SQLException ex) {
            LOG.error("[JDBC] Could not read user {}", id, ex);
            throw new DataAccessException(
                    "Could not read user: " + ex.getMessage(), ex);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT + "ORDER BY user_id");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(map(rs));
            }

            LOG.debug("[JDBC] findAll returned {} user(s)", users.size());
            return users;

        } catch (SQLException ex) {
            LOG.error("[JDBC] Could not list users", ex);
            throw new DataAccessException(
                    "Could not list users: " + ex.getMessage(), ex);
        }
    }

    // ---------- UPDATE ----------
    public boolean update(User user) {
        String sql =
                "UPDATE USERS SET name = ?, email = ?, "
                + "notifications_enabled = ?, "
                + "allow_purchase_suggestions = ? WHERE user_id = ?";

        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setBoolean(3, user.getNotificationsEnabled());
            ps.setBoolean(4, user.getAllowPurchaseSuggestions());
            ps.setInt(5, user.getId());

            boolean updated = ps.executeUpdate() > 0;

            if (updated) {
                LOG.info("[JDBC] User updated: id={}", user.getId());
            } else {
                LOG.warn("[JDBC] Update skipped: no user with id={}",
                        user.getId());
            }

            return updated;

        } catch (SQLException ex) {
            LOG.error("[JDBC] Could not update user {}", user.getId(), ex);
            throw new DataAccessException(
                    "Could not update user: " + ex.getMessage(), ex);
        }
    }

    // ---------- DELETE ----------
    public boolean delete(int id) {
        try (Connection con = JdbcUtil.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement items = con.prepareStatement(
                         "DELETE FROM PANTRY_ITEM WHERE user_id = ?");
                 PreparedStatement user = con.prepareStatement(
                         "DELETE FROM USERS WHERE user_id = ?")) {

                items.setInt(1, id);
                items.executeUpdate();

                user.setInt(1, id);
                int rows = user.executeUpdate();

                con.commit();

                if (rows > 0) {
                    LOG.info("[JDBC] User deleted: id={}", id);
                } else {
                    LOG.warn("[JDBC] Delete skipped: no user with id={}", id);
                }

                return rows > 0;

            } catch (SQLException ex) {
                try {
                    con.rollback();
                    LOG.warn(
                            "[JDBC] Transaction rolled back while deleting user {}",
                            id);
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                    LOG.error("[JDBC] Rollback failed for user {}", id,
                            rollbackEx);
                }

                throw ex;
            }

        } catch (SQLException ex) {
            LOG.error("[JDBC] Could not delete user {}", id, ex);
            throw new DataAccessException(
                    "Could not delete user: " + ex.getMessage(), ex);
        }
    }

    // Convert a database row into a User object.
    private static User map(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash")
        );

        user.setId(rs.getInt("user_id"));
        user.setNotificationsEnabled(
                rs.getBoolean("notifications_enabled"));
        user.setAllowPurchaseSuggestions(
                rs.getBoolean("allow_purchase_suggestions"));

        return user;
    }
}