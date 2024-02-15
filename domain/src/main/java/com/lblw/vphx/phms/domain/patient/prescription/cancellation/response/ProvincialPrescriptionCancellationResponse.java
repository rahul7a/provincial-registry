package com.lblw.vphx.phms.domain.patient.prescription.cancellation.response;

import com.lblw.vphx.phms.domain.common.DomainResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764538584988754&cot=14">Provincial
 *     Prescription Cancellation Response</a>
 */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionCancellationResponse
    extends DomainResponse<ProvincialPrescriptionCancellationResult> {
}
