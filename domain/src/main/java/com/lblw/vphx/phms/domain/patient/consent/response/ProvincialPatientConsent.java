package com.lblw.vphx.phms.domain.patient.consent.response;

import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
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
public class ProvincialPatientConsent extends ProvincialResponse {
  private SystemIdentifier identifier;

  @Schema(description = "Consent token, as obtained from the 'Get Consent Directive Query' API")
  private String consentToken;

  @Schema(description = "Consent validity start date time", example = "2021-01-01T10:00:00Z")
  private Instant consentValidityStartDateTime;

  @Schema(description = "Consent validity end date time", example = "2021-02-02T10:00:00Z")
  private Instant consentValidityEndDateTime;
}
