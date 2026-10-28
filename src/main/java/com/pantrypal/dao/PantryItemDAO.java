package com.pantrypal.dao;

import com.pantrypal.model.Ingredient;
import com.pantrypal.model.ItemStatus;
import com.pantrypal.model.PantryItem;
import com.pantrypal.model.User;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.Session;

public class PantryItemDAO extends BaseDAO {

    // CREATE
    public Integer addItem(
            Integer userId,
            Integer ingredientId,
            PantryItem item
    ) {
        Integer id = executeInTransaction(session -> {
            User user = session.get(User.class, userId);

            if (user == null) {
                throw new DataAccessException("No user with id " + userId);
            }

            Ingredient ingredient
                    = session.get(Ingredient.class, ingredientId);

            if (ingredient == null) {
                throw new DataAccessException(
                        "No ingredient with id " + ingredientId
                );
            }

            item.setUser(user);
            item.setIngredient(ingredient);

            if (item.getExpiryDate() == null) {
                int days = ingredient.getCategory().getShelfLifeDays();

                item.setExpiryDate(
                        item.getPurchaseDate().plusDays(days)
                );
            }

            session.persist(item);
            return item.getId();
        });

        log.info(
                "Pantry item added: id={}, user={}, ingredient={}, expires={}",
                id,
                userId,
                ingredientId,
                item.getExpiryDate()
        );

        return id;
    }

    // READ
    public List<PantryItem> findByUser(Integer userId) {
        List<PantryItem> items = executeInTransaction(session
                -> session.createQuery(
                        "select p from PantryItem p "
                        + "join fetch p.ingredient i "
                        + "join fetch i.category "
                        + "where p.user.id = :uid "
                        + "order by p.expiryDate",
                        PantryItem.class
                )
                        .setParameter("uid", userId)
                        .getResultList()
        );

        log.debug(
                "findByUser({}) returned {} item(s)",
                userId,
                items.size()
        );

        return items;
    }

    public List<PantryItem> findExpiringBy(
            Integer userId,
            LocalDate cutoff
    ) {
        List<PantryItem> items = executeInTransaction(session
                -> session.createQuery(
                        "select p from PantryItem p "
                        + "join fetch p.ingredient i "
                        + "join fetch i.category "
                        + "where p.user.id = :uid "
                        + "and p.status = :st "
                        + "and p.expiryDate <= :cutoff "
                        + "order by p.expiryDate",
                        PantryItem.class
                )
                        .setParameter("uid", userId)
                        .setParameter("st", ItemStatus.ACTIVE)
                        .setParameter("cutoff", cutoff)
                        .getResultList()
        );

        if (!items.isEmpty()) {
            log.warn(
                    "{} item(s) of user {} expire on or before {}",
                    items.size(),
                    userId,
                    cutoff
            );
        }

        return items;
    }

    // UPDATE
    public void updateQuantity(Integer itemId, int quantity) {
        runInTransaction(session
                -> requireItem(session, itemId)
                        .setQuantity(Math.max(0, quantity))
        );

        log.info(
                "Pantry item {} quantity set to {}",
                itemId,
                Math.max(0, quantity)
        );
    }

    public void markStatus(Integer itemId, ItemStatus status) {
        runInTransaction(session -> {PantryItem item = requireItem(session, itemId);

            item.setStatus(status);
            item.setStatusDate(LocalDate.now());
        });

        log.info("Pantry item {} marked {}", itemId, status);
    }

    // DELETE
    public boolean deleteItem(Integer itemId) {
        boolean deleted = executeInTransaction(session -> {
            PantryItem item = session.get(PantryItem.class, itemId);

            if (item == null) {
                return false;
            }

            session.remove(item);
            return true;
        });

        if (deleted) {
            log.info("Pantry item deleted: id={}", itemId);
        } else {
            log.warn("Delete skipped: no pantry item with id={}", itemId);
        }

        return deleted;
    }

    private PantryItem requireItem(Session session, Integer itemId) {
        PantryItem item = session.get(PantryItem.class, itemId);

        if (item == null) {
            throw new DataAccessException(
                    "No pantry item with id " + itemId
            );
        }

        return item;
    }
}
