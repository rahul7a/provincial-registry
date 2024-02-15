package com.lblw.vphx.phms.domain.common.logs;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import java.time.Instant;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530745916789&cot=14">Message</a>
 */
@Data
@NoArgsConstructor // no-arg constructor required for spring data rest
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "messages")
public class Message {

  @MongoId private ObjectId systemId;

  @Indexed(background = true)
  private MessageType messageType;

  @Indexed(background = true)
  private MessageProcess messageProcess;

  @Indexed(background = true)
  private String requestId;

  private Instant timestamp;
  private DomainEntity domainEntity;
  private Service source;
  private Service target;
  private Object data;
  private OperationOutcome operationOutcome;
  private MessagePayload messagePayload;
}
