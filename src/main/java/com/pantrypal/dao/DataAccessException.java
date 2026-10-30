package com.pantrypal.dao;

/** 
 * Unchecked exception thrown by the DAO layer so callers (console or Swing) 
 * are not forced to depend on Hibernate's exception types. 
 */ 
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public DataAccessException(String message) { super(message); }
    public DataAccessException(String message, Throwable cause) { 
        super(message, cause); 
    }
}
