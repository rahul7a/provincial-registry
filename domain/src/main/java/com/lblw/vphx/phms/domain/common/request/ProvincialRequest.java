package com.lblw.vphx.phms.domain.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class ProvincialRequest {
  @Schema(hidden = true)
  private ProvincialRequestControl provincialRequestControl;
}
