package com.lblw.vphx.phms.domain.medication;

import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.ProvincialMedication;
import com.lblw.vphx.phms.domain.common.Quantity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764536740211059&cot=14">ProvincialMedicationInstructions
 *     </a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
public class ProvincialMedicationInstructions {
  @Schema(
      description =
          "Non-standard instructions as provided by the pharmacy for the medication, understandable by a user. Includes administration route, administration period and maximum doses.")
  private String sig;

  private String form;

  /**
   * Start data of the period of time during which the prescription can be dispensed to the patient
   *
   * @deprecated This field is no longer acceptable.
   *     <p>Use {@link ProvincialMedicationInstructions#instructionEffectiveTime} instead.
   */
  @Deprecated private LocalDate instructionStartDate;

  /**
   * End data of the period of time during which the prescription can be dispensed to the patient
   *
   * @deprecated This field is no longer acceptable.
   *     <p>Use {@link ProvincialMedicationInstructions#instructionEffectiveTime} instead.
   */
  @Deprecated private LocalDate instructionEndDate;

  /**
   * Duration of the period of time during which the prescription can be dispensed to the patient
   *
   * @deprecated This field is no longer acceptable.
   *     <p>Use {@link ProvincialMedicationInstructions#instructionEffectiveTime} instead.
   */
  @Deprecated private Double duration;

  private Quantity dailyDosage;
  private Quantity weeklyDosage;

  @Schema(
      description =
          "Maximum medication quantity that can be administered daily and/or weekly in localized text.")
  private LocalizedText[] maxDosages;

  private Coding administrationUnitCode;

  @Schema(description = "Administration route code")
  private Coding routeOfAdministration;

  @Schema(
      description =
          "Human administration sites. 0 to 5 occurrences for prescription and 1 to 50 occurrences for prescription transactions.")
  private List<Coding> administrationSites;

  private ProvincialMedication provincialMedication;

  @Schema(
      description =
          "Indicates the period of time during which the prescription can be dispensed to the patient.")
  private String instructionEffectiveTime;

  @Schema(
      description =
          "The total quantity of prescribed medication for the instruction's concentration.",
      nullable = true)
  private Quantity totalPrescribedQuantity;

  @Schema(
      description = "Conditions related to the administration of the medication",
      nullable = true,
      example = "empty stomach")
  private String supplementalInstruction;
}
