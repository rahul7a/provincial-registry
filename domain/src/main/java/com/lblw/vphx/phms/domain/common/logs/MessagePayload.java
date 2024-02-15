package com.lblw.vphx.phms.domain.common.logs;

import com.lblw.vphx.phms.domain.common.context.MessagePayloadTemplate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764531169531673&cot=14">Message
 *     Payload</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MessagePayload {
  private Object value;
  private MessagePayloadTemplate messagePayloadTemplate;
}
