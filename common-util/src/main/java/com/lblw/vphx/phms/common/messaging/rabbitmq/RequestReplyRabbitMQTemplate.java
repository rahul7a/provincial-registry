package com.lblw.vphx.phms.common.messaging.rabbitmq;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lblw.vphx.phms.common.messaging.RequestReplyTemplate;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * A RabbitMQ Template that implements request/reply semantics to be used by a client that is both a
 * request publisher and a reply subscriber.
 *
 * @param <R> the outbound data type.
 * @param <S> the reply data type.
 */
@Slf4j
public class RequestReplyRabbitMQTemplate<R, S>
    implements RequestReplyTemplate<R, S>, SmartLifecycle, MessageListener {
  public static final Duration DEFAULT_REPLY_TIMEOUT = Duration.ofSeconds(5);

  private final ConcurrentMap<String, Sinks.One<Message>> perRequestPublisherMap =
      new ConcurrentHashMap<>();
  private final RabbitTemplate rabbitTemplate;
  private final Class<S> replyType;
  private final String requestExchange;
  private final String requestRoutingKey;
  private Duration defaultReplyTimeout = DEFAULT_REPLY_TIMEOUT;
  private ObjectMapper objectMapper = new ObjectMapper();
  private boolean running;

  public RequestReplyRabbitMQTemplate(
      RabbitTemplate rabbitTemplate,
      String requestExchange,
      String requestRoutingKey,
      Class<S> replyType) {
    this.rabbitTemplate = rabbitTemplate;
    this.requestExchange = requestExchange;
    this.requestRoutingKey = requestRoutingKey;
    this.replyType = replyType;
  }

  public RequestReplyRabbitMQTemplate(
      RabbitTemplate rabbitTemplate,
      String requestExchange,
      String requestRoutingKey,
      Class<S> replyType,
      ObjectMapper objectMapper) {
    this.rabbitTemplate = rabbitTemplate;
    this.requestExchange = requestExchange;
    this.requestRoutingKey = requestRoutingKey;
    this.replyType = replyType;
    this.objectMapper = objectMapper;
    this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
  }

  /**
   * Set the reply timeout used if no replyTimeout is provided in the {@link #sendAndReceive(R,
   * String)} call.
   *
   * @param defaultReplyTimeout the timeout.
   */
  public void setDefaultReplyTimeout(Duration defaultReplyTimeout) {
    Assert.notNull(defaultReplyTimeout, "'defaultReplyTimeout' cannot be null");
    Assert.isTrue(defaultReplyTimeout.toMillis() >= 0, "'replyTimeout' must be >= 0");
    this.defaultReplyTimeout = defaultReplyTimeout;
  }

  @Override
  public Mono<S> sendAndReceive(R request, String requestId) {
    return sendAndReceive(request, requestId, new HashMap<>(), this.defaultReplyTimeout);
  }

  @Override
  public Mono<S> sendAndReceive(R request, String requestId, Map<String, String> attributes) {
    return sendAndReceive(request, requestId, attributes, this.defaultReplyTimeout);
  }

  @Override
  public Mono<S> sendAndReceive(
      R request, String requestId, Map<String, String> attributes, Duration replyTimeout) {
    var replySink = Sinks.<Message>one();
    perRequestPublisherMap.put(requestId, replySink);

    var requestMessage =
        MessageBuilder.withBody(request.toString().getBytes(StandardCharsets.UTF_8))
            .setContentType(APPLICATION_JSON)
            .build();

    var requestPublisher =
        Mono.fromRunnable(
                () -> rabbitTemplate.send(requestExchange, requestRoutingKey, requestMessage))
            .subscribeOn(Schedulers.boundedElastic());

    return requestPublisher.then(
        replySink
            .asMono()
            .map(
                message -> {
                  if (replyType.equals(String.class)) {
                    return new String(message.getBody());
                  }

                  try {
                    return objectMapper.readValue(message.getBody(), replyType);
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                })
            .cast(replyType)
            .doOnError(
                error -> {
                  log.error(
                      "Exception processing received response with requestId: {}, {}",
                      requestId,
                      error);
                  perRequestPublisherMap.remove(requestId);
                }));
  }

  @Override
  public void onMessage(Message message) {
    log.info("Received Message: {}", message.getMessageProperties());

    getRequestIdFromMessage(message)
        .ifPresentOrElse(
            replyMessageRequestId -> {
              if (perRequestPublisherMap.containsKey(replyMessageRequestId)) {
                log.info(
                    "Accepted Message with requestId: {}, {}",
                    replyMessageRequestId,
                    message.getMessageProperties());

                var replySink = perRequestPublisherMap.get(replyMessageRequestId);
                replySink.tryEmitValue(message);
                perRequestPublisherMap.remove(replyMessageRequestId);
                return;
              }

              log.info(
                  "Ignored Message with requestId: {}, {}",
                  replyMessageRequestId,
                  message.getMessageProperties());
            },
            () -> log.info("Ignored Message: {}", message.getMessageProperties()));
  }

  /**
   * TODO: To be replaced with extraction from sleuth baggage when implemented
   *
   * <p>This function extracts the correlation id from the message body
   *
   * @param message The message that was received from the queue.
   * @return Optional, requestId as string if found else empty.
   */
  private Optional<String> getRequestIdFromMessage(Message message) {
    var rawResponse = new String(message.getBody());
    Pattern pattern =
        Pattern.compile("\"hdaRequestId\":\\s*\"(?<requestId>.*?)\"", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(rawResponse);
    if (matcher.find()) {
      return Optional.ofNullable(matcher.group("requestId"));
    }
    return Optional.empty();
  }

  @Override
  public void start() {
    if (this.running) {
      return;
    }

    this.running = true;
  }

  @Override
  public void stop() {
    if (!this.running) {
      return;
    }

    this.perRequestPublisherMap.clear();
    this.running = false;
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }
}
