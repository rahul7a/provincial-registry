package com.lblw.vphx.phms.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EffectiveDateRange {
  @Schema(description = "Effective date range - low", example = "2021-01-01")
  private Instant low;

  @Schema(description = "Effective date range - high", example = "2021-02-02")
  private Instant high;
}
