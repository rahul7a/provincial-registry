package com.lblw.vphx.phms.common.outbox.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.exceptions.InternalProcessorException;
import com.lblw.vphx.phms.common.outbox.entity.EventOutboxMessage;
import com.lblw.vphx.phms.common.outbox.repositories.EventOutboxMessageRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Outbox Service to save the transaction event message to Outbox and publish the message to GCP-
 * PubSub topic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventOutboxService {

  // TODO: move constant to error constant.
  private static final String EXCEPTION_WHILE_PROCESSING_PHMS_OUTBOX_MESSAGE =
      "Exception while processing phms outbox message ";

  private final EventOutboxPubSubPublisher eventOutboxMessagePublisher;

  private final EventOutboxMessageRepository eventOutboxMessageRepository;

  private final ObjectMapper objectMapper;

  /**
   * Process outbox message
   *
   * @param id {@link String} request id
   * @param eventType {@link String} event type corresponds to transction type
   * @param dataVersion {@link String} data version 1.0 / 2.0 based on the event of occurrence
   * @param message - {@link String} data message (Domain Response)
   * @return
   */
  public void processOutBoxMessage(
      String id, String eventType, String dataVersion, Object message) {

    EventOutboxMessage eventOutboxMessage =
        saveOutboxMessage(
            EventOutboxMessage.builder()
                .requestId(id)
                .eventTime(Instant.now())
                .eventType(eventType)
                .dataVersion(dataVersion)
                .data(message)
                .build());
    publishOutboxMessage(eventOutboxMessage);
  }

  /**
   * Publishes outbox message to mapped topic.
   *
   * @param eventOutboxMessage {@link EventOutboxMessage}
   */
  private void publishOutboxMessage(EventOutboxMessage eventOutboxMessage) {
    try {
      eventOutboxMessagePublisher.publish(objectMapper.writeValueAsBytes(eventOutboxMessage));
    } catch (JsonProcessingException e) {
      var errorMessage =
          ErrorConstants.CAN_NOT_CONVERT_EVENT_OUTBOX_MESSAGE_TO_BYTE.concat(e.getMessage());
      log.error(errorMessage);
      throw new InternalProcessorException(errorMessage);
    }
  }

  /**
   * Logs the outbox message
   *
   * @param eventOutboxMessage
   * @return {@link EventOutboxMessage}
   */
  private EventOutboxMessage saveOutboxMessage(EventOutboxMessage eventOutboxMessage) {
    return eventOutboxMessageRepository.insert(eventOutboxMessage);
  }
}
