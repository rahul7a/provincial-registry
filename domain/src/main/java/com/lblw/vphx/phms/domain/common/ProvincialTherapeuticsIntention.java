package com.lblw.vphx.phms.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
@ToString
public class ProvincialTherapeuticsIntention {
  @Schema(
      description =
          "Represents the order priority of the therapeutic intentions. Multiple intentions can have the same priority. \"1\" being the highest priority.")
  String priorityNumber;
}
