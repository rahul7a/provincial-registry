package com.lblw.vphx.phms.common.logs;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.LOG_COMPRESSION_ENABLED_CONTEXT;
import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.LOG_ENCRYPTION_ENABLED_CONTEXT;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.common.internal.compression.CompressionService;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.internal.dataprotection.DataProtectionService;
import com.lblw.vphx.phms.common.repositories.MessageRepository;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.logs.Message;
import com.lblw.vphx.phms.domain.common.logs.MessagePayload;
import com.lblw.vphx.phms.domain.common.logs.MessageType;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.prescription.request.hl7v3.ProvincialPrescriptionSearchRequest;
import com.lblw.vphx.phms.domain.patient.prescription.transaction.response.hl7v3.ProvincialPrescriptionTransactionSearchResponse;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import java.util.Base64;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.types.Binary;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@DataMongoTest
@ContextConfiguration(classes = {MongoConfig.class, MessageRepository.class, MessageLogger.class})
@EnableConfigurationProperties(value = {InternalApiConfig.class})
@AutoConfigureJson
@ActiveProfiles("test")
class MessageLoggerTest {
  private static final String TEST_REQUEST_ID = "test_id";
  private static final MessageType TEST_MESSAGE_TYPE = MessageType.DOMAIN_REQUEST;
  private static final MessageProcess TEST_MESSAGE_PROCESS = MessageProcess.LOCATION_SEARCH;

  @MockBean DataProtectionService dataProtectionService;
  @MockBean CompressionService compressionService;
  @SpyBean private MessageRepository messageRepository;
  @Autowired private MessageLogger messageLogger;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void beforeEach() {
    when(dataProtectionService.encrypt(any()))
        .thenAnswer(
            invocation ->
                Mono.just(ArrayUtils.addAll("encrypted ".getBytes(), invocation.getArgument(0))));
    when(compressionService.compress(any()))
        .thenAnswer(
            invocation ->
                Mono.just(ArrayUtils.addAll("compressed ".getBytes(), invocation.getArgument(0))));
    when(dataProtectionService.decrypt(any()))
        .thenAnswer(
            invocation -> {
              var byteArray = (byte[]) invocation.getArgument(0);
              return Mono.just(
                  ArrayUtils.subarray(byteArray, "encrypted ".getBytes().length, byteArray.length));
            });
    when(compressionService.decompress(any()))
        .thenAnswer(
            invocation -> {
              var byteArray = (byte[]) invocation.getArgument(0);
              return Mono.just(
                  ArrayUtils.subarray(
                      byteArray, "compressed ".getBytes().length, byteArray.length));
            });
    Schedulers.setFactory(new MessageLoggerTestSchedulers());
  }

  @AfterEach
  void afterEach() {
    Schedulers.resetFactory();
  }

  @Test
  @DirtiesContext
  void givenUnexpectedContext_thenSkipPersistMessageLog() {
    messageLogger.logMessage(
        Context.of(MessageProcess.class, MessageProcess.PRESCRIPTION_SEARCH),
        MessageType.DOMAIN_REQUEST,
        OperationOutcome.builder().build(),
        ProvincialPrescriptionSearchRequest.builder().build());

    verify(messageRepository, never()).save(any());
  }

  @Test
  @DirtiesContext
  void givenValidArguments_thenPersistMessageLog() {
    var onSaveSuccessful = Sinks.empty();

    doAnswer(
            invocation ->
                messageRepository
                    .insert((Message) invocation.getArgument(0))
                    .doOnSuccess((log) -> onSaveSuccessful.tryEmitEmpty()))
        .when(messageRepository)
        .save(any());

    var data = ProvincialPrescriptionSearchRequest.builder().build();
    var messagePayloadValue = "{\"test\":\"json\"}";

    messageLogger.logMessage(
        Context.of(
            ProvincialRequestControl.class,
            ProvincialRequestControl.builder()
                .requestId(TEST_REQUEST_ID)
                .province(Province.QC)
                .build(),
            MessageProcess.class,
            TEST_MESSAGE_PROCESS),
        TEST_MESSAGE_TYPE,
        OperationOutcome.builder().build(),
        data,
        MessagePayload.builder().value(messagePayloadValue).build());

    verify(messageRepository, times(1)).save(any());

    StepVerifier.create(onSaveSuccessful.asMono().then(messageRepository.findAll().take(1).next()))
        .assertNext(
            message -> {
              try {
                Assertions.assertEquals(
                    new Binary(objectMapper.writeValueAsBytes(data)), message.getData());
              } catch (JsonProcessingException e) {
                fail(e);
              }
              Assertions.assertEquals("test_id", message.getRequestId());
            })
        .verifyComplete();
  }

  @Test
  @DirtiesContext
  void whenPersistingFails_thenRecoversWithNoExceptions() {
    doReturn(Mono.error(new RuntimeException())).when(messageRepository).save(any());

    try {
      messageLogger.logMessage(
          Context.of(
              ProvincialRequestControl.class,
              ProvincialRequestControl.builder().province(Province.QC).build(),
              MessageProcess.class,
              MessageProcess.PRESCRIPTION_SEARCH),
          MessageType.DOMAIN_REQUEST,
          OperationOutcome.builder().build(),
          ProvincialPrescriptionSearchRequest.builder().build());
    } catch (Throwable t) {
      fail();
    }

    verify(messageRepository, times(1)).save(any());
    StepVerifier.create(messageRepository.save(any())).verifyError(RuntimeException.class);
  }

  static final class MessageLoggerTestSchedulers implements Schedulers.Factory {
    @Override
    public Scheduler newBoundedElastic(
        int threadCap, int taskCap, ThreadFactory threadFactory, int ttlSeconds) {
      return Schedulers.immediate();
    }

    @Override
    public Scheduler newParallel(int parallelism, ThreadFactory threadFactory) {
      return Schedulers.immediate();
    }

    @Override
    public Scheduler newSingle(ThreadFactory threadFactory) {
      return Schedulers.immediate();
    }
  }

  @Nested
  class TransformationsTests {
    private void assertOnMessageProcess(
        boolean enableEncryption,
        boolean enableCompression,
        Object data,
        String messagePayloadValue,
        Consumer<Message> assertion) {
      var onSaveSuccessful = Sinks.empty();

      doAnswer(
              invocation ->
                  messageRepository
                      .insert((Message) invocation.getArgument(0))
                      .doOnSuccess((log) -> onSaveSuccessful.tryEmitEmpty()))
          .when(messageRepository)
          .save(any());

      messageLogger.logMessage(
          Context.of(
              ProvincialRequestControl.class,
              ProvincialRequestControl.builder()
                  .requestId(TEST_REQUEST_ID)
                  .province(Province.QC)
                  .build(),
              MessageProcess.class,
              TEST_MESSAGE_PROCESS,
              LOG_ENCRYPTION_ENABLED_CONTEXT,
              enableEncryption,
              LOG_COMPRESSION_ENABLED_CONTEXT,
              enableCompression),
          MessageType.DOMAIN_REQUEST,
          OperationOutcome.builder().build(),
          data,
          MessagePayload.builder().value(messagePayloadValue).build());

      StepVerifier.create(onSaveSuccessful.asMono()).verifyComplete();

      StepVerifier.create(
              onSaveSuccessful.asMono().then(messageRepository.findAll().take(1).next()))
          .assertNext(assertion)
          .verifyComplete();
    }

    @Test
    @DirtiesContext
    void
        givenConfigurationWithNoCompressionAndNoEncryption_whenLogMessage_thenSkipsCompressionAndEncryption() {
      var data = ProvincialLocationSearchResponse.builder().build();
      var messagePayloadValue = "{\"test\": \"ProvincialLocationSearchResponse\"}";
      boolean enableEncryption = false;
      boolean enableCompression = false;

      assertOnMessageProcess(
          enableEncryption,
          enableCompression,
          data,
          messagePayloadValue,
          message -> {
            try {
              Assertions.assertArrayEquals(
                  objectMapper.writeValueAsBytes(data), ((Binary) message.getData()).getData());
              Assertions.assertArrayEquals(
                  messagePayloadValue.getBytes(),
                  ((Binary) message.getMessagePayload().getValue()).getData());
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          });
    }

    @Test
    @DirtiesContext
    void
        givenConfigurationWithOnlyCompressionAndNoEncryption_whenLogMessage_thenDoesCompressionAndSkipsEncryption() {
      var data = ProvincialPrescriptionTransactionSearchResponse.builder().build();
      var messagePayloadValue = "{\"test\": \"ProvincialPrescriptionTransactionSearchResponse\"}";
      boolean enableEncryption = false;
      boolean enableCompression = true;

      assertOnMessageProcess(
          enableEncryption,
          enableCompression,
          data,
          messagePayloadValue,
          message -> {
            try {
              Assertions.assertArrayEquals(
                  ArrayUtils.addAll("compressed ".getBytes(), objectMapper.writeValueAsBytes(data)),
                  ((Binary) message.getData()).getData());
              Assertions.assertArrayEquals(
                  ArrayUtils.addAll("compressed ".getBytes(), messagePayloadValue.getBytes()),
                  ((Binary) message.getMessagePayload().getValue()).getData());
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          });
    }

    @Test
    @DirtiesContext
    void
        givenConfigurationWithNoCompressionAndOnlyEncryption_whenLogMessage_thenSkipsCompressionAndDoesEncryption() {
      var data = ProvincialPatientConsentResponse.builder().build();
      var messagePayloadValue = "{\"test\": \"ProvincialPatientConsentResponse\"}";
      boolean enableEncryption = true;
      boolean enableCompression = false;

      assertOnMessageProcess(
          enableEncryption,
          enableCompression,
          data,
          messagePayloadValue,
          message -> {
            try {
              Assertions.assertArrayEquals(
                  ArrayUtils.addAll("encrypted ".getBytes(), objectMapper.writeValueAsBytes(data)),
                  ((Binary) message.getData()).getData());
              Assertions.assertArrayEquals(
                  ArrayUtils.addAll("encrypted ".getBytes(), messagePayloadValue.getBytes()),
                  ((Binary) message.getMessagePayload().getValue()).getData());
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          });
    }

    @Test
    @DirtiesContext
    void
        givenConfigurationWithCompressionAndEncryption_whenLogMessage_thenDoesCompressionAndEncryption() {
      var data = ProvincialPatientSearchResponse.builder().build();
      var messagePayloadValue = "{\"test\": \"ProvincialPatientSearchResponse\"}";
      var encoder = Base64.getEncoder();
      boolean enableEncryption = true;
      boolean enableCompression = true;

      assertOnMessageProcess(
          enableEncryption,
          enableCompression,
          data,
          messagePayloadValue,
          message -> {
            try {
              var compressedData =
                  ArrayUtils.addAll("compressed ".getBytes(), objectMapper.writeValueAsBytes(data));
              var compressedMessagePayload =
                  ArrayUtils.addAll("compressed ".getBytes(), messagePayloadValue.getBytes());
              Assertions.assertArrayEquals(
                  ArrayUtils.addAll("encrypted ".getBytes(), encoder.encode(compressedData)),
                  ((Binary) message.getData()).getData());
              Assertions.assertArrayEquals(
                  ArrayUtils.addAll(
                      "encrypted ".getBytes(), encoder.encode(compressedMessagePayload)),
                  ((Binary) message.getMessagePayload().getValue()).getData());
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          });
    }
  }

  @Nested
  class FetchMessageTests {
    private Mono<Void> logMessage(
        MessageProcess messageProcess, Object data, String messagePayloadValue) {
      var onSaveSuccessful = Sinks.empty();

      doAnswer(
              invocation ->
                  messageRepository
                      .insert((Message) invocation.getArgument(0))
                      .doOnSuccess((log) -> onSaveSuccessful.tryEmitEmpty()))
          .when(messageRepository)
          .save(any());

      messageLogger.logMessage(
          Context.of(
              ProvincialRequestControl.class,
              ProvincialRequestControl.builder()
                  .requestId(TEST_REQUEST_ID)
                  .province(Province.QC)
                  .build(),
              MessageProcess.class,
              messageProcess,
              LOG_ENCRYPTION_ENABLED_CONTEXT,
              true,
              LOG_COMPRESSION_ENABLED_CONTEXT,
              true),
          TEST_MESSAGE_TYPE,
          OperationOutcome.builder().build(),
          data,
          MessagePayload.builder().value(messagePayloadValue).build());

      return onSaveSuccessful.asMono().then();
    }

    @Test
    @DirtiesContext
    void
        givenConfigurationWithCompressionAndEncryption_whenfetchMessageAfterLogMessage_thenReturnsOriginal() {
      var data = ProvincialLocationSearchResponse.builder().build();
      var messagePayloadValue = "{\"test\": \"ProvincialLocationSearchResponse\"}";

      var logMessage = logMessage(TEST_MESSAGE_PROCESS, data, messagePayloadValue);
      var fetchMessage =
          messageLogger
              .fetchMessage(TEST_REQUEST_ID, TEST_MESSAGE_PROCESS, TEST_MESSAGE_TYPE)
              .contextWrite(
                  Context.of(
                      LOG_ENCRYPTION_ENABLED_CONTEXT, true, LOG_COMPRESSION_ENABLED_CONTEXT, true));
      var fetchMessageAfterLogMessage = logMessage.then(fetchMessage);

      StepVerifier.create(fetchMessageAfterLogMessage)
          .assertNext(
              message -> {
                try {
                  Assertions.assertEquals(objectMapper.writeValueAsString(data), message.getData());
                  Assertions.assertEquals(
                      messagePayloadValue, message.getMessagePayload().getValue());
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
              })
          .verifyComplete();
    }
  }
}
