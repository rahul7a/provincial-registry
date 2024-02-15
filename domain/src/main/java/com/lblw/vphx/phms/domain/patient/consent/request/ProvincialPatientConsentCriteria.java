package com.lblw.vphx.phms.domain.patient.consent.request;

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
public class ProvincialPatientConsentCriteria extends ProvincialRequest {
  @Schema(description = "Patient first name", required = true)
  private String firstName;

  @Schema(description = "Patient last name", required = true)
  private String lastName;

  @Schema(description = "Provincial patient id", required = true)
  private String patientIdentifier;

  @Schema(description = "Effective date time", required = true)
  private String effectiveDateTime;
}
