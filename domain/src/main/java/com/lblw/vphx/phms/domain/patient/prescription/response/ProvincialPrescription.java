package com.lblw.vphx.phms.domain.patient.prescription.response;

import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.*;
import com.lblw.vphx.phms.domain.location.ProvincialLocation;
import com.lblw.vphx.phms.domain.patient.prescription.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class ProvincialPrescription {
  private String provincialId;
  private PrescriptionType prescriptionType;
  private PrescriptionStatus prescriptionStatus;
  private LocalDate writtenDate;
  private LocalDate expiryDate;
  private Boolean clinicalNotesIndicatorFlag;
  private Boolean issuesIndicatorFlag;
  private ProvincialProviderProfile prescriber;
  private Product product;
  private String sig;

  @Schema(description = "The location where the prescription was written")
  private ProvincialLocation provincialLocation;

  private PrescriptionDispensableIndicator dispenseIndicator;

  @Schema(
      description =
          "Represents the total quantity of prescribed medication.\n"
              + "In the case of multiple dispense instructions, represents the first total quantity per instruction's concentration.\n"
              + "Only applicable for drug prescription type.",
      nullable = true)
  private Quantity totalAuthorizedQuantity;

  /**
   * @deprecated (this will be removed and replaced by patientCareComposition)
   */
  @Deprecated(forRemoval = true)
  private String careComposition;

  @Schema(
      description =
          "Episodes of care where the medication was dispensed.\n"
              + "Includes context of care of the prescription in which the medication was or will be dispensed and Type of location where care was provided.\n"
              + "Applies only to medications delivered in hospitals",
      nullable = true,
      example = "Ambulatory/Oncology")
  private LocalizedText[] patientCareComposition;

  private ProvincialPrescriptionTransaction mostRecentTransaction;
  private List<ReferenceProtocol> referenceProtocols;

  @Schema(
      description =
          "Medication or compound that was handed to the patient. Prescribed in the case of PrescriptionType.DRUG.\n"
              + "Populates on details query unavailable on search query",
      nullable = true)
  private ProvincialMedication provincialMedication;

  @Schema(
      description =
          "Problems associated with occurrences of the change history where the prescription is being stopped.",
      maxLength = 25)
  private List<ProvincialPrescriptionStopProblem> stopProblems;

  @Schema(
      description =
          "The changes history of this prescription including the events impacting its transactions.\n"
              + "A warning will be returned if the prescription has more than 20 changes.\n"
              + "Sorted in ascending order.",
      maxLength = 20)
  private List<PrescriptionChangeHistory> changeHistory;

  @Schema(
      description =
          "The pharmacy location that is assigned to the prescription. Does not prevent another pharmacy to dispense this prescription. Only applicable for drug prescription type.")
  private ProvincialLocation assignedPharmacy;

  @Schema(
      description = "Notes related to this prescription. Sorted in ascending order of addedDate.",
      maxLength = 99)
  private List<PrescriptionNote> notes;

  @Schema(
      description =
          "Estimated number of remaining dispenses on the prescription. Only applicable for drug prescription type.",
      nullable = true)
  private Integer remainingRefills;

  @Schema(
      description =
          "Estimated total quantity that can still be dispensed on this prescription. Only applicable for drug prescription type.",
      nullable = true)
  private Quantity remainingRefillsTotalQuantity;

  @Schema(
      description =
          "Total number of dispenses made against this prescription. Only applicable for drug prescription type.",
      nullable = true)
  private Integer processedRefills;

  @Schema(
      description =
          "Total quantity of medication dispensed in the previous processed dispenses. Only applicable for drug prescription type.",
      nullable = true)
  private Quantity processedRefillsTotalQuantity;

  @Schema(
      description =
          "Fill refusals registered against the prescription. Sorted in ascending order of refusalDate. Only applicable for drug prescription type.",
      maxLength = 10)
  private List<PrescriptionFillRefusal> fillRefusals;
}
