package com.lblw.vphx.phms.common.security.services;

import com.lblw.vphx.iams.securityauditengine.oauth.ApplicationName;
import com.lblw.vphx.iams.securityauditengine.oauth.OAuthClientCredentials;
import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * OAuthClientService connects with security-audit-engine-0.5.10 or above to generate the JWT Token
 * with Role = 'APP_PHMS'
 *
 * <p>The token generated is maintained as state and is reused till it expires and is regenerated
 * when expired.
 */
@Service
@Slf4j
public class OAuthClientService {

  // covers up the time delays in case of any issues with a mark of X seconds, defaults to 3 sec.
  private static final long ALLOWANCE_ERROR_TIME_MS = 3000;
  // state to maintain jwt
  private final Sinks.Many<String> jwtState = Sinks.many().replay().latestOrDefault(Strings.EMPTY);

  private final OAuthClientCredentials oAuthClientCredentials;
  private final InternalApiConfig internalApiConfig;
  /** Constructor */
  public OAuthClientService(
      OAuthClientCredentials oAuthClientCredentials, InternalApiConfig internalApiConfig) {
    this.oAuthClientCredentials = oAuthClientCredentials;
    this.internalApiConfig = internalApiConfig;
  }

  /**
   * Returns the valid JWT token, if the token is null or expired it generates the valid token
   *
   * @return {@link Mono<String> }
   */
  public Mono<String> getJWT() {

    return this.jwtState
        .asFlux()
        .flatMap(
            token -> {
              if (Strings.isBlank(token) || isJWTExpired(token)) {
                return generateJWT();
              }
              return Mono.just(token);
            })
        .next();
  }

  /**
   * Validates if token is expired or not
   *
   * @param jwt - {@link String}
   * @return {@link Boolean}
   */
  private boolean isJWTExpired(String jwt) {
    JWT parsedToken = null;
    Date expirationTime = null;
    try {
      parsedToken = JWTParser.parse(jwt);
      expirationTime = parsedToken.getJWTClaimsSet().getExpirationTime();

    } catch (ParseException e) {
      log.warn(
          "Parse Exception while validating expiry date for existing parsedToken, Error: {}",
          e.getMessage());
      return true;
    }
    // TODO: assuming expiration date is in UTC. Confirm with Rahul / Mitul

    Date now = new Date(Instant.now().toEpochMilli() - (ALLOWANCE_ERROR_TIME_MS * 100));
    return expirationTime == null || expirationTime.before(now);
  }

  /**
   * Calls security-audit-engine service to fetch the JWT .
   *
   * <p>for any Error case scenarios throws Runtime Exception with cause.
   *
   * @return {@link Mono}
   */
  private Mono<String> generateJWT() {
    return oAuthClientCredentials
        .generateJwtToken(
            ApplicationName.PHMS.getAppName(),
            internalApiConfig.getIam().getClientId(),
            internalApiConfig.getIam().getClientSecret(),
            internalApiConfig.getIam().getEnvironment())
        .doOnNext(this.jwtState::tryEmitNext)
        .doOnError(
            throwable -> {
              if (throwable instanceof ResponseStatusException) {
                ResponseStatusException responseStatusException =
                    (ResponseStatusException) throwable;
                log.error(
                    "Exception while generating JWT token, failed with error code: {}, and error message: {}",
                    responseStatusException.getRawStatusCode(),
                    responseStatusException.getMessage());
              } else {
                log.error(
                    "Internal Server Error while generating JWT token, error message: {} ",
                    throwable.getMessage());
                throw new ProvincialProcessorException(
                    "Error while fetching auth token", throwable);
              }
            });
  }
}
