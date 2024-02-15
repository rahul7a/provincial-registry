package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.Coding;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProvincialProviderProfile {
  // TODO: To be removed in LTPHVP-24701
  private SystemIdentifier identifier;

  @Schema(description = "Provider's first name", example = "Florence")
  private String firstName;

  @Schema(description = "Provider's last name", example = "Adams")
  private String lastName;

  @Schema(description = "Provider gender", example = "female")
  private Gender gender;

  @Schema(description = "Provider role")
  private Coding providerRole;

  @Schema(description = "Provider speciality")
  private Coding providerSpeciality;

  private SystemIdentifier license;
  private SystemIdentifier billing;
  @Schema(description = "The social insurance number of the provider", nullable = true)
  private SystemIdentifier sin;

  @Schema(description = "Provider status", example = "ACTIVE")
  private String status;

  @Schema(description = "Effective date range")
  private EffectiveDateRange effectiveDateRange;

  private String mainPlaceIndicator;

  @Schema(description = "Complete address")
  private Address address;

  private SystemIdentifier location;
}
