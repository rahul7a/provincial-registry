package com.lblw.vphx.phms.domain.common.context;

import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764531439235307&cot=14">Message
 *     Payload Template</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class MessagePayloadTemplate {
  private String uri;
  private MessagePayloadTemplateType messagePayloadTemplateType;
}
