package com.lblw.vphx.phms.domain.patient.prescription;

import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.location.LocationAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
@ToString
public class PrescriptionFillRefusal {
  @Schema(description = "Date when the pharmacist refused to fill the prescription.")
  private LocalDate refusalDate;

  @Schema(
      description = "The provider who refused to fill the prescription.",
      example = "Karim Jotun (Pharmacist - 097310)")
  private LocalizedText[] author;

  @Schema(
      description = "The location where the refusal to fill happened.",
      example = "Pharmacie Marc Potvin (Type: Pharmacy Tel: 5143334444)")
  private LocalizedText[] location;

  @Schema(description = "The location address where the refusal to fill happened.", nullable = true)
  private LocationAddress locationAddress;

  @Schema(description = "The reason(s) for the refusal to fill.", minLength = 1, maxLength = 50)
  private List<Coding> reasonCodes;
}
