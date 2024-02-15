package com.lblw.vphx.phms.domain.coding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class LocalizedText {
  @Schema(description = "Language", example = "ENG")
  private LanguageCode language;

  @Schema(
      description =
          "A description of the code in localized text defined by associated 'language' field",
      example = "DENTIST")
  private String text;
}
