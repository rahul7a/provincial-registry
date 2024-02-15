package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.common.response.ServiceCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530747123617&cot=14">Service</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Service {
  @Schema(description = "Code", example = "VPHX_PHMS")
  private ServiceCode code;

  private String serviceOutageStatus;
}
