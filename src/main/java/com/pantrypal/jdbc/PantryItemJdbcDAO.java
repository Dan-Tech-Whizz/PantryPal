package com.pantrypal.jdbc;

import com.pantrypal.dao.DataAccessException;
import com.pantrypal.model.Ingredient;
import com.pantrypal.model.ItemStatus;
import com.pantrypal.model.PantryItem;
import com.pantrypal.model.Receipt;
import com.pantrypal.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PantryItemJdbcDAO {

    private static final Logger LOG =
            LogManager.getLogger(PantryItemJdbcDAO.class);

    private static final String SELECT =
            "SELECT p.item_id, p.user_id, p.ingredient_id, "
            + "i.ingredient_name, p.receipt_id, p.purchase_date, "
            + "p.expiry_date, p.quantity, p.status, p.status_date "
            + "FROM PANTRY_ITEM p JOIN INGREDIENT i "
            + "ON i.ingredient_id = p.ingredient_id ";

    // ---------- CREATE ----------
    public int create(PantryItem item) {
        String sql =
                "INSERT INTO PANTRY_ITEM (user_id, ingredient_id, "
                + "receipt_id, purchase_date, expiry_date, quantity, "
                + "status, status_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getUser().getId());
            ps.setInt(2, item.getIngredient().getId());

            if (item.getReceipt() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, item.getReceipt().getId());
            }

            ps.setObject(4, item.getPurchaseDate());
            ps.setObject(5, item.getExpiryDate());
            ps.setInt(6, item.getQuantity());
            ps.setString(7, item.getStatus().name());

            if (item.getStatusDate() == null) {
                ps.setNull(8, Types.DATE);
            } else {
                ps.setObject(8, item.getStatusDate());
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated pantry item ID returned");
                }
                item.setId(keys.getInt(1));
            }

            LOG.info(
                    "[JDBC] Pantry item created: id={}, user={}, ingredient={}",
                    item.getId(),
                    item.getUser().getId(),
                    item.getIngredient().getId()
            );

            return item.getId();

        } catch (SQLException ex) {
            LOG.error("[JDBC] Could not create pantry item", ex);
            throw new DataAccessException(
                    "Could not create pantry item: " + ex.getMessage(), ex);
        }
    }

    // ---------- READ ----------
    public Optional<PantryItem> findById(int itemId) {
        List<PantryItem> found =
                query(SELECT + "WHERE p.item_id = ?", itemId);

        if (found.isEmpty()) {
            LOG.warn("[JDBC] No pantry item found with id={}", itemId);
            return Optional.empty();
        }

        return Optional.of(found.get(0));
    }

    public List<PantryItem> findByUser(int userId) {
        List<PantryItem> items = query(
                SELECT + "WHERE p.user_id = ? ORDER BY p.expiry_date",
                userId
        );

        LOG.debug("[JDBC] findByUser({}) returned {} item(s)",
                userId, items.size());

        return items;
    }

    // ---------- UPDATE ----------
    public boolean updateQuantity(int itemId, int quantity) {
        int rows = executeUpdate(
                "UPDATE PANTRY_ITEM SET quantity = ? WHERE item_id = ?",
                "Update quantity",
                Math.max(0, quantity), itemId
        );

        return report(rows, "quantity updated", itemId);
    }

    public boolean updateStatus(int itemId, ItemStatus status) {
        int rows = executeUpdate(
                "UPDATE PANTRY_ITEM SET status = ?, status_date = ? "
                        + "WHERE item_id = ?",
                "Update status",
                status.name(), LocalDate.now(), itemId
        );

        return report(rows, "marked " + status, itemId);
    }

    // ---------- DELETE ----------
    public boolean delete(int itemId) {
        int rows = executeUpdate(
                "DELETE FROM PANTRY_ITEM WHERE item_id = ?",
                "Delete",
                itemId
        );

        return report(rows, "deleted", itemId);
    }

    // ---------- SHARED HELPERS ----------
    private List<PantryItem> query(String sql, int param) {
        List<PantryItem> items = new ArrayList<>();

        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(map(rs));
                }
            }

            return items;

        } catch (SQLException ex) {
            LOG.error("[JDBC] Query failed: {}", sql, ex);
            throw new DataAccessException(
                    "Query failed: " + ex.getMessage(), ex);
        }
    }

    private int executeUpdate(
            String sql, String what, Object... params) {

        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            return ps.executeUpdate();

        } catch (SQLException ex) {
            LOG.error("[JDBC] {} failed", what, ex);
            throw new DataAccessException(
                    what + " failed: " + ex.getMessage(), ex);
        }
    }

    private boolean report(int rows, String action, int itemId) {
        if (rows > 0) {
            LOG.info("[JDBC] Pantry item {}: {}", itemId, action);
            return true;
        }

        LOG.warn(
                "[JDBC] Pantry item {}: nothing changed ({}), id not found",
                itemId, action
        );

        return false;
    }

    private static PantryItem map(ResultSet rs) throws SQLException {
        PantryItem item = new PantryItem(
                rs.getDate("purchase_date").toLocalDate(),
                rs.getDate("expiry_date").toLocalDate(),
                rs.getInt("quantity")
        );

        item.setId(rs.getInt("item_id"));

        User user = new User();
        user.setId(rs.getInt("user_id"));
        item.setUser(user);

        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("ingredient_id"));
        ingredient.setIngredientName(rs.getString("ingredient_name"));
        item.setIngredient(ingredient);

        int receiptId = rs.getInt("receipt_id");

        if (!rs.wasNull()) {
            Receipt receipt = new Receipt();
            receipt.setId(receiptId);
            item.setReceipt(receipt);
        }

        item.setStatus(ItemStatus.valueOf(rs.getString("status")));

        java.sql.Date statusDate = rs.getDate("status_date");

        if (statusDate != null) {
            item.setStatusDate(statusDate.toLocalDate());
        }

        return item;
    }
}