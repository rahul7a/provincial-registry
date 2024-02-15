package com.lblw.vphx.phms.domain.patient.prescription.creation.request;

import com.lblw.vphx.phms.domain.common.DomainRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764523449487323&cot=14">Provincial
 *     Prescription Creation Request</a>
 */
@Data
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
// TODO: @NoArgsConstructor Required for serialisation; to be removed after
// dis-service-processor-merger
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialPrescriptionCreationRequest extends DomainRequest {
  private String prescriptionSystemId;
  private String prescriptionTransactionSystemId;
  private String prescriptionDataVersion;

  // TODO: To be removed; Required for enabling mocks
  private String mockPrescriptionPrescriberSystemId;
  private String mockPrescriptionPatientSystemId;
  private String mockPrescriptionTransactionMedicationSystemId;
  private String mockPrescriptionTransactionPharmacySystemId;
}
