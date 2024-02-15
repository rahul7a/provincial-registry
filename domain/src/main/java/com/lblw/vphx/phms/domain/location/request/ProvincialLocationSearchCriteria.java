package com.lblw.vphx.phms.domain.location.request;

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
public class ProvincialLocationSearchCriteria extends ProvincialRequest {
  @Schema(description = "Location name", required = true)
  private String locationName;

  @Schema(description = "Location type", required = true)
  private String locationType;

  @Schema(
      description = "Provincial location type code",
      required = true,
      example = "HOSP, OF, CHR, etc.")
  private LocationIdentifierType provincialLocationIdentifierType;

  @Schema(description = "Provincial location value", required = true)
  private String provincialLocationIdentifierValue;

  @Schema(description = "Location request address")
  private LocationRequestAddress locationRequestAddress;
}
