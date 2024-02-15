package com.lblw.vphx.phms.domain.patient.prescription;

import com.lblw.vphx.phms.domain.coding.LocalizedText;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class PrescriptionNote {
  @Schema(
      description =
          "Date of the clinical act that caused the DIS to automatically add the note.\n"
              + "In the case of a medication prescribed in the context of activities reserved for pharmacists, this will be the prescription date.")
  private LocalDate addedDate;

  @Schema(
      description =
          "The provider responsible for the clinical act that resulted in the automatic generation of the note by the DIS.\n"
              + "In the case of a medication prescribed in the context of the activities reserved to the pharmacists, it will contains the information of the prescriber.",
      example = "PAUL DUPOND (Médecin - 21501)")
  private LocalizedText[] author;

  @Schema(
      description =
          "Text of the prescription note. \n"
              + "The note will be using the language of the transaction.",
      example = "Médicament prescrit dans le cadre d'un protocole de recherche.",
      maxLength = 2000)
  private String text;
}
