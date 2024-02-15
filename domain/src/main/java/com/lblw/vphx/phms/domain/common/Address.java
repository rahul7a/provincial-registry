package com.lblw.vphx.phms.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
  @Schema(description = "Street address line", example = "500 Lake Shore Blvd W")
  private String streetAddressLine;

  @Schema(description = "City", example = "Montreal")
  private String city;

  @Schema(description = "Province", example = "QC")
  private String province;

  @Schema(description = "Country", example = "CA")
  private String country;

  @Schema(description = "Postal code", example = "H0HH9X")
  private String postalCode;
}
