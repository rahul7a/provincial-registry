package com.lblw.vphx.phms.domain.location.details.response;

import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.location.ProvincialLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ProvincialLocationDetails extends ProvincialResponse {
  @Schema(description = "Provincial location details")
  private ProvincialLocation provincialLocation;
}
