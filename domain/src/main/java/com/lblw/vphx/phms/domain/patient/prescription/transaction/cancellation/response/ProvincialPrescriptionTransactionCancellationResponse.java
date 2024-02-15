package com.lblw.vphx.phms.domain.patient.prescription.transaction.cancellation.response;

import com.lblw.vphx.phms.domain.common.DomainResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764538585075777&cot=14">Provincial
 *     Prescription Transaction Cancellation Response</a>
 */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionCancellationResponse
    extends DomainResponse<ProvincialPrescriptionTransactionCancellationResult> {
  /** TODO: Add Model fields when available */
}
