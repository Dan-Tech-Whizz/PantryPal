package com.pantrypal.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
/* Domain-object tests only: no database needed. */
@Suite
@SuiteDisplayName("PantryPal domain object tests")
@SelectPackages("com.pantrypal.domain")
public class DomainSuite {}
