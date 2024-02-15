package com.lblw.vphx.phms.domain.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.lblw.vphx.phms.domain.common.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {
  /**
   * Customizes default autoconfigured instance of {@link Jackson2ObjectMapperBuilder} which defines
   * how json is accepted in REST request and how JSON is rendered in response.
   *
   * @return {@link Jackson2ObjectMapperBuilder}
   */
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilder() {
    return builder ->
        builder
            .serializerByType(
                LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DateFormat.SYSTEM_DATE_FORMAT)))
            .deserializerByType(
                LocalDate.class,
                new LocalDateDeserializer(
                    DateTimeFormatter.ofPattern(DateFormat.SYSTEM_DATE_FORMAT)))
            .serializerByType(Instant.class, InstantSerializer.INSTANCE)
            .deserializerByType(Instant.class, InstantDeserializer.INSTANT)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToEnable(
                JsonParser.Feature.STRICT_DUPLICATE_DETECTION,
                DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
}
