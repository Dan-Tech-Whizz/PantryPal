package com.pantrypal.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
/* Event-logging tests: appenders, layouts, loggers and one event per level. */
@Suite
@SuiteDisplayName("PantryPal logging tests")
@SelectPackages("com.pantrypal.logging")
public class LoggingSuite {}
