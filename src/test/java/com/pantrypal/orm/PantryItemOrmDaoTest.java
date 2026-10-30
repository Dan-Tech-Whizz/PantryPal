package com.pantrypal.orm;

import static org.junit.jupiter.api.Assertions.*;
import com.pantrypal.dao.DataAccessException;
import com.pantrypal.dao.PantryItemDAO;
import com.pantrypal.dao.UserDAO;
import com.pantrypal.model.ItemStatus;
import com.pantrypal.model.PantryItem;
import com.pantrypal.model.User;
import com.pantrypal.support.LogCapture;
import com.pantrypal.support.TestLogger;
import java.time.LocalDate;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

//CRUD tests for the Hibernate (ORM) PantryItemDAO
@ExtendWith(TestLogger.class)
class PantryItemOrmDaoTest {

    private static final int MILK = 1;
// seed data: Milk, category Dairy (7 days)
    private static final LocalDate BOUGHT = LocalDate.of(2026, 10, 5);
    private final UserDAO users = new UserDAO();
    private final PantryItemDAO items = new PantryItemDAO();
    private Integer userId;

    @BeforeEach
    void createOwner() {
        userId = users.createUser(new User("ORM Item Tester", "orm-item"
                + System.nanoTime() + "@test.local", "hash"));
    }

    @AfterEach
    void removeOwner() {
        users.deleteUser(userId);           // cascade also removes the test items
    }

    private Integer add(LocalDate expiry, int quantity) {
        return items.addItem(userId, MILK, new PantryItem(BOUGHT, expiry,
                quantity));
    }

    // ---------- CREATE ----------
    @Test
    void addItem_withoutExpiry_usesTheCategoryShelfLife() {
        Integer id = add(null, 2);
        PantryItem saved = items.findByUser(userId).get(0);
        assertNotNull(id);
        assertEquals(LocalDate.of(2026, 10, 12), saved.getExpiryDate(),
                "2026-10-05 plus 7 days (Dairy)");
        assertEquals(ItemStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void addItem_withExpiry_keepsTheGivenDate() {
        add(LocalDate.of(2026, 11, 30), 1);
        assertEquals(LocalDate.of(2026, 11, 30),
                items.findByUser(userId).get(0).getExpiryDate());
    }

    @Test
    void addItem_unknownUser_throwsAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertThrows(DataAccessException.class,
                    () -> items.addItem(-1, MILK, new PantryItem(BOUGHT,
                            null, 1)));
            assertTrue(log.has(Level.WARN));
        }
    }

    @Test
    void addItem_unknownIngredient_throwsAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertThrows(DataAccessException.class,
                    () -> items.addItem(userId, -1, new PantryItem(BOUGHT,
                            null, 1)));
            assertTrue(log.has(Level.WARN));
        }
    }

    // ---------- READ ----------
    @Test
    void findByUser_loadsIngredientAndCategory() {
        add(null, 1);
        PantryItem item = items.findByUser(userId).get(0);
        // Safe after the Session closed, because the query used "join fetch".
        assertEquals("Milk", item.getIngredient().getIngredientName());
        assertEquals("Dairy", item.getIngredient().getCategory().getCategoryName());
    }

    @Test
    void findByUser_ordersBySoonestExpiry() {
        add(LocalDate.of(2026, 12, 1), 1);
        add(LocalDate.of(2026, 10, 20), 1);
        List<PantryItem> list = items.findByUser(userId);
        assertEquals(2, list.size());
        assertEquals(LocalDate.of(2026, 10, 20),
                list.get(0).getExpiryDate());
    }

    @Test
    void findExpiringBy_returnsOnlyActiveItemsUpToTheCutoff() {
        Integer id = add(LocalDate.of(2026, 10, 12), 1);
        assertTrue(items.findExpiringBy(userId, LocalDate.of(2026, 10, 11)).isEmpty());
        assertEquals(1, items.findExpiringBy(userId, LocalDate.of(2026, 10, 12)).size());
        items.markStatus(id, ItemStatus.CONSUMED);
        assertTrue(items.findExpiringBy(userId, LocalDate.of(2026, 10, 12)).isEmpty(),
                "consumed items are no longer expiring");
    }

    // ---------- UPDATE ----------
    @Test
    void updateQuantity_changesTheValue() {
        Integer id = add(null, 2);
        items.updateQuantity(id, 5);
        assertEquals(5, items.findByUser(userId).get(0).getQuantity());
    }

    @Test
    void updateQuantity_negativeNumberBecomesZero() {
        Integer id = add(null, 2);
        items.updateQuantity(id, -5);
        assertEquals(0, items.findByUser(userId).get(0).getQuantity());
    }

    @Test
    void markStatus_setsStatusAndStatusDate() {
        Integer id = add(null, 1);
        items.markStatus(id, ItemStatus.DISCARDED);
        PantryItem item = items.findByUser(userId).get(0);
        assertEquals(ItemStatus.DISCARDED, item.getStatus());
        assertEquals(LocalDate.now(), item.getStatusDate());
    }

    @Test
    void updateQuantity_unknownItem_throwsAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertThrows(DataAccessException.class, ()
                    -> items.updateQuantity(-1, 3));
            assertTrue(log.has(Level.WARN));
        }
    }

    // ---------- DELETE ----------
    @Test
    void deleteItem_removesTheRow() {
        Integer id = add(null, 1);
        assertTrue(items.deleteItem(id));
        assertTrue(items.findByUser(userId).isEmpty());
    }

    @Test
    void deleteItem_unknownId_returnsFalseAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertFalse(items.deleteItem(-1));
            assertTrue(log.has(Level.WARN, "Delete skipped"));
        }
    }

    @Test
    void deletingTheUser_alsoDeletesTheirItems() {
        add(null, 1);
        add(null, 2);
        assertTrue(users.deleteUser(userId));
        assertTrue(items.findByUser(userId).isEmpty(), "cascade = ALL removed the items first");
    }
}
