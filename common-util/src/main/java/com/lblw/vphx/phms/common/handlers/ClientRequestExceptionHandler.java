package com.lblw.vphx.phms.common.handlers;

import com.lblw.vphx.phms.common.exceptions.RequestNotAcceptableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/** ClientRequestExceptionHandler handling 400 series of exceptions */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ClientRequestExceptionHandler implements WebExceptionHandler {

  /**
   * Handles all exceptions and returns respective error response.
   *
   * @param exchange {@link ServerWebExchange}
   * @param ex {@link Throwable}
   */
  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (ex instanceof JwtException) {
      handleJwtException(ex);
    } else if (ex instanceof ServerWebInputException) {
      handleBadRequestExceptions(ex);
    } else if (ex instanceof RequestNotAcceptableException) {
      handleNotAcceptableExceptions(ex);
    }
    return Mono.error(ex);
  }
  /**
   * Handles all Bad request exceptions and returns HTTP 400 response.
   *
   * @param exception {@link JwtException}
   */
  @ExceptionHandler(ServerWebInputException.class)
  public final void handleBadRequestExceptions(Throwable exception) {
    log.error("Error accepting Bad Request", ExceptionUtils.getStackTrace(exception));
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles all jwt exceptions and returns HTTP 401 response.
   *
   * @param exception {@link JwtException}
   */
  @ExceptionHandler(JwtException.class)
  public final void handleJwtException(Throwable exception) {
    log.error("Error reading JWT", ExceptionUtils.getStackTrace(exception));
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, HttpHeaders.AUTHORIZATION);
  }

  /**
   * Handles Province Not Acceptable and returns HTTP 406 response.
   *
   * @param exception {@link JwtException}
   */
  @ExceptionHandler(RequestNotAcceptableException.class)
  public final void handleNotAcceptableExceptions(Throwable exception) {
    log.error("Error accepting Not Acceptable Request", ExceptionUtils.getStackTrace(exception));
    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
  }
}
