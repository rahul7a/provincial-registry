package com.lblw.vphx.phms.common.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/** Sets up MongoDB configurations. */
@Configuration
@EnableMongoRepositories(basePackages = {"com.lblw.vphx.phms.*"})
@EnableReactiveMongoRepositories(basePackages = {"com.lblw.vphx.phms.*"})
public class MongoConfig {
  @Bean
  public MongoCustomConversions customConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    return new MongoCustomConversions(converters);
  }
}
