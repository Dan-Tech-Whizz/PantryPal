package com.pantrypal.jdbc;
import static org.junit.jupiter.api.Assertions.*;
import com.pantrypal.dao.DataAccessException;
import com.pantrypal.model.Ingredient;
import com.pantrypal.model.PantryItem;
import com.pantrypal.model.User;
import com.pantrypal.support.LogCapture;
import com.pantrypal.support.TestLogger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
/** CRUD tests for the traditional (JDBC) UserJdbcDAO. */
@ExtendWith(TestLogger.class)
class UserJdbcDaoTest {
    private final UserJdbcDAO dao = new UserJdbcDAO();
    private final List<Integer> created = new ArrayList<>();
    private static User newUser() {
        return new User("JDBC Tester", "jdbc" + System.nanoTime() + 
"@test.local", "hash");
    }
    private int save(User user) {
        int id = dao.create(user);
        created.add(id);
        return id;
    }
    @AfterEach
    void cleanUp() {
        created.forEach(dao::delete);
        created.clear();
    }
    // ---------- CREATE ----------
    @Test
    void create_assignsAnIdAndCopiesItIntoTheObject() {
        User user = newUser();
        try (LogCapture log = new LogCapture()) {
            int id = save(user);
            assertTrue(id > 0);
            assertEquals(id, user.getId());
            assertTrue(log.has(Level.INFO, "[JDBC] User created"));
        }
    }
    @Test
    void create_duplicateEmail_throwsAndLogsAnError() {
        User first = newUser();
        save(first);
        User duplicate = new User("Duplicate", first.getEmail(), "hash");
        try (LogCapture log = new LogCapture()) {
            assertThrows(DataAccessException.class, () -> 
dao.create(duplicate));
            assertTrue(log.has(Level.ERROR));
            assertNotNull(log.last(Level.ERROR).orElseThrow().getThrown());
        }
    }
    // ---------- READ ----------
    @Test
    void findById_returnsTheSavedUser() {
        User user = newUser();
        int id = save(user);
        User found = dao.findById(id).orElseThrow();
        assertEquals(user.getEmail(), found.getEmail());
        assertEquals("JDBC Tester", found.getName());
        assertTrue(found.getNotificationsEnabled());
        assertFalse(found.getAllowPurchaseSuggestions());
    }
    @Test
    void findById_unknownId_isEmptyAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertTrue(dao.findById(-1).isEmpty());
            assertTrue(log.has(Level.WARN, "No user found"));
        }
    }

    @Test
    void findAll_containsTheSavedUser() {
        int id = save(newUser());
        assertTrue(dao.findAll().stream().anyMatch(u -> u.getId() == id));
    }
    // ---------- UPDATE ----------
    @Test
    void update_changesTheStoredValues() {
        User user = newUser();
        int id = save(user);
        user.setName("Renamed Tester");
        user.setAllowPurchaseSuggestions(true);
        assertTrue(dao.update(user));
        User reloaded = dao.findById(id).orElseThrow();
        assertEquals("Renamed Tester", reloaded.getName());
        assertTrue(reloaded.getAllowPurchaseSuggestions());
    }
    @Test
    void update_unknownId_returnsFalseAndLogsAWarning() {
        User ghost = newUser();
        ghost.setId(-1);
        try (LogCapture log = new LogCapture()) {
            assertFalse(dao.update(ghost));
            assertTrue(log.has(Level.WARN, "Update skipped"));
        }
    }
    // ---------- DELETE ----------
    @Test
    void delete_removesTheUser() {
        int id = save(newUser());
        assertTrue(dao.delete(id));
        created.remove(Integer.valueOf(id));
        assertTrue(dao.findById(id).isEmpty());
    }
    @Test
    void delete_unknownId_returnsFalseAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertFalse(dao.delete(-1));
            assertTrue(log.has(Level.WARN, "Delete skipped"));
        }
    }
    @Test
    void delete_alsoRemovesThePantryItemsInOneTransaction() {
        int userId = save(newUser());
        PantryItemJdbcDAO itemDao = new PantryItemJdbcDAO();
        User owner = new User();
        owner.setId(userId);
        Ingredient milk = new Ingredient();
        milk.setId(1);
        PantryItem item = new PantryItem(LocalDate.of(2026, 10, 5), 
LocalDate.of(2026, 10, 12), 1);
        item.setUser(owner);
        item.setIngredient(milk);
        itemDao.create(item);
        assertTrue(dao.delete(userId));
        created.remove(Integer.valueOf(userId));
        assertTrue(itemDao.findByUser(userId).isEmpty());
    }
}
