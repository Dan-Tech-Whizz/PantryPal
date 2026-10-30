package com.pantrypal.dao;

import com.pantrypal.util.HibernateUtil;
import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.Transaction;

/** Every public DAO method runs in its own Session and its own transaction. 
*/
public abstract class BaseDAO {
    /** Runs work inside a transaction and returns a result. Rolls back on 
    any failure. */
    protected <T> T executeInTransaction(Function<Session, T> work) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            T result = work.apply(session);
            tx.commit();
            return result;
        } catch (DataAccessException ex) {
            rollbackQuietly(tx);
            throw ex;                         
        } catch (RuntimeException ex) {
            rollbackQuietly(tx);
            throw new DataAccessException("Database operation failed: " + ex.getMessage(), ex);
        } finally {
            session.close();
        }
    }
     
    protected void runInTransaction(Consumer<Session> work) {
        executeInTransaction(session -> {
            work.accept(session);
            return null;
        });
    }
    private void rollbackQuietly(Transaction tx) {
        try {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } catch (RuntimeException rollbackEx) {
            System.err.println("Rollback failed: " + rollbackEx.getMessage());
        }
    }
}
