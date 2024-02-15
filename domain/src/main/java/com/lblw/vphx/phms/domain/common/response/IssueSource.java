package com.lblw.vphx.phms.domain.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530754270901&cot=14">Issue
 *     Source</a>
 *     <p>/** issue source for an issue request {@link Issue}
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class IssueSource {
  @Schema(description = "Code", example = "INTERNAL")
  private Source code;

  @Schema(description = "Text")
  private String text;
}
