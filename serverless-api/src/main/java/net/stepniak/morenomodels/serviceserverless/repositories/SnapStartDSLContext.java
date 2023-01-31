package net.stepniak.morenomodels.serviceserverless.repositories;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import io.quarkus.runtime.Startup;
import lombok.Getter;
import net.stepniak.morenomodels.serviceserverless.Tables;
import net.stepniak.morenomodels.serviceserverless.configuration.SecretsManagerCredentialProvider;

@Startup
@ApplicationScoped
public class SnapStartDSLContext {

    @Inject
    Logger log;

    @Inject
    SecretsManagerCredentialProvider secretsManagerCredentialProvider;

    SecretsManagerCredentialProvider.SecretModel secretModel;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;

    @Getter
    DSLContext dslContext;


    @PostConstruct
    void init() {
        secretModel = secretsManagerCredentialProvider.getCredentials();
        dslContext = createContext();
        dslContext.selectFrom(Tables.MODELS).limit(1).execute();
    }

     private DSLContext createContext() {
        return DSL.using(new ConnectionProvider() {
            @Override
            public Connection acquire() throws DataAccessException {
                log.info("Acquiring connection...");
                Connection connection = getConnection(false);
                log.info("Acquired connection.");
                return connection;
            }

            @Override
            public void release(Connection connection) throws DataAccessException {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new DataAccessException("Failed to close JDBC connection", e);
                }
            }
        }, SQLDialect.POSTGRES);
    }

    private Connection getConnection(boolean wasRetried) {
        try {
            return DriverManager.getConnection(jdbcUrl, getConnectionProperties(secretModel));
        } catch (SQLException e) {
            log.warn("Failed to establish db connection with cached credentials", e);
            if (!wasRetried) {
                log.info("Refetching secret, because it may have been rotated...");
                secretModel = secretsManagerCredentialProvider.getCredentials();
                log.info("Refetched the secret.");
                return getConnection(true);
            } else {
                throw new DataAccessException("Failed to establish db connection", e);
            }
        }
    }


    private Properties getConnectionProperties(SecretsManagerCredentialProvider.SecretModel dbMetadata) {
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("user", dbMetadata.getUsername());
        connectionProperties.setProperty("password", dbMetadata.getPassword());
        return connectionProperties;
    }
}
