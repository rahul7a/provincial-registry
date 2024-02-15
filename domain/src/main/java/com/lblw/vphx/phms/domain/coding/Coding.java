package com.lblw.vphx.phms.domain.coding;

import com.lblw.vphx.phms.domain.common.Province;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Coding {
  @Schema(description = "Issuing province", example = "QC")
  @Indexed
  private Province province;

  @Schema(description = "The target system applicable to coding", example = "SYSTEM/ALL")
  @Indexed
  private String system;

  @Schema(description = "The system code for which this coding is applied", example = "2060000")
  @Indexed
  private String code;

  @Schema(
      description =
          "Any associated OID identifier for the coding. Only applicable for provincial codes")
  @EqualsAndHashCode.Exclude
  private String codingIdentifier;

  @Schema(description = "A description of the code in localized text in both english and french")
  @EqualsAndHashCode.Exclude
  private LocalizedText[] display;
}
