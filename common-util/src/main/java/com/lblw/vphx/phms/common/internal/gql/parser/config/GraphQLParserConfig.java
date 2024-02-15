package com.lblw.vphx.phms.common.internal.gql.parser.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lblw.vphx.phms.common.internal.gql.parser.deserializer.PrescriptionTransactionDeserializer;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 *
 *
 * <h3>GraphQLParserConfig</h3>
 *
 * GraphQLParserConfig helps in configuring multiple GraphQLParser instances for serializing /
 * deserializing different domain objects using pre-configured ObjectMapper
 *
 * <p>To configure additional Parser for another different object thant configured below, implement
 * GraphQLParser with required ObjectMapper configuration / new modules and deserializing class
 * type.
 *
 * <p>DefaultParser shall be used for non configured domain class
 */
@Configuration
public class GraphQLParserConfig {

  /**
   * Builds the bean of {@link Map}<{@link Class}, {@link GraphQLParser}>
   *
   * @param graphQLParsers {@link Set<GraphQLParser>}
   * @return {@link Map}<{@link Class}, {@link GraphQLParser}>
   */
  @Bean
  public Map<Class<Object>, GraphQLParser> getGraphQLParsersMap(Set<GraphQLParser> graphQLParsers) {

    return graphQLParsers.stream()
        .collect(
            Collectors.toMap(GraphQLParser::getDeserializerType, graphQLParser -> graphQLParser));
  }

  /** GraphQL Parser interface to supply new ObjectMapper . */
  public interface GraphQLParser extends Supplier<ObjectMapper> {

    /**
     * To retrieve the {@link Class} of deserializing type.
     *
     * @return Class - used for key to identify the GraphQLParser
     */
    Class<Object> getDeserializerType();
  }

  /** Configuration for default parser */
  @Component
  public class DefaultParser implements GraphQLParser {
    private final ObjectMapper defaultObjectMapper;

    private final Class<?> deserializerType = DefaultParser.class;

    public DefaultParser() {

      this.defaultObjectMapper =
          new ObjectMapper()
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .registerModule(new JavaTimeModule())
              .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public ObjectMapper get() {
      return defaultObjectMapper;
    }

    @Override
    public Class getDeserializerType() {
      return this.deserializerType;
    }
  }

  /** Configuration for PrescritionTransactionParser with PrescriptionTransactionDeserializer */
  @Component
  class PrescriptionTransactionParser implements GraphQLParser {
    private final ObjectMapper prescriptionTransactionObjectMapper;

    private final Class<?> deserializerType = PrescriptionTransaction.class;

    public PrescriptionTransactionParser(DefaultParser defaultParser) {
      SimpleModule module = new SimpleModule();
      module.addDeserializer(
          PrescriptionTransaction.class,
          new PrescriptionTransactionDeserializer(defaultParser.get()));
      this.prescriptionTransactionObjectMapper =
          new ObjectMapper()
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .registerModule(new JavaTimeModule())
              .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
              .registerModule(module);
    }

    @Override
    public ObjectMapper get() {
      return prescriptionTransactionObjectMapper;
    }

    @Override
    public Class getDeserializerType() {
      return this.deserializerType;
    }
  }
}
