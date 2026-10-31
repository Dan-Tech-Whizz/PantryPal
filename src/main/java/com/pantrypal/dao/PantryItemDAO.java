package com.pantrypal.dao;

import com.pantrypal.model.Ingredient;
import com.pantrypal.model.ItemStatus;
import com.pantrypal.model.PantryItem;
import com.pantrypal.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;

public class PantryItemDAO extends BaseDAO {
    private static final Logger LOG = LoggerFactory.getLogger(PantryItemDAO.class);

    // ---------- CREATE ----------
    public Integer addItem(Integer userId, Integer ingredientId, PantryItem item) {
        return executeInTransaction(session -> {
            // Validate user exists
            User user = session.get(User.class, userId);
            if (user == null) {
                LOG.warn("addItem failed: no user with id {}", userId);
                throw new DataAccessException("No user with id " + userId);
            }
            // Validate ingredient exists
            Ingredient ingredient = session.get(Ingredient.class, ingredientId);
            if (ingredient == null) {
                LOG.warn("addItem failed: no ingredient with id {}", ingredientId);
                throw new DataAccessException("No ingredient with id " + ingredientId);
            }

            // Keep both sides in sync - set relationships
            item.setUser(user);
            item.setIngredient(ingredient);

            // If expiry is null, work it out from the category shelf life
            // Use purchaseDate + shelfLifeDays, not LocalDate.now()
            if (item.getExpiryDate() == null) {
                LocalDate base = item.getPurchaseDate() != null ? item.getPurchaseDate() : LocalDate.now();
                int days = ingredient.getCategory().getShelfLifeDays();
                item.setExpiryDate(base.plusDays(days));
            }

            // INSERT PantryItem
            session.persist(item);
            LOG.info("Pantry item added: id={}, user={}", item.getId(), userId);
            return item.getId();
        });
    }

    // ---------- READ ----------
    public List<PantryItem> findByUser(Integer userId) {
        // join fetch to avoid lazy loading issues, order by expiry
        return executeInTransaction(session ->
            session.createQuery(
                "from PantryItem p join fetch p.ingredient join fetch p.ingredient.category where p.user.id = :uid order by p.expiryDate",
                PantryItem.class)
                .setParameter("uid", userId)
                .getResultList()
        );
    }

    public List<PantryItem> findExpiringBy(Integer userId, LocalDate date) {
        // Only ACTIVE items expiring on or before cutoff - consumed items are no longer expiring
        return executeInTransaction(session ->
            session.createQuery(
                "from PantryItem p join fetch p.ingredient join fetch p.ingredient.category where p.user.id = :uid and p.expiryDate <= :date and p.status = :active order by p.expiryDate",
                PantryItem.class)
                .setParameter("uid", userId)
                .setParameter("date", date)
                .setParameter("active", ItemStatus.ACTIVE)
                .getResultList()
        );
    }

    // ---------- UPDATE ----------
    public void updateQuantity(Integer itemId, int quantity) {
        executeInTransaction(session -> {
            PantryItem item = requireItem(session, itemId);
            // Prevent negative quantity
            item.setQuantity(Math.max(0, quantity));
            LOG.info("Quantity updated for item id={} to {}", itemId, item.getQuantity());
            return null;
        });
    }

    public void markStatus(Integer itemId, ItemStatus status) {
        executeInTransaction(session -> {
            PantryItem item = requireItem(session, itemId);
            // Hibernate detects changes and issues UPDATE, set statusDate to now
            item.setStatus(status);
            item.setStatusDate(LocalDate.now());
            LOG.info("Status updated for item id={} to {}", itemId, status);
            return null;
        });
    }

    // ---------- DELETE ----------
    public boolean deleteItem(Integer itemId) {
        return executeInTransaction(session -> {
            PantryItem item = session.get(PantryItem.class, itemId);

            if (item == null) {
                LOG.warn("Delete skipped: no pantry item with id={}", itemId);
                return false;
            }

            session.remove(item);
            LOG.info("Pantry item deleted: id={}", itemId);
            return true;
        });
    }

    // ---------- Helper ----------
    private PantryItem requireItem(org.hibernate.Session session, Integer itemId) {
        PantryItem item = session.get(PantryItem.class, itemId);
        if (item == null) {
            LOG.warn("No pantry item found with id={}", itemId);
            throw new DataAccessException("No pantry item with id " + itemId);
        }
        return item;
    }
}