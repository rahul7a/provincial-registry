package com.lblw.vphx.phms.domain.patient.prescription.creation.post.request;

import com.lblw.vphx.phms.domain.common.ProvincialRequestData;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NonPrescribedPrescriptionCreationRequestData
    extends ProvincialRequestData<NonPrescribedPrescriptionCreationRequest> {
  PrescriptionTransaction prescriptionTransaction;
}
