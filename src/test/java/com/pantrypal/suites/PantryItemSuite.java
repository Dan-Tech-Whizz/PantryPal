package com.pantrypal.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
/* Everything that tests PantryItem, across all three implementations. */
@Suite
@SuiteDisplayName("PantryPal pantry item tests (domain, ORM, JDBC)")
@SelectClasses(names = {
"com.pantrypal.domain.PantryItemTest",
"com.pantrypal.orm.PantryItemOrmDaoTest",
"com.pantrypal.jdbc.PantryItemJdbcDaoTest"
})
public class PantryItemSuite {}
