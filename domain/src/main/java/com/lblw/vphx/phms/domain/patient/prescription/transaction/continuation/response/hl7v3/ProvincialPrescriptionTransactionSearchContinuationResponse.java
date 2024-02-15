package com.lblw.vphx.phms.domain.patient.prescription.transaction.continuation.response.hl7v3;

import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.patient.prescription.transaction.continuation.response.ProvincialPrescriptionTransactionContinuationResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionSearchContinuationResponse
    extends Response<
        ProvincialPrescriptionTransactionSearchContinuationResponseControlAct,
        ProvincialPrescriptionTransactionContinuationResponse> {}
