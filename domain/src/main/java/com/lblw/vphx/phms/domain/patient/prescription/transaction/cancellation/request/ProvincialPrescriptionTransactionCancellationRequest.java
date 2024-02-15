package com.lblw.vphx.phms.domain.patient.prescription.transaction.cancellation.request;

import com.lblw.vphx.phms.domain.common.DomainRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764538584908090&cot=14">Provincial
 *     Prescription Transaction Cancellation Request</a>
 */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionTransactionCancellationRequest extends DomainRequest {
  private String prescriptionSystemId;
  private String prescriptionTransactionSystemId;
}
