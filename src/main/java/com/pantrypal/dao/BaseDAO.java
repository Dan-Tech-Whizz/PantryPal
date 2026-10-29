package com.pantrypal.dao;

import com.pantrypal.util.HibernateUtil;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;


public abstract class BaseDAO {

    protected final Logger log = LogManager.getLogger(getClass());

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

            log.warn("Transaction rolled back: {}", ex.getMessage());
            throw ex;

        } catch (RuntimeException ex) {
            rollbackQuietly(tx);

            log.error(
                    "Database operation failed; transaction rolled back",
                    ex
            );

            throw new DataAccessException(
                    "Database operation failed: " + ex.getMessage(),
                    ex
            );

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
            log.error("Rollback failed", rollbackEx);
        }
    }
}