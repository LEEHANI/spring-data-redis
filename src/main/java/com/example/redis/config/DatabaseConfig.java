package com.example.redis.config;

import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

  static {
    //Mysql DNS cache configuration must be invalidated due to HA.
    java.security.Security.setProperty("networkaddress.cache.ttl", "5");
    java.security.Security.setProperty("networkaddress.cache.negative.ttl", "5");
  }


  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  @Bean
  public DataSource dataSource() {
    return DataSourceBuilder.create().build();
  }

}
