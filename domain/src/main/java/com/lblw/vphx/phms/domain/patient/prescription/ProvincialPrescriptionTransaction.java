package com.lblw.vphx.phms.domain.patient.prescription;

import com.lblw.vphx.phms.domain.common.Pharmacy;
import com.lblw.vphx.phms.domain.common.ProvincialMedication;
import com.lblw.vphx.phms.domain.common.ProvincialMedicationManagedIssue;
import com.lblw.vphx.phms.domain.common.Quantity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProvincialPrescriptionTransaction {
  private String provincialId;
  private Product product;
  private String sig;
  private Pharmacy pharmacy;
  private LocalDate serviceDate;
  private Quantity dispensedQuantity;
  private Quantity supplyDays;
  private ProvincialPrescriptionTransactionStatus status;
  private ProvincialMedication provincialMedication;
  private List<ProvincialMedicationManagedIssue> issues;

  @Schema(
      description =
          "Dispense validity period during which dispensing is allowed on the prescription")
  private String dispensingAllowedPeriod;
}
