package com.pantrypal.jdbc;

import static org.junit.jupiter.api.Assertions.*;
import com.pantrypal.dao.DataAccessException;
import com.pantrypal.model.Ingredient;
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

/*CRUD tests for the traditional (JDBC) PantryItemJdbcDAO.*/
@ExtendWith(TestLogger.class)
class PantryItemJdbcDaoTest {

    private static final int MILK = 1;      // seed data: Milk, category Dairy
    private static final LocalDate BOUGHT = LocalDate.of(2026, 10, 5);
    private final UserJdbcDAO users = new UserJdbcDAO();
    private final PantryItemJdbcDAO items = new PantryItemJdbcDAO();
    private int userId;

    @BeforeEach
    void createOwner() {
        userId = users.create(new User("JDBC Item Tester", "jdbc-item"
                + System.nanoTime() + "@test.local", "hash"));
    }

    @AfterEach
    void removeOwner() {
        users.delete(userId);               // also removes the test items
    }

    private PantryItem newItem(int ownerId, LocalDate expiry, int quantity) {
        PantryItem item = new PantryItem(BOUGHT, expiry, quantity);
        User owner = new User();
        owner.setId(ownerId);
        Ingredient milk = new Ingredient();
        milk.setId(MILK);
        item.setUser(owner);
        item.setIngredient(milk);
        return item;
    }

    // ---------- CREATE ----------
    @Test
    void create_assignsAnId() {
        PantryItem item = newItem(userId, LocalDate.of(2026, 10, 12), 2);
        try (LogCapture log = new LogCapture()) {
            int id = items.create(item);
            assertTrue(id > 0);
            assertEquals(id, item.getId());
            assertTrue(log.has(Level.INFO, "[JDBC] Pantry item created"));
        }
    }

    @Test
    void create_unknownUser_throwsAndLogsAnError() {
        PantryItem orphan = newItem(-1, LocalDate.of(2026, 10, 12), 1);
        try (LogCapture log = new LogCapture()) {
            assertThrows(DataAccessException.class, ()
                    -> items.create(orphan));
            assertTrue(log.has(Level.ERROR), "the foreign key violation must be logged at ERROR");
        }
    }

    // ---------- READ ----------
    @Test
    void findById_returnsTheStoredValues() {
        int id = items.create(newItem(userId, LocalDate.of(2026, 10, 12), 2));
        PantryItem found = items.findById(id).orElseThrow();
        assertEquals("Milk", found.getIngredient().getIngredientName());
        assertEquals(LocalDate.of(2026, 10, 12), found.getExpiryDate());
        assertEquals(2, found.getQuantity());
        assertEquals(ItemStatus.ACTIVE, found.getStatus());
        assertNull(found.getReceipt());
        assertNull(found.getStatusDate());
    }

    @Test
    void findById_unknownId_isEmptyAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertTrue(items.findById(-1).isEmpty());
            assertTrue(log.has(Level.WARN, "No pantry item found"));
        }
    }

    @Test
    void findByUser_ordersBySoonestExpiry() {
        items.create(newItem(userId, LocalDate.of(2026, 12, 1), 1));
        items.create(newItem(userId, LocalDate.of(2026, 10, 20), 1));
        List<PantryItem> list = items.findByUser(userId);
        assertEquals(2, list.size());
        assertEquals(LocalDate.of(2026, 10, 20),
                list.get(0).getExpiryDate());
    }

    // ---------- UPDATE ----------
    @Test
    void updateQuantity_changesTheValue() {
        int id = items.create(newItem(userId, LocalDate.of(2026, 10, 12),
                2));
        assertTrue(items.updateQuantity(id, 7));
        assertEquals(7, items.findById(id).orElseThrow().getQuantity());
    }

    @Test
    void updateStatus_setsStatusAndStatusDate() {
        int id = items.create(newItem(userId, LocalDate.of(2026, 10, 12),
                1));
        assertTrue(items.updateStatus(id, ItemStatus.CONSUMED));
        PantryItem found = items.findById(id).orElseThrow();
        assertEquals(ItemStatus.CONSUMED, found.getStatus());
        assertEquals(LocalDate.now(), found.getStatusDate());
    }

    @Test
    void update_unknownItem_returnsFalseAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertFalse(items.updateQuantity(-1, 3));
            assertTrue(log.has(Level.WARN, "nothing changed"));
        }
    }
// ---------- DELETE ----------

    @Test
    void delete_removesTheRow() {
        int id = items.create(newItem(userId, LocalDate.of(2026, 10, 12), 1));

        assertTrue(items.delete(id));
        assertTrue(items.findById(id).isEmpty());
    }

    @Test
    void delete_unknownId_returnsFalseAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertFalse(items.delete(-1));
            assertTrue(log.has(Level.WARN));
        }
    }
}
