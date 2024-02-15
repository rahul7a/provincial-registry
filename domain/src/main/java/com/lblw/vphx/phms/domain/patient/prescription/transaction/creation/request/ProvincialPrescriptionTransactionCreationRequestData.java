package com.lblw.vphx.phms.domain.patient.prescription.transaction.creation.request;

import com.lblw.vphx.phms.domain.common.ProvincialRequestData;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** TODO: Add Miro Ref when available */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionCreationRequestData
    extends ProvincialRequestData<ProvincialPrescriptionTransactionCreationRequest> {
  PrescriptionTransaction prescriptionTransaction;
}
