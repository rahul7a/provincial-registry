package com.lblw.vphx.phms.domain.provider.request;

import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ProvincialProviderSearchCriteria extends ProvincialRequest {
  @Schema(description = "Provider's first name", required = true)
  private String firstName;

  @Schema(description = "Provider's last name", required = true)
  private String lastName;

  @Schema(description = "Provider gender")
  private Gender gender;

  @Schema(description = "Date of birth")
  private String dateOfBirth;

  @Schema(description = "Provider type", required = true)
  private ProviderIdentifierType providerIdentifierType;

  @Schema(description = "Provider value")
  private String providerIdentifierValue;

  @Schema(description = "Role code", required = true)
  private String roleCode;

  @Schema(description = "Role speciality code")
  private String roleSpecialityCode;

  @Schema(description = "Provider request address")
  private ProviderRequestAddress providerRequestAddress;

  @Schema(description = "Licensing province", required = true)
  private String licensingProvince;
}
