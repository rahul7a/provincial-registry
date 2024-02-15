package com.lblw.vphx.phms.domain.patient.prescription.transaction.continuation.request.hl7v3;

import com.lblw.vphx.phms.domain.common.request.hl7v3.continuation.ProvincialSearchContinuationRequest;
import com.lblw.vphx.phms.domain.patient.prescription.transaction.continuation.request.ProvincialPrescriptionTransactionSearchContinuationCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** Request specialisation for Provincial Prescription Transaction Search Continuation */
@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionSearchContinuationRequest
    extends ProvincialSearchContinuationRequest<
        ProvincialPrescriptionTransactionSearchContinuationCriteria> {}
