package com.lblw.vphx.phms.common.logs;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.internal.compression.CompressionService;
import com.lblw.vphx.phms.common.internal.dataprotection.DataProtectionService;
import com.lblw.vphx.phms.common.repositories.MessageRepository;
import com.lblw.vphx.phms.common.repositories.PageListLogRepository;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplate;
import com.lblw.vphx.phms.domain.common.logs.DomainEntity;
import com.lblw.vphx.phms.domain.common.logs.Message;
import com.lblw.vphx.phms.domain.common.logs.MessagePayload;
import com.lblw.vphx.phms.domain.common.logs.MessageType;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.PageList;
import com.lblw.vphx.phms.domain.common.response.ServiceCode;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.bson.types.Binary;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;

/** A Service for building and logging a {@link Message} */
@org.springframework.stereotype.Service
@Slf4j
public class MessageLogger {
  private final MessageRepository messageRepository;
  private final PageListLogRepository pageListLogRepository;
  private final DataProtectionService dataProtectionService;
  private final CompressionService compressionService;
  private final ObjectMapper objectMapper;
  private final Base64.Encoder encoder;
  private final Base64.Decoder decoder;

  /**
   * Constructor
   *
   * @param messageRepository {@link MessageRepository
   * @param pageListLogRepository {@link PageListLogRepository}
   * @param dataProtectionService {@link DataProtectionService}
   * @param compressionService {@link CompressionService}
   * @param objectMapper {@link ObjectMapper}
   * @param internalApiConfig
   * @param decoder
   */
  public MessageLogger(
      MessageRepository messageRepository,
      PageListLogRepository pageListLogRepository,
      DataProtectionService dataProtectionService,
      CompressionService compressionService,
      ObjectMapper objectMapper) {
    this.messageRepository = messageRepository;
    this.pageListLogRepository = pageListLogRepository;
    this.dataProtectionService = dataProtectionService;
    this.compressionService = compressionService;
    this.objectMapper = objectMapper;
    this.encoder = Base64.getEncoder();
    this.decoder = Base64.getDecoder();
  }
  /**
   * Overloaded method for logging a message when message payload is absent
   *
   * @param contextView {@link ContextView} the reactive context invoking the logging
   * @param messageType {@link MessageType} message type for the data at log point
   * @param operationOutcome {@link OperationOutcome} operationOutcome at the time of logging
   * @param data data to be logged
   */
  public void logMessage(
      ContextView contextView,
      MessageType messageType,
      OperationOutcome operationOutcome,
      @Nullable Object data) {
    this.logMessage(contextView, messageType, operationOutcome, data, null);
  }
  /**
   * Builds and logs a {@link Message} wrapping a data argument.
   *
   * @param contextView {@link ContextView} the reactive context invoking the logging
   * @param messageType {@link MessageType} message type for the data at log point
   * @param operationOutcome {@link OperationOutcome} operationOutcome at the time of logging
   * @param data data to be logged
   * @param messagePayload {@link MessagePayload} the rawRequest or rawResponse of a transaction
   */
  public void logMessage(
      ContextView contextView,
      MessageType messageType,
      OperationOutcome operationOutcome,
      @Nullable Object data,
      @Nullable MessagePayload messagePayload) {
    Mono.deferContextual(
            context -> {
              var provincialRequestControl =
                  context.getOrDefault(
                      ProvincialRequestControl.class, (ProvincialRequestControl) null);
              var messageProcess =
                  context.getOrDefault(MessageProcess.class, (MessageProcess) null);
              var domainEntity = context.getOrDefault(DomainEntity.class, (DomainEntity) null);
              // TODO: serviceOutageStatus ,it's a required field, as of now we have set as empty
              var unknownSource =
                  Service.builder()
                      .code(ServiceCode.UNKNOWN)
                      .serviceOutageStatus(Strings.EMPTY)
                      .build();
              var source = context.getOrDefault(LOG_SOURCE_CONTEXT, unknownSource);
              var target = context.getOrDefault(LOG_TARGET_CONTEXT, unknownSource);

              Assert.notNull(
                  provincialRequestControl,
                  "Expecting provincialRequestControl. Possibly invoked from a non-reactive context?");
              Assert.notNull(
                  messageProcess,
                  "Expecting Type for Transaction. Possibly invoked from a non-reactive context?");

              return Mono.zip(
                      buildTransformation(data),
                      buildTransformation(
                          messagePayload == null ? null : messagePayload.getValue()))
                  .flatMap(
                      tuple -> {
                        byte[] encryptedMessageData = tuple.getT1();
                        byte[] encryptedMessagePayloadValue = tuple.getT2();

                        var message =
                            Message.builder()
                                .timestamp(
                                    DateUtils.getCurrentTimeStamp(
                                        provincialRequestControl.getProvince()))
                                .messageType(messageType)
                                .messageProcess(messageProcess)
                                .requestId(provincialRequestControl.getRequestId())
                                .domainEntity(domainEntity)
                                .source(source)
                                .target(target)
                                .data(encryptedMessageData)
                                .messagePayload(
                                    MessagePayload.builder()
                                        .value(encryptedMessagePayloadValue)
                                        .messagePayloadTemplate(
                                            MessagePayloadTemplate.builder().build())
                                        .build())
                                .operationOutcome(operationOutcome)
                                .build();
                        return messageRepository
                            .save(message)
                            .doOnError(
                                e ->
                                    log.error(
                                        MessageFormat.format(
                                            ErrorConstants
                                                .MESSAGE_LOGGING_SERVICE_EXCEPTION_FOR_BUILT_MESSAGE,
                                            message),
                                        e))
                            .onErrorResume((Throwable e) -> Mono.empty());
                      });
            })
        .onErrorContinue(
            (Throwable e, Object cause) ->
                log.error(ErrorConstants.MESSAGE_LOGGING_SERVICE_EXCEPTION, e))
        .contextWrite(contextView)
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
  }

  /**
   * Builds and logs a {@link PageListLog} wrapping a data argument for QYRC
   * transactions(Prescription , Prescription Transaction).
   *
   * @param contextView {@link ContextView} the reactive context invoking the logging
   * @param pageList {@link PageList} the rawRequest or rawResponse of a transaction
   */
  public void logPageList(ContextView contextView, PageList<?> pageList) {
    Mono.deferContextual(
            context -> {
              if (pageList.getQueryId() == null) {
                return Mono.empty();
              }
              var provincialRequestControl = context.get(ProvincialRequestControl.class);
              PageListLog pageListLog =
                  PageListLog.builder()
                      .provincialQueryId(pageList.getQueryId())
                      .createdAt(
                          DateUtils.getCurrentTimeStamp(provincialRequestControl.getProvince()))
                      .requestId(provincialRequestControl.getRequestId())
                      .currentRecordCount(pageList.getCurrentRecordCount())
                      .totalRecordCount(pageList.getTotalRecordCount())
                      .remainingRecordCount(pageList.getRemainingRecordCount())
                      .pageIndex(pageList.getPageIndex())
                      .pageSize(pageList.getPageSize())
                      .build();
              return pageListLogRepository.save(pageListLog);
            })
        .contextWrite(contextView)
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
  }

  /**
   * This method just serialize the data object
   *
   * @param data {@link Object}
   * @return A Mono of a byte array of serialized data.
   */
  private Mono<byte[]> serialise(@Nullable Object data) {
    if (data == null) {
      return Mono.just(new byte[0]);
    }
    return Mono.defer(
        () -> {
          if (data instanceof String) {
            return Mono.just(((String) data).getBytes());
          }

          try {
            return Mono.just(objectMapper.writeValueAsBytes(data));
          } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  private Mono<byte[]> buildTransformation(@Nullable Object data) {
    if (data == null) {
      return Mono.just(new byte[0]);
    }

    return Mono.deferContextual(
        contextView -> {
          boolean encryptionEnabled =
              Boolean.TRUE.equals(contextView.getOrDefault(LOG_ENCRYPTION_ENABLED_CONTEXT, false));
          boolean compressionEnabled =
              Boolean.TRUE.equals(contextView.getOrDefault(LOG_COMPRESSION_ENABLED_CONTEXT, false));

          Mono<byte[]> transformation = serialise(data);

          if (compressionEnabled) {
            transformation = transformation.flatMap(compressionService::compress);
          }

          if (compressionEnabled && encryptionEnabled) {
            transformation = transformation.map(encoder::encode);
          }

          if (encryptionEnabled) {
            transformation = transformation.flatMap(dataProtectionService::encrypt);
          }

          return transformation;
        });
  }

  private Mono<String> buildReverseTransformation(@Nullable byte[] data) {
    if (ArrayUtils.isEmpty(data)) {
      return Mono.just(StringUtils.EMPTY);
    }

    return Mono.deferContextual(
        contextView -> {
          boolean encryptionEnabled =
              Boolean.TRUE.equals(contextView.getOrDefault(LOG_ENCRYPTION_ENABLED_CONTEXT, false));
          boolean compressionEnabled =
              Boolean.TRUE.equals(contextView.getOrDefault(LOG_COMPRESSION_ENABLED_CONTEXT, false));

          var transformation = Mono.just(data);

          if (encryptionEnabled) {
            transformation = transformation.flatMap(dataProtectionService::decrypt);
          }

          if (compressionEnabled && encryptionEnabled) {
            transformation = transformation.map(decoder::decode);
          }

          if (compressionEnabled) {
            transformation = transformation.flatMap(compressionService::decompress);
          }

          return transformation.map(
              bytes -> new String(Objects.requireNonNull(bytes), StandardCharsets.UTF_8));
        });
  }

  /**
   * this method helps to fetch the specific message logs using requestId, messageProcess and
   * MessageType
   *
   * @param requestId
   * @param messageType
   * @param messageProcess
   * @return
   */
  public Mono<Message> fetchMessage(
      String requestId, MessageProcess messageProcess, MessageType messageType) {
    return Mono.deferContextual(
        context ->
            messageRepository
                .findByRequestIdAndMessageProcessAndMessageType(
                    requestId, String.valueOf(messageProcess), String.valueOf(messageType))
                .flatMap(
                    messageData ->
                        Mono.zip(
                                buildReverseTransformation(
                                    ((Binary) messageData.getData()).getData()),
                                buildReverseTransformation(
                                    ((Binary) messageData.getMessagePayload().getValue())
                                        .getData()))
                            .map(
                                tuple -> {
                                  messageData.setData(tuple.getT1());
                                  messageData.getMessagePayload().setValue(tuple.getT2());
                                  return messageData;
                                })));
  }
}
