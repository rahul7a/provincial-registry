package com.lblw.vphx.phms.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Telecom {
  @Schema(description = "Telephone number", example = "416-556-9502")
  private String number;
}
