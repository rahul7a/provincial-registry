package com.lblw.vphx.phms.domain.patient.prescription.transaction.creation.response;

import com.lblw.vphx.phms.domain.common.DomainResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** TODO: Add Miro Ref when available */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionCreationResponse
    extends DomainResponse<ProvincialPrescriptionTransactionCreationResult> {}
