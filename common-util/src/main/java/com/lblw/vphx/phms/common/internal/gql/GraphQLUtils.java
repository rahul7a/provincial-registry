package com.lblw.vphx.phms.common.internal.gql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.lblw.vphx.phms.common.constants.GQLConstants;
import com.lblw.vphx.phms.common.exceptions.InternalProcessorException;
import com.lblw.vphx.phms.common.internal.gql.parser.config.GraphQLParserConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GraphQLUtils {

  public static final String DEFAULT = "DEFAULT";
  private final Map<Class<Object>, GraphQLParserConfig.GraphQLParser> graphQLParsers;

  /** Constructor */
  public GraphQLUtils(Map<Class<Object>, GraphQLParserConfig.GraphQLParser> graphQLParsers) {
    this.graphQLParsers = graphQLParsers;
  }

  /**
   * This method is used to parse json node data in requested DTO class
   *
   * @param node {@link JsonNode}
   * @param responseClass {@link Class}
   * @return Object
   */
  public <T> T parseData(JsonNode node, Class<T> responseClass) {
    GraphQLParserConfig.GraphQLParser graphQLParser =
        graphQLParsers.getOrDefault(responseClass, getDefaultParser());

    return graphQLParser.get().convertValue(node, responseClass);
  }

  /**
   * This method is used to read graphql query and variables files
   *
   * @param fileName {@link String}
   */
  public String getSchemaFromFileName(final String fileName) {
    try {
      return new String(
          GraphQLUtils.class
              .getClassLoader()
              .getResourceAsStream(
                  GQLConstants.GRAPHQL_FOLDER + fileName + GQLConstants.GRAPHQL_FILE_EXTN)
              .readAllBytes(),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new InternalProcessorException(
          String.format(GQLConstants.GRAPHQL_FILE_NOT_FOUND_MESSAGE, fileName), e);
    }
  }

  /**
   * This method is used to convert json node data as text
   *
   * @param node {@link JsonNode}
   * @return String
   */
  public String convertToString(JsonNode node) {
    try {
      return getDefaultParser().get().writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new InternalProcessorException(e.getMessage(), e.getCause());
    }
  }
  /**
   * This method is used to convert string json data as expected response
   *
   * @param json {@link String}
   * @return T
   */
  public <T> T convertStringToObject(String json, Class<T> responseClass) {
    try {
      return parseData(getDefaultParser().get().readTree(json), responseClass);
    } catch (JsonProcessingException e) {
      throw new InternalProcessorException(e.getMessage(), e.getCause());
    }
  }

  /**
   * Fetches the default configured parser
   *
   * @return default parser {@link GraphQLParserConfig.GraphQLParser}
   */
  private GraphQLParserConfig.GraphQLParser getDefaultParser() {
    if (graphQLParsers.containsKey(GraphQLParserConfig.DefaultParser.class)) {
      return graphQLParsers.get(GraphQLParserConfig.DefaultParser.class);
    } else {
      throw new IllegalArgumentException("Default GraphQL Parser not configured");
    }
  }
}
