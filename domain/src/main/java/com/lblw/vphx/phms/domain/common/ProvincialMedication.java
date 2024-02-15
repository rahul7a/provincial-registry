package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.medication.ProvincialMedicationInstructions;
import com.lblw.vphx.phms.domain.medication.ProvincialTransactionInstruction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764536740211059&cot=14">Provincial
 *     Medication</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
@ToString
public class ProvincialMedication {
  private String din;

  @Schema(
      description =
          "Can contain name of the medication.\n"
              + "Can be DIN (DIN, pseudo-DIN, DIN-HM, NPN), name of the compound or local name if it doesn't have a DIN yet.\n"
              + "If describing a compound ingredient medication, will have DIN or pseudo-DIN.",
      nullable = true)
  private String tradeName;

  @Schema(
      description =
          "Form code of the medication. Mandatory when DIN is not present - as in compound or local name.",
      nullable = true)
  private String form;

  private Double packSize;

  @Schema(
      description =
          "Non-standard instructions as provided by the pharmacy for all medications, understandable by a user. Includes administration route, administration period and maximum doses.")
  private String sig;

  @Schema(
      description = "Ingredients of 'compound' medication. Not applicable for 'drug' medication.",
      nullable = true,
      minLength = 0,
      maxLength = 50)
  private List<ProvincialMedicationIngredient> ingredients;

  @Schema(
      description =
          "Administration instructions for the medication. Multiple occurrences occur only in the case of packages (containing multiple medications) sorted by their receiving order.\n"
              + "Only applicable for medication described on prescription and transaction (dispense or post) levels.",
      nullable = true,
      minLength = 1,
      maxLength = 10)
  private List<ProvincialMedicationInstructions> instructions;

  @Schema(
      description =
          "Drug dispensing instructions sorted in order of reception. Multiple occurrences can be present when the medication must be dispensed with multiple concentrations.\n"
              + "Only applicable for medication described on prescription (drug) level.",
      nullable = true,
      minLength = 1,
      maxLength = 5)
  private List<ProvincialTransactionInstruction> dispensingInstructions;

  @Schema(
      description =
          "Represents the therapeutics intentions; the reason why the medication was prescribed. Sorted by priority\n"
              + "Only applicable for medication described on prescription (drug) level.",
      maxLength = 5)
  private List<ProvincialTherapeuticsIntention> therapeuticsIntentions;
}
