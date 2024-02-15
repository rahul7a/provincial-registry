package com.lblw.vphx.phms.common.handlers;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.LOG_TARGET_CONTEXT;

import com.lblw.vphx.phms.common.exceptions.InternalAPIClientException;
import com.lblw.vphx.phms.common.exceptions.InternalProcessorException;
import com.lblw.vphx.phms.common.exceptions.InternalTimeoutException;
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

/** InternalExceptionHandler allows to handle internal timeout exception */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class InternalExceptionHandler {

  /**
   * Handles InternalExceptionHandler and returns HTTP 200 response.
   *
   * @param internalTimeoutException {@link InternalTimeoutException}
   * @return Mono of responseEntity with {@link HttpStatus#OK}
   */
  @ExceptionHandler(InternalTimeoutException.class)
  public final Mono<ResponseEntity<DomainResponse<Object>>> handleInternalRegistryServiceException(
      InternalTimeoutException internalTimeoutException) {
    return handleInternalException(internalTimeoutException);
  }

  /**
   * Handles all unhandled exceptions and returns HTTP 200 response.
   *
   * @param exception {@link InternalProcessorException}
   * @return Mono of responseEntity with {@link ProvincialResponse}
   */
  @ExceptionHandler(InternalAPIClientException.class)
  public final Mono<ResponseEntity<DomainResponse<Object>>> handleException(
      InternalAPIClientException exception) {
    return handleInternalException(exception);
  }
  /**
   * Handles all unhandled exceptions and returns HTTP 200 response.
   *
   * @param exception {@link InternalProcessorException}
   * @return Mono of responseEntity with {@link ProvincialResponse}
   */
  @ExceptionHandler(InternalProcessorException.class)
  public final Mono<ResponseEntity<DomainResponse<Object>>> handleException(
      InternalProcessorException exception) {
    return handleInternalException(exception);
  }
  /**
   * Handles all exceptions based on operationOutcome and returns HTTP 200 response.
   *
   * @param exception {@link Exception}
   * @return Mono of responseEntity with {@link ProvincialResponse}
   */
  public final Mono<ResponseEntity<DomainResponse<Object>>> handleInternalException(
      Exception exception) {
    return Mono.deferContextual(Mono::just)
        .map(
            context -> {
              log.error("handling InternalException.", exception);
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
              if (exception instanceof InternalTimeoutException) {
                operationOutcome =
                    OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
                        Source.INTERNAL, targetService.getCode(), messageProcess, outcomeMessage);
              } else {
                operationOutcome =
                    OperationOutcome.buildOperationOutcomeForInternalSystemError(
                        Source.INTERNAL, targetService.getCode(), messageProcess, outcomeMessage);
              }
              return ResponseEntity.ok(
                  DomainResponse.builder().operationOutcomes(List.of(operationOutcome)).build());
            });
  }
}
