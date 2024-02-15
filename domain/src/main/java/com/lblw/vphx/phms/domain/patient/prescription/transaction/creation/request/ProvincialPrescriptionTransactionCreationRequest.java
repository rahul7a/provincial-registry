package com.lblw.vphx.phms.domain.patient.prescription.transaction.creation.request;

import com.lblw.vphx.phms.domain.common.DomainRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** TODO: Add Miro Ref when available */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionCreationRequest extends DomainRequest {
  private String prescriptionSystemId;
  private String prescriptionTransactionSystemId;
}
