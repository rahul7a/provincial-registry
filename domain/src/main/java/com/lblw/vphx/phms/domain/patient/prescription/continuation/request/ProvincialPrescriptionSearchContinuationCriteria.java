package com.lblw.vphx.phms.domain.patient.prescription.continuation.request;

import com.lblw.vphx.phms.domain.common.request.continuation.ProvincialSearchContinuationCriteria;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionSearchContinuationCriteria
    extends ProvincialSearchContinuationCriteria {
  @Schema(description = "Identity signature, signed using the USB Key of the user")
  private String provincialIdentityCertificateSignature;
}
