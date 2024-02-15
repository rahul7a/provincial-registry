package com.lblw.vphx.phms.domain.patient.prescription.discontinuation.response;

import com.lblw.vphx.phms.domain.common.DomainResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionDiscontinuationResponse
    extends DomainResponse<ProvincialPrescriptionDiscontinuationResult> {
}
