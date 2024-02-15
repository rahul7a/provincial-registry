package com.lblw.vphx.phms.domain.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530754071336&cot=14">Response
 *     Status</a>
 *     <p>/** Status for an Operation Outcome {@link OperationOutcome}
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class ResponseStatus {
  @Schema(description = "Code", example = "ACCEPT")
  private Status code;

  @Schema(description = "Text")
  private String text;
}
