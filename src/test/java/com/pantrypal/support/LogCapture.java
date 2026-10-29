package com.pantrypal.support;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
/** Records log events in memory so a unit test can assert on them. */
public final class LogCapture implements AutoCloseable {
private final List<LogEvent> events = new CopyOnWriteArrayList<>();
private final LoggerContext context = LoggerContext.getContext(false);
private final LoggerConfig loggerConfig = 
context.getConfiguration().getLoggerConfig("com.pantrypal");
private final AbstractAppender appender;
public LogCapture() {
appender = new AbstractAppender("TestCapture-" + System.nanoTime(), 
null, null, true, Property.EMPTY_ARRAY) {
@Override
public void append(LogEvent event) {
events.add(event.toImmutable());    
// copy: Log4j reuses 
event objects
}
  };
        appender.start();
        loggerConfig.addAppender(appender, Level.ALL, null);
        context.updateLoggers();
    }
    /** True if at least one event of this level was logged. */
    public boolean has(Level level) {
        return events.stream().anyMatch(e -> e.getLevel().equals(level));
    }
    /** True if an event of this level whose message contains the text was 
logged. */
    public boolean has(Level level, String text) {
        return events.stream().anyMatch(e -> e.getLevel().equals(level)
                && e.getMessage().getFormattedMessage().contains(text));
    }
    /** The most recent event of this level. */
    public Optional<LogEvent> last(Level level) {
        return events.stream().filter(e -> 
e.getLevel().equals(level)).reduce((first, second) -> second);
    }
    public List<LogEvent> events() {
        return List.copyOf(events);
    }
    @Override
    public void close() {
        loggerConfig.removeAppender(appender.getName());
        appender.stop();
        context.updateLoggers();
    }
}