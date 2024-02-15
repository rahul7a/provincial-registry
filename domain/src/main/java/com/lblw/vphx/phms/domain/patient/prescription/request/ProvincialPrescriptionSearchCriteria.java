package com.lblw.vphx.phms.domain.patient.prescription.request;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionSearchCriteria extends ProvincialRequest {
  @Schema(description = "Provincial patient id", required = true)
  private String provincialPatientIdentifier;

  @Schema(description = "Provincial patient first name", required = true)
  private String provincialPatientFirstName;

  @Schema(description = "Provincial patient last name", required = true)
  private String provincialPatientLastName;

  @Schema(description = "Provincial patient consent token")
  private String provincialPatientConsentToken;

  @Schema(description = "Administration period start date")
  private LocalDate administrationEffectivePeriodStartDate;

  @Schema(description = "Administration period end date")
  private LocalDate administrationEffectivePeriodEndDate;

  @Schema(description = "Identity signature, signed using the USB Key of the user")
  private String provincialIdentityCertificateSignature;

  @Schema(
      description = "Maximum number of records to be returned by the DIS",
      required = true,
      minimum = "1",
      maximum = "100")
  private Integer pageSize;
}
