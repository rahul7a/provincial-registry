package com.lblw.vphx.phms.domain.patient.prescription.creation.post.response;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class NonPrescribedPrescriptionCreationResult {
  private String provincialPrescriptionId;
}
