package com.pantrypal.support;

import java.lang.reflect.Method;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class TestLogger implements BeforeEachCallback, TestWatcher, org.junit.jupiter.api.extension.Extension {

    // Inherits the "com.pantrypal" logger, so test events reach Console and File too.
    private static final Logger LOG
            = LogManager.getLogger("com.pantrypal.test");

    @Override
    public void beforeEach(ExtensionContext ctx) {
        LOG.info("START   {}", name(ctx));
    }

    @Override
    public void testSuccessful(ExtensionContext ctx) {
        LOG.info("PASSED  {}", name(ctx));
    }

    @Override
    public void testFailed(ExtensionContext ctx, Throwable cause) {
        LOG.error("FAILED  {}", name(ctx), cause);
    }

    @Override
    public void testAborted(ExtensionContext ctx, Throwable cause) {
        LOG.warn("ABORTED {} - {}", name(ctx), cause.getMessage());
    }

    @Override
    public void testDisabled(ExtensionContext ctx, Optional<String> reason) {
        LOG.warn("SKIPPED {} - {}", name(ctx), reason.orElse("no reason given"));
    }

    private static String name(ExtensionContext ctx) {
        String method = ctx.getTestMethod().map(Method::getName).orElse("(class)");
        return ctx.getRequiredTestClass().getSimpleName() + "." + method;
    }
}
