package com.pantrypal.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
/** CRUD tests for the traditional JDBC implementation. Needs MySQL running. 
*/
@Suite
@SuiteDisplayName("PantryPal JDBC CRUD tests")
@SelectPackages("com.pantrypal.jdbc")
public class JdbcCrudSuite {}

