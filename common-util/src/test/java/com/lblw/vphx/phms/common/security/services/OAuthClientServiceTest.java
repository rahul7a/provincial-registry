package com.lblw.vphx.phms.common.security.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.lblw.vphx.iams.securityauditengine.oauth.ApplicationName;
import com.lblw.vphx.iams.securityauditengine.oauth.OAuthClientCredentials;
import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.*;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(classes = OAuthClientService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OAuthClientServiceTest {

  @Autowired OAuthClientService oAuthClientService;
  @MockBean private OAuthClientCredentials oAuthClientCredentials;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  private InternalApiConfig internalApiConfig;
  /**
   * @return Expired dummy token
   */
  private String getExpiredToken() {
    return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxNDNhN2U0NS0xMTE4LTQ2OWMtOTFiZC0zZDBmNjgyNmJkZmQiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlL3YyLjAiLCJpYXQiOjE1Njg2OTM1NTcsIm5iZiI6MTU2ODY5MzU1NywiZXhwIjoxNTY4NjkzNTU3LCJhaW8iOiJFMlpnWUVqLzZmRDdldjJNOUdsUnlRN25WUDJmQUFBPSIsImF6cCI6ImZlZWM5ODA5LTljYTYtNDVjNi04NDc2LTIxOTE2NDBhZjU2NiIsImF6cGFjciI6IjEiLCJvaWQiOiIwYTRjMjY0ZC00MWM4LTQwODctYWNiZS04ZjlhZTIzM2VjOWUiLCJyaCI6IjAuQVJVQXFlZEJvX2VaejBtRFJXa1RuRy1QdmtWLU9oUVlFWnhHa2IwOUQyZ212ZjBWQUFBLiIsInJvbGVzIjpbIkFQUF9QSE1TIl0sInN1YiI6IjBhNGMyNjRkLTQxYzgtNDA4Ny1hY2JlLThmOWFlMjMzZWM5ZSIsInRpZCI6ImEzNDFlN2E5LTk5ZjctNDljZi04MzQ1LTY5MTM5YzZmOGZiZSIsInV0aSI6Ik9odU95MHJCcjBtcENtVlVSdUpYQUEiLCJ2ZXIiOiIyLjAifQ.MvXGxDzTArnRyoeJ8zhGpiwWURGK_brA4wcq6_xvTrE";
  }

  /**
   * @return Valid dummy token with expiry as 2050
   */
  private String getValidToken() {
    return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxNDNhN2U0NS0xMTE4LTQ2OWMtOTFiZC0zZDBmNjgyNmJkZmQiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlL3YyLjAiLCJpYXQiOjE2NjMzNTYwMjcsIm5iZiI6MTY2MzM1NjAyNywiZXhwIjoyNTQ3MDAwNzU3LCJhaW8iOiJFMlpnWUVqLzZmRDdldjJNOUdsUnlRN25WUDJmQUFBPSIsImF6cCI6ImZlZWM5ODA5LTljYTYtNDVjNi04NDc2LTIxOTE2NDBhZjU2NiIsImF6cGFjciI6IjEiLCJvaWQiOiIwYTRjMjY0ZC00MWM4LTQwODctYWNiZS04ZjlhZTIzM2VjOWUiLCJyaCI6IjAuQVJVQXFlZEJvX2VaejBtRFJXa1RuRy1QdmtWLU9oUVlFWnhHa2IwOUQyZ212ZjBWQUFBLiIsInJvbGVzIjpbIkFQUF9QSE1TIl0sInN1YiI6IjBhNGMyNjRkLTQxYzgtNDA4Ny1hY2JlLThmOWFlMjMzZWM5ZSIsInRpZCI6ImEzNDFlN2E5LTk5ZjctNDljZi04MzQ1LTY5MTM5YzZmOGZiZSIsInV0aSI6Ik9odU95MHJCcjBtcENtVlVSdUpYQUEiLCJ2ZXIiOiIyLjAifQ.p8HHAimAlMIOsgc8sPPyH2lIjIu8Q40w3Igl6Q28ecw";
  }

  /**
   * @return InvalidToken which can not be parsed
   */
  private String getInvalidToken() {
    return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxNDNhN2U0NS0xMTE4LTQ2OWMtOTFiZC0zZDBmNjgyNmJkZmQiLCJpc3MiOiJodHRwczovL2xJFMlpnWUVqLzZmRDdldjJNOUdsUnlRN25WUDJmQUFBPSIsImF6cCI6ImZlZWM5ODA5LTljYTYtNDVjNi04NDc2LTIxOTE2NDBhZjU2NiIsImF6cGFjciI6IjEiLCJvaWQiOiIwYTRjMjY0ZC00MWM4LTQwODctYWNiZS04ZjlhZTIzM2VjOWUiLCJyaCI6IjAuQVJVQXFlZEJvX2VaejBtRFJXa1RuRy1QdmtWLU9oUVlFWnhHa2IwOUQyZ212ZjBWQUFBLiIsInJvbGVzIjpbIkFQUF9QSE1TIl0sInN1YiI6IjBhNGMyNjRkLTQxYzgtNDA4Ny1hY2JlLThmOWFlMjMzZWM5ZSIsInRpZCI6ImEzNDFlN2E5LTk5ZjctNDljZi04MzQ1LTY5MTM5YzZmOGZiZSIsInV0aSI6Ik9odU95MHJCcjBtcENtVlVSdUpYQUEiLCJ2ZXIiOiIyLjAifQ.p8HHAimAlMIOsgc8sP40w3Igl6Q28ecw";
  }

  @BeforeEach
  void beforeEach() {
    when(internalApiConfig.getIam().getEnvironment()).thenReturn("dev");
    when(internalApiConfig.getIam().getClientSecret()).thenReturn("clientSecret");
    when(internalApiConfig.getIam().getClientId()).thenReturn("clientid");
  }

  @Test
  @Order(0)
  @DisplayName("Test to fetch the token for first time")
  void testGetToken() {

    Mono<String> stubbedValidToken = Mono.just(getValidToken());

    // Stubs valid token
    when(oAuthClientCredentials.generateJwtToken(
            ApplicationName.PHMS.getAppName(), "clientid", "clientSecret", "dev"))
        .thenReturn(stubbedValidToken);

    StepVerifier.create(oAuthClientService.getJWT()).expectNext(getValidToken()).verifyComplete();
  }

  @Test
  @Order(1)
  @DisplayName(
      "Test to check if token is already fetched once, and is valid, then same token is returned")
  // dependent test on Test ( Order = 0 ).
  void givenTokenAlreadyFetchedAndIsNotExpired_whenGetToken_thenReturnSameToken() {

    // Fetch the token again after testGetToken().
    StepVerifier.create(oAuthClientService.getJWT()).expectNext(getValidToken()).verifyComplete();

    // Validate oAuthClientCredentials.generateJwtToken() method is not called at all.
    Mockito.verify(oAuthClientCredentials, Mockito.times(0))
        .generateJwtToken("provinces", "clientid", "clientSecret", "dev");
  }

  @Test
  @DisplayName(
      "Test to check if token is already fetched once, and is expired, then new token is returned")
  void givenTokenAlreadyFetchedAndIsExpired_whenGetToken_thenReturnNewToken() {

    // Stubbing invalid token
    Mono<String> stubbedExpiredToken = Mono.just(getExpiredToken());

    // Resets the jwtState to expiredToken
    ReflectionTestUtils.setField(
        oAuthClientService, "jwtState", Sinks.many().replay().latestOrDefault(getExpiredToken()));

    // System refetches the token from oAuthClientCredentials.generateJwtToken()

    Mono<String> stubbedValidToken = Mono.just(getValidToken());
    when(oAuthClientCredentials.generateJwtToken(
            ApplicationName.PHMS.getAppName(), "clientid", "clientSecret", "dev"))
        .thenReturn(stubbedValidToken);

    StepVerifier.create(oAuthClientService.getJWT()).expectNext(getValidToken()).verifyComplete();

    // Validate oAuthClientCredentials.generateJwtToken() method is called twice.

    Mockito.verify(oAuthClientCredentials, Mockito.times(1))
        .generateJwtToken(ApplicationName.PHMS.getAppName(), "clientid", "clientSecret", "dev");
  }

  @Test
  @DisplayName(
      "Test to check if token is already fetched once, and is tampered , then new token is returned")
  void givenTokenAlreadyFetchedAndIsNowInvalid_whenGetToken_thenReturnNewToken() {

    // Resets the jwtState to invalid token
    ReflectionTestUtils.setField(
        oAuthClientService, "jwtState", Sinks.many().replay().latestOrDefault(getInvalidToken()));

    // System fetches the token from oAuthClientCredentials.generateJwtToken()

    Mono<String> stubbedValidToken = Mono.just(getValidToken());
    when(oAuthClientCredentials.generateJwtToken(
            ApplicationName.PHMS.getAppName(), "clientid", "clientSecret", "dev"))
        .thenReturn(stubbedValidToken);

    StepVerifier.create(oAuthClientService.getJWT()).expectNext(getValidToken()).verifyComplete();

    // Validate oAuthClientCredentials.generateJwtToken() method is called twice.

    Mockito.verify(oAuthClientCredentials, Mockito.times(1))
        .generateJwtToken(ApplicationName.PHMS.getAppName(), "clientid", "clientSecret", "dev");
  }

  @Test
  @DisplayName(
      "Test to validate when application name argument is wrong for OAuthClientCredentials service")
  void givenInvalidApplicationName_whenGetToken_thenReturnResponseStatusExceptionWith500Status() {

    // Resets the jwtToken to blank
    ReflectionTestUtils.setField(
        oAuthClientService, "jwtState", Sinks.many().replay().latestOrDefault(Strings.EMPTY));

    IllegalArgumentException expectedException =
        new IllegalArgumentException("Failed with 5xx error");

    doReturn(Mono.error(new IllegalArgumentException("Failed with 5xx error")))
        .when(oAuthClientCredentials)
        .generateJwtToken(any(), any(), any(), any());

    StepVerifier.create(oAuthClientService.getJWT())
        .expectError(ProvincialProcessorException.class)
        .log()
        .verify();
  }

  @Test
  @DisplayName(
      "Test to validate when client id and client secret name argument are wrong for OAuthClientCredentials service")
  void
      givenInvalidClientIdOrClientSecret_whenGetToken_thenReturnResponseStatusExceptionWith400Status() {

    // Resets the jwtToken to blank
    ReflectionTestUtils.setField(
        oAuthClientService, "jwtState", Sinks.many().replay().latestOrDefault(Strings.EMPTY));

    ResponseStatusException expectedException =
        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed with 4xx error");

    doReturn(
            Mono.error(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed with 4xx error")))
        .when(oAuthClientCredentials)
        .generateJwtToken(any(), any(), any(), any());

    StepVerifier.create(oAuthClientService.getJWT())
        .expectError(ResponseStatusException.class)
        .log()
        .verify();
  }
}
