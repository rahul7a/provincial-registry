package com.lblw.vphx.phms.domain.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
/** LocationRequestAddress to use in Location Search criteria */
public class LocationAddress {
  @Schema(description = "Street address line 1", example = "500")
  private String streetAddressLine1;

  @Schema(description = "Street address line 2", example = "Lake Shore Blvd W")
  private String streetAddressLine2;

  @Schema(description = "Postal code", example = "H0HH9X")
  private String postalCode;

  @Schema(description = "City", example = "Montreal")
  private String city;

  @Schema(description = "Country", example = "CA")
  private String country;
}
