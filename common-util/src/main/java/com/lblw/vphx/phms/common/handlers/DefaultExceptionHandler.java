package com.lblw.vphx.phms.common.handlers;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.LOG_TARGET_CONTEXT;

import com.lblw.vphx.phms.domain.common.DomainResponse;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ServiceCode;
import com.lblw.vphx.phms.domain.common.response.Source;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/** Handles application exceptions as a final/lowest-precedence catch-all handler */
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class DefaultExceptionHandler {
  /**
   * Handles all unhandled exceptions and returns HTTP 200 response.
   *
   * @param exception {@link Exception}
   * @return Mono of responseEntity with {@link DomainResponse}
   */
  @ExceptionHandler(Exception.class)
  public final Mono<ResponseEntity<DomainResponse<Object>>> handleException(
      Exception exception, ServerHttpRequest serverHttpRequest) {
    return Mono.deferContextual(Mono::just)
        .map(
            context -> {
              if (exception instanceof ResponseStatusException) {
                log.error("handling ResponseStatusException.", exception);
                throw (ResponseStatusException) exception;
              }

              log.error("handling RuntimeException.", exception);

              var outcomeMessage =
                  ExceptionUtils.getRootCause(exception)
                      .toString()
                      .concat(":")
                      .concat(ExceptionUtils.getRootCauseMessage(exception));

              var messageProcess = context.get(MessageProcess.class);
              var targetService =
                  context.getOrDefault(
                      LOG_TARGET_CONTEXT, Service.builder().code(ServiceCode.UNKNOWN).build());

              return ResponseEntity.ok(
                  DomainResponse.builder()
                      .operationOutcomes(
                          List.of(
                              OperationOutcome.buildOperationOutcomeForInternalSystemError(
                                  Source.INTERNAL,
                                  targetService.getCode(),
                                  messageProcess,
                                  outcomeMessage)))
                      .build());
            });
  }
}
