package com.lblw.vphx.phms.domain.patient.prescription.creation.request;

import com.lblw.vphx.phms.domain.common.ProvincialRequestData;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764523449551305&cot=14">Provincial
 *     Prescription Creation Request Data</a>
 */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionCreationRequestData
    extends ProvincialRequestData<ProvincialPrescriptionCreationRequest> {
  PrescriptionTransaction prescriptionTransaction;
}
