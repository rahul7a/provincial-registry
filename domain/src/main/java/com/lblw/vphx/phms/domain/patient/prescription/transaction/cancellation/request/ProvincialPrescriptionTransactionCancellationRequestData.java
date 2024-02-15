package com.lblw.vphx.phms.domain.patient.prescription.transaction.cancellation.request;

import com.lblw.vphx.phms.domain.common.ProvincialRequestData;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** TODO : Add Miro Ref when available */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionCancellationRequestData
    extends ProvincialRequestData<ProvincialPrescriptionTransactionCancellationRequest> {
  PrescriptionTransaction prescriptionTransaction;
}
