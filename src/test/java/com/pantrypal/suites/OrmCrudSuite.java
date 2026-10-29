package com.pantrypal.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
/** CRUD tests for the Hibernate (ORM) implementation. Needs MySQL running. 
*/
@Suite
@SuiteDisplayName("PantryPal ORM CRUD tests")
@SelectPackages("com.pantrypal.orm")
public class OrmCrudSuite {}
