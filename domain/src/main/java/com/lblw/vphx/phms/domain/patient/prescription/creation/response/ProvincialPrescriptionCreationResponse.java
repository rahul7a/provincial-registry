package com.lblw.vphx.phms.domain.patient.prescription.creation.response;

import com.lblw.vphx.phms.domain.common.DomainResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764531812135397&cot=14">Provincial
 *     Prescription Creation Response</a>
 */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionCreationResponse
    extends DomainResponse<ProvincialPrescriptionCreationResult> {}
