package com.pantrypal.logging;

import static org.junit.jupiter.api.Assertions.*;
import com.pantrypal.dao.DataAccessException;
import com.pantrypal.jdbc.JdbcUtil;
import com.pantrypal.support.LogCapture;
import com.pantrypal.support.TestLogger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLogger.class)
class LoggingEventsTest {

    private static final Logger LOG
            = LogManager.getLogger(LoggingEventsTest.class);
    private LogCapture log;

    @BeforeEach
    void startCapture() {
        log = new LogCapture();
    }

    @AfterEach
    void stopCapture() {
        log.close();
    }

    // ---------- Configuration: appenders, layouts, loggers ----------
    @Test
    void consoleAndFileAppendersAreConfigured() {
        Map<String, Appender> appenders
                = LoggerContext.getContext(false).getConfiguration().getAppenders();
        assertInstanceOf(ConsoleAppender.class, appenders.get("Console"));
        assertInstanceOf(FileAppender.class, appenders.get("File"));
    }

    @Test
    void eachAppenderHasItsOwnPatternLayout() {
        Map<String, Appender> appenders
                = LoggerContext.getContext(false).getConfiguration().getAppenders();
        PatternLayout console = assertInstanceOf(PatternLayout.class,
                appenders.get("Console").getLayout());
        PatternLayout file = assertInstanceOf(PatternLayout.class,
                appenders.get("File").getLayout());
        assertNotEquals(console.getConversionPattern(),
                file.getConversionPattern());
    }

    @Test
    void theLogFileExists() {
        LOG.info("Writing a line so the file is not empty");
        assertTrue(Files.exists(Path.of("logs", "pantrypal.log")));
    }

    @Test
    void loggersInheritTheirLevelsFromTheHierarchy() {

        assertTrue(LogManager.getLogger("com.pantrypal.dao.UserDAO").isDebugEnabled()
        );
        assertFalse(LogManager.getLogger("org.hibernate").isInfoEnabled());
        assertTrue(LogManager.getLogger("org.hibernate").isWarnEnabled());
    }

    // ---------- Events: information, warning, error, fatal ----------
    @Test
    void infoEventIsRecorded() {
        LOG.info("Pantry opened for user {}", 2);
        assertTrue(log.has(Level.INFO, "Pantry opened for user 2"));
    }

    @Test
    void warnEventIsRecorded() {
        LOG.warn("Milk expires in {} day(s)", 1);
        assertTrue(log.has(Level.WARN, "Milk expires in 1 day(s)"));
    }

    @Test
    void errorEventKeepsTheException() {
        try {
            Integer.parseInt("two");
        } catch (NumberFormatException ex) {
            LOG.error("Quantity must be a whole number", ex);
        }
        assertTrue(log.has(Level.ERROR, "Quantity must be a whole number"));
        assertInstanceOf(NumberFormatException.class,
                log.last(Level.ERROR).orElseThrow().getThrown());
    }

    @Test
    void fatalEventIsLoggedWhenTheDatabaseCannotBeReached() {
// Port 1 has no MySQL, so the connection is refused straight away.
        assertThrows(DataAccessException.class,
                ()
                -> JdbcUtil.getConnection("jdbc:mysql://localhost:1/pantrypal_db", "nobody",
                        "wrong"));
        assertTrue(log.has(Level.FATAL, "Cannot connect to the database"));
        assertNotNull(log.last(Level.FATAL).orElseThrow().getThrown());
    }
}
