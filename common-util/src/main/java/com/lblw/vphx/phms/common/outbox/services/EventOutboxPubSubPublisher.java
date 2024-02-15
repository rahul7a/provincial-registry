package com.lblw.vphx.phms.common.outbox.services;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.lblw.vphx.phms.common.messaging.pubsub.InternalPubSubPublisherTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Event Outbox Google Pub/Sub publisher. */
@Component
public class EventOutboxPubSubPublisher extends InternalPubSubPublisherTemplate {

  // TODO : read from application-environment**.yaml when available. Defaulting it to hard-coded
  @Value(
      "${outbox.message.topic:projects/vph-phms-lwr/topics/phms-pe-test-outbox-transactional-topic}")
  private String outboxMessageTopic;

  /**
   * k Constructor
   *
   * @param pubSubTemplate {@link PubSubTemplate}
   */
  public EventOutboxPubSubPublisher(PubSubTemplate pubSubTemplate) {
    super(pubSubTemplate);
  }

  @Override
  protected String topic() {
    return outboxMessageTopic;
  }
}
