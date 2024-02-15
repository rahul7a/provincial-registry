package com.lblw.vphx.phms.common.messaging.pubsub;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Generic Publishing template to publish message to Google Pub/Sub. */
@Slf4j
@RequiredArgsConstructor
public abstract class InternalPubSubPublisherTemplate {

  private final PubSubTemplate pubSubTemplate;

  /**
   * @return topic name {@link String}
   */
  protected abstract String topic();

  /**
   * Method to publish message to underlying implementing topic
   *
   * @param message {@link String} message to be published
   */
  public void publish(byte[] message) {
    pubSubTemplate.publish(topic(), message);
  }
}
