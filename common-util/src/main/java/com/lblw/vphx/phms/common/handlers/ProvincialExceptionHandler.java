package com.lblw.vphx.phms.common.handlers;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.LOG_TARGET_CONTEXT;

import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.exceptions.ProvincialTimeoutException;
import com.lblw.vphx.phms.domain.common.DomainResponse;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.common.response.ServiceCode;
import com.lblw.vphx.phms.domain.common.response.Source;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** GenericExceptionControllerAdvice allows to handle custom exception */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class ProvincialExceptionHandler {
  /**
   * Handles ProvincialTimeoutException and returns HTTP 200 response.
   *
   * @param provincialTimeoutException {@link ProvincialTimeoutException}
   * @return Mono of responseEntity with {@link HttpStatus#OK}
   */
  @ExceptionHandler(ProvincialTimeoutException.class)
  public final Mono<ResponseEntity<DomainResponse<Object>>>
      handleProvincialRegistryServiceException(
          ProvincialTimeoutException provincialTimeoutException) {
    return handleProvincialException(provincialTimeoutException);
  }

  /**
   * Handles all unhandled exceptions and returns HTTP 200 response.
   *
   * @param exception {@link ProvincialProcessorException}
   * @return Mono of responseEntity with {@link ProvincialResponse}
   */
  @ExceptionHandler(ProvincialProcessorException.class)
  public final Mono<ResponseEntity<DomainResponse<Object>>> handleException(
      ProvincialProcessorException exception) {
    return handleProvincialException(exception);
  }

  /**
   * Handles all exceptions based on operationOutcome and returns HTTP 200 response.
   *
   * @param exception {@link Exception}
   * @return Mono of responseEntity with {@link ProvincialResponse}
   */
  private Mono<ResponseEntity<DomainResponse<Object>>> handleProvincialException(
      Exception exception) {
    return Mono.deferContextual(Mono::just)
        .map(
            context -> {
              log.error("handling ProvincialException.", exception);
              var messageProcess = context.get(MessageProcess.class);
              var targetService =
                  context.getOrDefault(
                      LOG_TARGET_CONTEXT, Service.builder().code(ServiceCode.UNKNOWN).build());
              var outcomeMessage =
                  ExceptionUtils.getRootCause(exception)
                      .toString()
                      .concat(":")
                      .concat(ExceptionUtils.getRootCauseMessage(exception));
              OperationOutcome operationOutcome;
              if (exception instanceof ProvincialProcessorException) {
                operationOutcome =
                    OperationOutcome.buildOperationOutcomeForInternalSystemError(
                        Source.EXTERNAL, targetService.getCode(), messageProcess, outcomeMessage);
              } else {
                operationOutcome =
                    OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
                        Source.EXTERNAL, targetService.getCode(), messageProcess, outcomeMessage);
              }
              return ResponseEntity.ok(
                  DomainResponse.builder().operationOutcomes(List.of(operationOutcome)).build());
            });
  }
}
