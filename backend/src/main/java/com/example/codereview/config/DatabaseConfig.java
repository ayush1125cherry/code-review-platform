package com.example.codereview.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("postgres")
public class DatabaseConfig {

    @Value("${spring.datasource.url:#{null}}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String username;

    @Value("${spring.datasource.password:#{null}}")
    private String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        String url = datasourceUrl;
        String user = username;
        String pass = password;

        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                URI uri = new URI(url.replace("postgresql://", "http://").replace("postgres://", "http://"));
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    user = userInfo[0];
                    if (userInfo.length > 1) {
                        pass = userInfo[1];
                    }
                }
                int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                String path = uri.getPath();
                url = "jdbc:postgresql://" + uri.getHost() + ":" + port + path;
            } catch (Exception ignored) {
                if (!url.startsWith("jdbc:")) {
                    url = "jdbc:" + url;
                }
            }
        }

        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }
}
