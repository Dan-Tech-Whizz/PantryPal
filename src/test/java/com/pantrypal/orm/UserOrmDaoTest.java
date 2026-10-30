package com.pantrypal.orm;

import static org.junit.jupiter.api.Assertions.*;
import com.pantrypal.dao.DataAccessException;
import com.pantrypal.dao.UserDAO;
import com.pantrypal.model.User;
import com.pantrypal.support.LogCapture;
import com.pantrypal.support.TestLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/* CRUD tests for the Hibernate (ORM) UserDAO.*/
@ExtendWith(TestLogger.class)
class UserOrmDaoTest {

    private final UserDAO dao = new UserDAO();
    private final List<Integer> created = new ArrayList<>();

    private static User newUser() {
        return new User("ORM Tester", "orm" + System.nanoTime()
                + "@test.local", "hash");
    }

    private Integer save(User user) {
        Integer id = dao.createUser(user);
        created.add(id);                    // remembered so @AfterEach can delete it
        return id;
    }

    @AfterEach
    void cleanUp() {
        created.forEach(dao::deleteUser);
        created.clear();
    }

    // ---------- CREATE ----------
    @Test
    void createUser_assignsAGeneratedId() {
        try (LogCapture log = new LogCapture()) {
            Integer id = save(newUser());
            assertNotNull(id);
            assertTrue(id > 0);
            assertTrue(log.has(Level.INFO, "User created"));
        }
    }

    @Test
    void createUser_duplicateEmail_throwsAndLogsAnError() {
        User first = newUser();
        save(first);
        User duplicate = new User("Duplicate", first.getEmail(), "hash");
        try (LogCapture log = new LogCapture()) {
            DataAccessException ex = assertThrows(DataAccessException.class,
                    () -> dao.createUser(duplicate));
            assertNotNull(ex.getCause(), "the original Hibernate exception is kept as the cause");
            assertTrue(log.has(Level.ERROR), "a failed insert must be logged at ERROR");
            assertNotNull(log.last(Level.ERROR).orElseThrow().getThrown(), "with the stack trace");
        }
    }

    // ---------- READ ----------
    @Test
    void findById_returnsTheSavedUser() {
        User user = newUser();
        Integer id = save(user);
        Optional<User> found = dao.findById(id);
        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
        assertTrue(found.get().getNotificationsEnabled());
        assertTrue(found.get().getPantryItems().isEmpty());
    }

    @Test
    void findById_unknownId_isEmptyAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertTrue(dao.findById(-1).isEmpty());
            assertTrue(log.has(Level.WARN, "No user found"));
        }
    }

    @Test
    void findByEmail_findsTheUser() {
        User user = newUser();
        Integer id = save(user);
        assertEquals(id,
                dao.findByEmail(user.getEmail()).orElseThrow().getId());
    }

    @Test
    void findAll_containsTheSavedUser() {
        Integer id = save(newUser());
        assertTrue(dao.findAll().stream().anyMatch(u
                -> id.equals(u.getId())));
    }

    // ---------- UPDATE ----------
    @Test
    void updateSettings_changesBothFlags() {
        Integer id = save(newUser());
        try (LogCapture log = new LogCapture()) {
            dao.updateSettings(id, false, true);
            User reloaded = dao.findById(id).orElseThrow();
            assertFalse(reloaded.getNotificationsEnabled());
            assertTrue(reloaded.getAllowPurchaseSuggestions());
            assertTrue(log.has(Level.INFO, "Settings updated"));
        }
    }

    @Test
    void updateSettings_unknownUser_throwsAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertThrows(DataAccessException.class, ()
                    -> dao.updateSettings(-1, true, true));
            assertTrue(log.has(Level.WARN));
        }
    }

    // ---------- DELETE ----------
    @Test
    void deleteUser_removesTheRow() {
        Integer id = save(newUser());
        assertTrue(dao.deleteUser(id));
        created.remove(id);                 // already gone, nothing for @AfterEach to do
        assertTrue(dao.findById(id).isEmpty());
    }

    @Test
    void deleteUser_unknownId_returnsFalseAndLogsAWarning() {
        try (LogCapture log = new LogCapture()) {
            assertFalse(dao.deleteUser(-1));
            assertTrue(log.has(Level.WARN, "Delete skipped"));
        }
    }
}
