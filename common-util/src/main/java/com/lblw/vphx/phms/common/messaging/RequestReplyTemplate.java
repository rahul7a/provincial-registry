package com.lblw.vphx.phms.common.messaging;

import java.time.Duration;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * A reactive generic interface to enable Request/Reply pattern. Underlying Messaging technology to
 * be abstracted away to interface implementations
 */
public interface RequestReplyTemplate<R, S> {
  /**
   * Send a request message and receive a reply message with a default timeout.
   *
   * @param request the message to send.
   * @param requestId the requestId for the message; to be also used for request-reply correlation.
   * @return a Mono of the response.
   */
  public Mono<S> sendAndReceive(R request, String requestId);

  /**
   * Send a request message and receive a reply message with provided attributes and default
   * timeout. Depending on underlying Messaging implementation the attributes can be transmitted as
   * headers (as in Kafka/RabbitMQ) or attributes (as in GCP PubSub)
   *
   * @param request the message to send.
   * @param requestId the requestId for the message; to be also used for request-reply correlation.
   * @param attributes custom attributes to be sent with message.
   * @return a Mono of the response.
   */
  public Mono<S> sendAndReceive(R request, String requestId, Map<String, String> attributes);

  /**
   * Send a request message and receive a reply message with provided attributes and timeout.
   * Depending on underlying Messaging implementation the attributes can be transmitted as * headers
   * (as in Kafka/RabbitMQ) or attributes (as in GCP PubSub)
   *
   * @param request the message to send.
   * @param requestId the requestId for the message; to be also used for request-reply correlation.
   * @param attributes custom attributes to be sent with message.
   * @param replyTimeout the reply timeout; if null, the default will be used.
   * @return a Mono of the response.
   */
  public Mono<S> sendAndReceive(
      R request, String requestId, Map<String, String> attributes, Duration replyTimeout);
}
