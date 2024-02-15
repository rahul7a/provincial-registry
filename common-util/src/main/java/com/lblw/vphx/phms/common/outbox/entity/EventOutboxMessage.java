package com.lblw.vphx.phms.common.outbox.entity;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Outbox event collection for PE responses. */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "eventOutbox")
public class EventOutboxMessage {

  // controlls removal of document after 2 days from event occurrence.
  // TODO: re look into expiry logic
  @Indexed(expireAfter = "2d", background = true)
  private Instant eventTime;

  private String eventType;

  @Indexed(background = true)
  private String requestId;

  private String dataVersion;
  private Object data;
}
