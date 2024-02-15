package com.lblw.vphx.phms.registry.filters;

import com.lblw.vphx.phms.common.constants.APIConstants;
import com.lblw.vphx.phms.common.constants.HeaderConstants;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationFilter implements WebFilter {

  private final WebFluxProperties webFluxProperties;

  /**
   * class constructor
   *
   * @param webFluxProperties {@link WebFluxProperties}
   */
  public AuthenticationFilter(WebFluxProperties webFluxProperties) {
    this.webFluxProperties = webFluxProperties;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    HttpHeaders headers = request.getHeaders();

    /* Uncomment following code to make HttpHeaders.AUTHORIZATION required */
    var path = request.getPath();
    var xRequestId = headers.getFirst(HeaderConstants.X_REQUEST_ID_HEADER);
    var bearerToken = headers.getFirst(HttpHeaders.AUTHORIZATION);

    boolean securedEndpoint =
        APIConstants.DSQ_ENDPOINTS.stream()
            .map(endPoint -> webFluxProperties.getBasePath().concat(endPoint))
            .collect(Collectors.toList())
            .contains(path.toString());

    if (securedEndpoint && bearerToken == null) {
      log.warn(
          "{} header not provided for {} request-id: {}",
          HttpHeaders.AUTHORIZATION,
          path,
          xRequestId);
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, HttpHeaders.AUTHORIZATION + " header is required!");
    }
    return chain.filter(exchange);
  }
}
