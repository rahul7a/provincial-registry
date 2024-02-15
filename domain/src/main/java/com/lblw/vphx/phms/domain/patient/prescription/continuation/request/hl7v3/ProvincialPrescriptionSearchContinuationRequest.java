package com.lblw.vphx.phms.domain.patient.prescription.continuation.request.hl7v3;

import com.lblw.vphx.phms.domain.common.request.hl7v3.continuation.ProvincialSearchContinuationRequest;
import com.lblw.vphx.phms.domain.patient.prescription.continuation.request.ProvincialPrescriptionSearchContinuationCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** Request specialisation for Provincial Prescription Search Continuation */
@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionSearchContinuationRequest
    extends ProvincialSearchContinuationRequest<ProvincialPrescriptionSearchContinuationCriteria> {}
