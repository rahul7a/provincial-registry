package com.lblw.vphx.phms.domain.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530754270782&cot=14">Issue
 *     Priority</a>
 *     <p>/** issue priority for an issue request {@link Issue}
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class IssuePriority {
  @Schema(description = "A string which has at least one character", example = "ERROR")
  private Priority code;

  @Schema(description = "Text")
  private String text;
}
