package com.lblw.vphx.phms.domain.patient.prescription.creation.response;

import lombok.*;

/**
 * TODO: rename to ProvincialPrescriptionCreationOutcome once
 * hl7v3.ProvincialPrescriptionCreationOutcome is deprecated
 *
 * <p>TODO: Add Miro ref after above TODO is resolved
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class ProvincialPrescriptionCreationResult {
  private String provincialPrescriptionId;
  private String provincialPrescriptionTransactionId;
}
