package com.lblw.vphx.phms.domain.patient.prescription.creation.post.request;

import com.lblw.vphx.phms.domain.common.DomainRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
// TODO: @NoArgsConstructor Required for serialisation; to be removed after
// dis-service-processor-merger
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NonPrescribedPrescriptionCreationRequest extends DomainRequest {
  private String prescriptionTransactionSystemId;
  private String prescriptionDataVersion;
}
