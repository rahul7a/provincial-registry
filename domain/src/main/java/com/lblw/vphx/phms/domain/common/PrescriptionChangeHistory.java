package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.LocalizedText;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
@ToString
public class PrescriptionChangeHistory {
  @Schema(
      description =
          "Provider or the Application responsible for the change on the prescription in localized text.",
      example = "PAUL DUPOND (Médecin - 21501)")
  private LocalizedText[] author;
}
