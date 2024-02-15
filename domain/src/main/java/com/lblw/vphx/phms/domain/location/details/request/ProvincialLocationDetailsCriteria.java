package com.lblw.vphx.phms.domain.location.details.request;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class ProvincialLocationDetailsCriteria extends ProvincialRequest {
  @Schema(description = "Provincial location id", required = true)
  private String identifier;
}
