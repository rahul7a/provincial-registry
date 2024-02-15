package com.lblw.vphx.phms.domain.patient.prescription.transaction.creation.response;

import lombok.*;

/** TODO: Add Miro Ref when available */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class ProvincialPrescriptionTransactionCreationResult {
  private String provincialPrescriptionId;
  private String provincialPrescriptionTransactionId;

  /** TODO: Add additional Model fields when available */
}
