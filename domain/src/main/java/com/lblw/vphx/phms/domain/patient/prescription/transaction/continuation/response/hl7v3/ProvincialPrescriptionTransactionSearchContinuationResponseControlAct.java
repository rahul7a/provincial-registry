package com.lblw.vphx.phms.domain.patient.prescription.transaction.continuation.response.hl7v3;

import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.domain.patient.prescription.transaction.continuation.request.ProvincialPrescriptionTransactionSearchContinuationCriteria;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionSearchContinuationResponseControlAct
    extends ResponseControlAct {
  private ProvincialPrescriptionTransactionSearchContinuationCriteria
      provincialPrescriptionTransactionSearchContinuationCriteria;
}
