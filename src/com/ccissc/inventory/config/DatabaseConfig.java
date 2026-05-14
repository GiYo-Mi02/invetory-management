package com.ccissc.inventory.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String PROPERTIES_PATH = "/com/ccissc/inventory/config.properties";
    private static DatabaseConfig instance;

    private final HikariDataSource dataSource;

    private DatabaseConfig() {
        Properties properties = loadProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.user"));
        config.setPassword(properties.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.size", "10")));
        config.setPoolName("CCISSCInventoryPool");
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);
        LOGGER.info("Database pool initialized");
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Database pool closed");
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = DatabaseConfig.class.getResourceAsStream(PROPERTIES_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("Missing config.properties at " + PROPERTIES_PATH);
            }
            properties.load(stream);
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load database config", ex);
        }
    }
}
