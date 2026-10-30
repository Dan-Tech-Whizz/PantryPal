package com.pantrypal.dao;

import com.pantrypal.model.Ingredient;
import com.pantrypal.model.ItemStatus;
import com.pantrypal.model.PantryItem;
import com.pantrypal.model.User;
import java.time.LocalDate;
import java.util.List;

public class PantryItemDAO extends BaseDAO {
    // =====================================================================
    // CREATE
    // =====================================================================
    /**
     * Adds an item to a user's pantry. If no expiry date was given, it is worked out
     * as purchase date + the category's shelf life (Dairy 7 days, Meat 3 days ...).
     *
     * @return the new item_id
     */
    
    public Integer addItem(Integer userId, Integer ingredientId, PantryItem item) {
        return executeInTransaction(session -> {
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new DataAccessException("No user with id " + userId);
            }
            Ingredient ingredient = session.get(Ingredient.class, ingredientId);
            if (ingredient == null) {
                throw new DataAccessException("No ingredient with id " + ingredientId);
            }

            item.setUser(user);                 
            item.setIngredient(ingredient);
            if (item.getExpiryDate() == null) {
                int days = ingredient.getCategory().getShelfLifeDays();  
                item.setExpiryDate(item.getPurchaseDate().plusDays(days));
            }
            session.persist(item);              
            return item.getId();
        });
    }
    
    // =====================================================================
    // READ
    // =====================================================================
    /**
     * All items of one user, soonest expiry first. "join fetch" loads the ingredient
     * and its category in the SAME query, so they are safe to use after the Session closes.
     */
    
    public List<PantryItem> findByUser(Integer userId) {
        return executeInTransaction(session -> session.createQuery(
                "select p from PantryItem p "
              + "join fetch p.ingredient i join fetch i.category "
              + "where p.user.id = :uid order by p.expiryDate", PantryItem.class)
                .setParameter("uid", userId)
                .getResultList());
    }
    /** ACTIVE items that expire on or before the cutoff date (drives the 
    red/amber cards). */
    public List<PantryItem> findExpiringBy(Integer userId, LocalDate cutoff) 
    {
        return executeInTransaction(session -> session.createQuery(
                "select p from PantryItem p "
              + "join fetch p.ingredient i join fetch i.category "
              + "where p.user.id = :uid and p.status = :st and p.expiryDate <= :cutoff "
              + "order by p.expiryDate", PantryItem.class)
                .setParameter("uid", userId)
                .setParameter("st", ItemStatus.ACTIVE)
                .setParameter("cutoff", cutoff)
                .getResultList());
    }
    
    // =====================================================================
    // UPDATE
    // =====================================================================
    public void updateQuantity(Integer itemId, int quantity) {
        runInTransaction(session -> {
            PantryItem item = requireItem(session, itemId);
            item.setQuantity(Math.max(0, quantity));
        });
    }
    
    /** Marks an item CONSUMED, EXPIRED or DISCARDED and stamps status_date 
    with today. */
    public void markStatus(Integer itemId, ItemStatus status) {
        runInTransaction(session -> {
            PantryItem item = requireItem(session, itemId);
            item.setStatus(status);
            item.setStatusDate(LocalDate.now());
        });
    }
    
    // =====================================================================
    // DELETE
    // =====================================================================
    /** @return true if a row was deleted, false if the id did not exist */
    public boolean deleteItem(Integer itemId) {
        return executeInTransaction(session -> {
            PantryItem item = session.get(PantryItem.class, itemId);
            if (item == null) {
                return false;
            }
            session.remove(item);
            return true;
        });
    }
    
    private PantryItem requireItem(org.hibernate.Session session, Integer itemId) {
        PantryItem item = session.get(PantryItem.class, itemId);
        if (item == null) {
            throw new DataAccessException("No pantry item with id " + itemId);
        }
        return item;
    }
}
