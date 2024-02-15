package com.lblw.vphx.phms.domain.patient.prescription.transaction.continuation.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionContinuationResponse
    extends ProvincialPrescriptionTransactionContinuationPageList {}
