package com.lblw.vphx.phms.domain.patient.prescription.creation.post.response;

import com.lblw.vphx.phms.domain.common.DomainResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NonPrescribedPrescriptionCreationResponse
    extends DomainResponse<NonPrescribedPrescriptionCreationResult> {}
