package com.sparc.knappsack.components.server;


import ch.qos.logback.classic.LoggerContext;
import com.hazelcast.core.Hazelcast;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * This class manages startup and shutdown procedures.
 */
@Component
public class WebAppContextListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(WebAppContextListener.class);

    public void contextInitialized(ServletContextEvent arg0) {
    }

    public final void contextDestroyed(ServletContextEvent arg0) {
        try {
            log.info("Shutting down web app");
            Thread.sleep(1000);
            log.info("Shutting down the dbWebServer");
            DBWebServer.stopServer();

            shutDownLogger();

            Thread.sleep(1000);
            deregisterDrivers();

            log.info("Shutting down Hazelcast");
            Hazelcast.shutdownAll();
        } catch (InterruptedException e) {
            log.error("InterruptedException while de-registering drivers.", e);
        }
    }

    private void shutDownLogger() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (loggerFactory instanceof LoggerContext) {
            log.info("Shutting down LoggerContext");
            LoggerContext context = (LoggerContext) loggerFactory;
            context.stop();
        }
    }

    private void deregisterDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log.info(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                log.error(String.format("Error deregistering driver %s", driver), e);
            }
        }
    }
}