package com.lblw.vphx.phms.registry.controllers;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.LOG_COMPRESSION_ENABLED_CONTEXT;
import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.LOG_ENCRYPTION_ENABLED_CONTEXT;
import static com.lblw.vphx.phms.registry.configurations.MessageLogConfig.COMPRESSION_ENABLED_MESSAGE_PROCESS;
import static com.lblw.vphx.phms.registry.configurations.MessageLogConfig.ENCRYPTION_ENABLED_MESSAGE_PROCESS;

import com.lblw.vphx.phms.common.constants.OpenAPIConstants;
import com.lblw.vphx.phms.common.logs.MessageLogger;
import com.lblw.vphx.phms.configurations.OpenAPIConfiguration;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.logs.Message;
import com.lblw.vphx.phms.domain.common.logs.MessageType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/** Controller class exposing APIs for log messages */
@Slf4j
@RestController
public class MessageLogController {

  public static final String MESSAGE_LOG_DATA_SEARCH_URI = "/message-log-data/search";
  public static final String MESSAGE_LOG_TAGS = "Message log controller Open APIs";
  private final MessageLogger messageLogger;

  /**
   * @param messageLogger
   */
  public MessageLogController(MessageLogger messageLogger) {
    this.messageLogger = messageLogger;
  }

  /**
   * Endpoint for fetching the MessageLog using RequestId, MessageProcess and MessageType
   *
   * @return a Mono of {@link Message}
   */
  @GetMapping(value = MESSAGE_LOG_DATA_SEARCH_URI, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      security = {@SecurityRequirement(name = OpenAPIConfiguration.OPEN_API_SECURITY_KEY)},
      summary = "",
      tags = MESSAGE_LOG_TAGS,
      description = "")
  @ApiResponses(
      value = {@ApiResponse(responseCode = OpenAPIConstants.StatusCode.OK, description = "")})
  public Mono<ResponseEntity<Message>> fetchMessageLogs(
      @RequestParam(value = "requestId") String requestId,
      @RequestParam(value = "messageProcess") MessageProcess messageProcess,
      @RequestParam(value = "messageType") MessageType messageType) {

    boolean messageLogEncryptionEnabled =
        ENCRYPTION_ENABLED_MESSAGE_PROCESS.contains(messageProcess);
    boolean messageLogCompressionEnabled =
        COMPRESSION_ENABLED_MESSAGE_PROCESS.contains(messageProcess);
    return messageLogger
        .fetchMessage(requestId, messageProcess, messageType)
        .map(ResponseEntity::ok)
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
        .contextWrite(
            Context.of(
                Map.ofEntries(
                    Map.entry(MessageProcess.class, messageProcess),
                    Map.entry(LOG_ENCRYPTION_ENABLED_CONTEXT, messageLogEncryptionEnabled),
                    Map.entry(LOG_COMPRESSION_ENABLED_CONTEXT, messageLogCompressionEnabled))));
  }
}
