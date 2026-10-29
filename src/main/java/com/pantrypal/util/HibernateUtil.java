package com.pantrypal.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

    private static final Logger LOG =
            LogManager.getLogger(HibernateUtil.class);

    private static final SessionFactory SESSION_FACTORY =
            buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        try {
            LOG.info(
                    "Building Hibernate SessionFactory from hibernate.cfg.xml");

            SessionFactory factory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();

            LOG.info("Hibernate SessionFactory is ready");
            return factory;

        } catch (Throwable ex) {
            LOG.fatal(
                    "SessionFactory creation failed: the application cannot access the database",
                    ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null && SESSION_FACTORY.isOpen()) {
            SESSION_FACTORY.close();
            LOG.info("Hibernate SessionFactory closed");
        }
    }
}