package com.marcowillemart.eventstore.embedded;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;

@ConfigurationProperties("eventstore.embedded")
public class EmbeddedEventStoreProperties {

    private boolean enabled;
    private final DataSourceProperties dataSourceProperties;

    public EmbeddedEventStoreProperties() {
        super();

        this.enabled = false;
        this.dataSourceProperties = new DataSourceProperties();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDriverClassName() {
        return dataSourceProperties.getDriverClassName();
    }

    public void setDriverClassName(String driverClassName) {
        dataSourceProperties.setDriverClassName(driverClassName);
    }

    public String getUrl() {
        return dataSourceProperties.getUrl();
    }

    public void setUrl(String url) {
        dataSourceProperties.setUrl(url);
    }

    public String getUsername() {
        return dataSourceProperties.getUsername();
    }

    public void setUsername(String username) {
        dataSourceProperties.setUsername(username);
    }

    public String getPassword() {
        return dataSourceProperties.getPassword();
    }

    public void setPassword(String password) {
        dataSourceProperties.setPassword(password);
    }

    public DataSourceBuilder<?> initializeDataSourceBuilder() {
        return dataSourceProperties.initializeDataSourceBuilder();
    }
}
