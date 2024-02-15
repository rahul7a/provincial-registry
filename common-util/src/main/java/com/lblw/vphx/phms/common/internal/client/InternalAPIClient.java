package com.lblw.vphx.phms.common.internal.client;

import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.constants.GQLConstants;
import com.lblw.vphx.phms.common.exceptions.InternalAPIClientException;
import com.lblw.vphx.phms.common.exceptions.InternalTimeoutException;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.internal.gql.GraphQLRequest;
import com.lblw.vphx.phms.common.internal.gql.GraphQLResponse;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
@Slf4j
public class InternalAPIClient {

  private final InternalApiConfig apiConfig;

  /**
   * public constructor
   *
   * @param apiConfig {@link InternalApiConfig}
   */
  public InternalAPIClient(InternalApiConfig apiConfig) {
    this.apiConfig = apiConfig;
  }

  /**
   * * This Method is used to make a post call to GQL API with provided parameters
   *
   * @param request {@link GraphQLRequest} query, variables, header and response class
   * @return {@link GraphQLResponse} Response
   */
  public Mono<GraphQLResponse> postGQL(GraphQLRequest request) {
    return buildWebClient(apiConfig.getGraphql().getUrl())
        .post()
        .headers(buildHttpHeaders(request.getHeaders()))
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                buildGQLRequestBody(request.getQuery(), request.getVariables())))
        .retrieve()
        .bodyToMono(GraphQLResponse.class)
        .onErrorMap(
            throwable -> {
              var cause = throwable.getCause();
              log.error(GQLConstants.EXCEPTION_OCCURRED_WHILE_CALLING_GQL, cause);
              return (cause instanceof ReadTimeoutException
                      || cause instanceof WriteTimeoutException
                      || cause instanceof ConnectTimeoutException)
                  ? new InternalTimeoutException(cause.getMessage(), throwable)
                  : new InternalAPIClientException(cause.getMessage(), throwable);
            });
  }

  /**
   * This Method is used to build webclient with required configuration
   *
   * @param baseUrl {@link String}
   * @return {@link WebClient}
   */
  private WebClient buildWebClient(String baseUrl) {
    return WebClient.builder()
        .clientConnector(
            buildReactorClientHttpConnector(
                apiConfig.getResponseTimeout(),
                apiConfig.getConnectTimeout(),
                apiConfig.getReadTimeout(),
                apiConfig.getWriteTimeout()))
        .filter(errorHandler())
        .baseUrl(baseUrl)
        .defaultHeaders(buildHttpHeaders())
        .build();
  }

  /**
   * This Method is used to build GQL request body
   *
   * @param query {@link String} GQL query
   * @param variables {@link String} GQL variables
   * @return {@link Map}
   */
  private Map<String, Object> buildGQLRequestBody(String query, String variables) {
    Map<String, Object> body = new HashMap<>();
    body.put(GQLConstants.QUERY, query);
    body.put(GQLConstants.VARIABLES, variables);
    return body;
  }

  /** This Method is used to handle exception */
  private ExchangeFilterFunction errorHandler() {
    return ExchangeFilterFunction.ofResponseProcessor(
        clientResponse -> {
          if (!clientResponse.statusCode().is2xxSuccessful()) {
            String responseErrorMessage =
                String.format(
                    ErrorConstants.INTERNAL_API_ERROR_MESSAGE,
                    clientResponse.statusCode().value(),
                    clientResponse.statusCode().getReasonPhrase());
            log.error(responseErrorMessage);
            return Mono.error(new InternalAPIClientException(responseErrorMessage));
          }
          return Mono.just(clientResponse);
        });
  }

  /**
   * This Method is used to configure timeouts in httpclient
   *
   * @param httpResponseTimeOut {@link Integer}
   * @param httpConnectionTimeOut {@link Integer}
   * @param httpReadTimeOut {@link Integer}
   * @param httpWriteTimeOut {@link Integer}
   * @return {@link ReactorClientHttpConnector}
   */
  private ReactorClientHttpConnector buildReactorClientHttpConnector(
      int httpResponseTimeOut,
      int httpConnectionTimeOut,
      int httpReadTimeOut,
      int httpWriteTimeOut) {

    return new ReactorClientHttpConnector(
        HttpClient.create()
            .responseTimeout(Duration.ofMillis(httpResponseTimeOut))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, httpConnectionTimeOut)
            .doOnConnected(
                connection ->
                    connection
                        .addHandlerLast(
                            new ReadTimeoutHandler(httpReadTimeOut, TimeUnit.MILLISECONDS))
                        .addHandlerLast(
                            new WriteTimeoutHandler(httpWriteTimeOut, TimeUnit.MILLISECONDS))));
  }
  /**
   * This Method is used to add all default headers
   *
   * @return {@link Consumer < HttpHeaders >}
   */
  private Consumer<HttpHeaders> buildHttpHeaders() {
    return headers -> headers.set(HttpHeaders.ACCEPT, String.valueOf(MediaType.APPLICATION_JSON));
  }

  /**
   * This Method is used to add all request custom headers
   *
   * @param headers {@link LinkedMultiValueMap}
   * @return {@link Consumer<HttpHeaders>}
   */
  private Consumer<HttpHeaders> buildHttpHeaders(LinkedMultiValueMap<String, String> headers) {
    return headers != null ? httpHeaders -> httpHeaders.addAll(headers) : HttpHeaders::clear;
  }
}
