package com.varshith.coderunner_workers.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;


@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(dbUrl);
        ds.setUsername(dbUser);
        ds.setPassword(dbPassword);

        // Good production defaults
        ds.setMaximumPoolSize(20);
        ds.setMinimumIdle(5);
        ds.setIdleTimeout(30000);
        ds.setConnectionTimeout(20000);

        return ds;
    }
}

