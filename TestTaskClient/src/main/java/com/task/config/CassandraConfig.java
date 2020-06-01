package com.task.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;

/**
 * @author Dmitry Novikov
 * @since 2020-05-30
 */

@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    private static final String DEFAULT_CONTACT_POINTS = "localhost";
    private static final String DEFAULT_PORT = "9042";
    private static final String DEFAULT_KEYSPACE_NAME = "sentence_keyspace";
    private static final String DEFAULT_LOCAL_DATACENTER = "datacenter1";
    private static final String ENTITY_BASE_PACKAGES = "com.task.domain";

    @Autowired
    private Environment env;

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        String keyspaceName = env.getProperty("cassandra.keyspaceName", DEFAULT_KEYSPACE_NAME);
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(keyspaceName).ifNotExists();

        return Arrays.asList(specification);
    }

    @Override
    protected String getKeyspaceName() {
        return env.getProperty("cassandra.keyspaceName", DEFAULT_KEYSPACE_NAME);
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{ENTITY_BASE_PACKAGES};
    }

    @Override
    protected String getLocalDataCenter() {
        return env.getProperty("cassandra.localDataCenter", DEFAULT_LOCAL_DATACENTER);
    }

    @Override
    protected String getContactPoints() {
        return env.getProperty("cassandra.contact-points", DEFAULT_CONTACT_POINTS);
    }

    @Override
    protected int getPort() {
        return Integer.parseInt(env.getProperty("cassandra.port", DEFAULT_PORT));
    }

}
