package com.lblw.vphx.phms.domain.patient.prescription.details.request;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.patient.prescription.PrescriptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class ProvincialPrescriptionDetailsSearchCriteria extends ProvincialRequest {
  @Schema(description = "Prescription type", required = true)
  private PrescriptionType provincialPrescriptionType;

  @Schema(description = "Provincial patient id", required = true)
  private String provincialPatientIdentifier;

  @Schema(description = "Patient first name", required = true)
  private String provincialPatientFirstName;

  @Schema(description = "Patient last name", required = true)
  private String provincialPatientLastName;

  @Schema(description = "Provincial prescription id", required = true)
  private String provincialPrescriptionId;

  @Schema(description = "Prescription version number")
  private String dataVersion;

  @Schema(description = "Identity signature, signed using the USB Key of the user")
  private String provincialIdentityCertificateSignature;
}
