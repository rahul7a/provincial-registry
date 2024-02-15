package com.lblw.vphx.phms.common.exceptions.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.exceptions.ProvincialTimeoutException;
import com.lblw.vphx.phms.common.handlers.ClientRequestExceptionHandler;
import com.lblw.vphx.phms.common.handlers.ProvincialExceptionHandler;
import com.lblw.vphx.phms.common.utils.ControllerUtils;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ServiceCode;
import com.lblw.vphx.phms.domain.common.response.Source;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@ExtendWith(MockitoExtension.class)
class ProvincialExceptionHandlerTest {
  private static final String BEARER_TOKEN =
      "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xjbGIyY2Rldi5iMmNsb2dpbi5jb20vYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlL3YyLjAvIiwiZXhwIjoxNjU1OTIwMjg2LCJuYmYiOjE2NTU5MTY2ODYsImF1ZCI6ImRmZjVjMTVmLWM2MzQtNGFlYi1hMGI0LWM0YTVhMTFlZDQ2ZSIsInN1YiI6IjY3YzRmYjUxLWVkYzMtNGMyMi04ZjM1LTZlYmVjM2YxMzE5MCIsInBoYXJtYWN5SWQiOiI2MTgwMzk0MmY1MDQzZDQ5ZjM1MjFkZjciLCJtYWNoaW5lTmFtZSI6IiIsInJvbGVzIjpbIk5VUlNFIl0sImxhbmd1YWdlQ29kZSI6IkZSRSIsInByb3ZpbmNlQ29kZSI6IlFDIiwidGlkIjoiYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlIiwiYXpwIjoiZGZmNWMxNWYtYzYzNC00YWViLWEwYjQtYzRhNWExMWVkNDZlIiwidmVyIjoiMS4wIiwiaWF0IjoxNjU1OTE2Njg2fQ.yNPwe_dXjC0hfOzOO3ylIQnu8LxuGL3zq3EJV0P-llA";

  @InjectMocks ClientRequestExceptionHandler clientRequestExceptionHandler;
  @InjectMocks ProvincialExceptionHandler provincialExceptionHandler;
  @Mock ControllerUtils controllerUtils;

  @Test
  void handleProvincialRegistryServiceExceptionTest() {

    ProvincialTimeoutException provincialTimeoutException = new ProvincialTimeoutException("");
    var messageProcess = MessageProcess.LOCATION_DETAILS;
    var provincialRequestControl = ProvincialRequestControl.builder().province(Province.QC).build();
    var expectedOperationOutcomes =
        List.of(
            OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
                Source.EXTERNAL,
                ServiceCode.UNKNOWN,
                messageProcess,
                ExceptionUtils.getRootCause(provincialTimeoutException)
                    .toString()
                    .concat(":")
                    .concat(ExceptionUtils.getRootCauseMessage(provincialTimeoutException))));
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
    httpHeaders.add("x-request-id", "requestid1234567");
    ServerHttpRequest serverHttpRequest =
        MockServerHttpRequest.post("foo/bar").headers(httpHeaders).build();
    var responseEntity =
        provincialExceptionHandler
            .handleProvincialRegistryServiceException(provincialTimeoutException)
            .contextWrite(
                Context.of(
                    ProvincialRequestControl.class,
                    provincialRequestControl,
                    MessageProcess.class,
                    messageProcess));
    StepVerifier.create(responseEntity)
        .assertNext(
            response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertThat(response.getBody().getOperationOutcomes())
                  .isEqualTo(expectedOperationOutcomes);
            })
        .verifyComplete();
  }

  @Test
  void handleExceptionTest() {
    ProvincialProcessorException provincialProcessorException =
        new ProvincialProcessorException("provincialProcessorException");
    var messageProcess = MessageProcess.LOCATION_DETAILS;
    var provincialRequestControl = ProvincialRequestControl.builder().province(Province.QC).build();
    var expectedOperationOutcomes =
        List.of(
            OperationOutcome.buildOperationOutcomeForInternalSystemError(
                Source.EXTERNAL,
                ServiceCode.UNKNOWN,
                messageProcess,
                ExceptionUtils.getRootCause(provincialProcessorException)
                    .toString()
                    .concat(":")
                    .concat(ExceptionUtils.getRootCauseMessage(provincialProcessorException))));
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
    httpHeaders.add("x-request-id", "requestid1234567");
    ServerHttpRequest serverHttpRequest =
        MockServerHttpRequest.post("foo/bar").headers(httpHeaders).build();
    var responseEntity =
        provincialExceptionHandler
            .handleException(provincialProcessorException)
            .contextWrite(
                Context.of(
                    ProvincialRequestControl.class,
                    provincialRequestControl,
                    MessageProcess.class,
                    messageProcess));
    StepVerifier.create(responseEntity)
        .assertNext(
            response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertThat(response.getBody().getOperationOutcomes())
                  .isEqualTo(expectedOperationOutcomes);
            })
        .verifyComplete();
  }

  @Test()
  void requestNotAcceptableExceptionTest() {
    ResponseStatusException expectedStatusException =
        new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
    var inputException = new ServerWebInputException("Error");
    try {
      clientRequestExceptionHandler.handleNotAcceptableExceptions(inputException);
    } catch (Exception e) {
      assertEquals(expectedStatusException.getMessage(), e.getMessage());
    }
  }

  @Test()
  void jwtExceptionTest() {
    ResponseStatusException expectedStatusException =
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, HttpHeaders.AUTHORIZATION);
    var jwtException = new JwtException("Error");
    try {
      clientRequestExceptionHandler.handleJwtException(jwtException);
    } catch (Exception e) {
      assertEquals(expectedStatusException.getMessage(), e.getMessage());
    }
  }

  @Test()
  void badRequestExceptionsTest() {
    ResponseStatusException expectedStatusException =
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, HttpHeaders.AUTHORIZATION);
    var jwtException = new JwtException("Error");
    try {
      clientRequestExceptionHandler.handleJwtException(jwtException);
    } catch (Exception e) {
      assertEquals(expectedStatusException.getMessage(), e.getMessage());
    }
  }
}
