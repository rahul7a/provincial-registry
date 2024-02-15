package com.lblw.vphx.phms.domain.medication;

import com.lblw.vphx.phms.domain.common.ProvincialMedication;
import com.lblw.vphx.phms.domain.common.Quantity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
public class ProvincialTransactionInstruction {
  @Schema(description = "Total number of allowed fills: refills + 1 (first fill).", nullable = true)
  private Integer numberOfFills;

  @Schema(description = "Standard fill quantity delivered to the patient.", nullable = true)
  private Quantity fillQuantity;

  @Schema(description = "First fill quantity delivered to the patient.", nullable = true)
  private Quantity firstFillQuantity;

  @Schema(
      description = "Expiry date of the prescription. Does not prevent dispensing after this date.",
      nullable = true)
  private LocalDate firstFillExpiryDate;

  @Schema(
      description =
          "Duration by when all the medication of the first fill is expected to be consumed.",
      nullable = true)
  private Quantity firstFillExpectedUseDuration;

  @Schema(
      description =
          "The minimum duration between consequent dispenses. Only applicable for drug prescription type.",
      nullable = true)
  private Quantity dispenseInterval;

  @Schema(
      description =
          "Identifies the specific medication concentration of this transaction instruction.",
      nullable = true)
  private ProvincialMedication provincialMedication;

  @Schema(
      description =
          "The total quantity of prescribed medication for the instruction's concentration.",
      nullable = true)
  private Quantity totalPrescribedQuantity;
}
