package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.Coding;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
@ToString
public class ProvincialPrescriptionStopProblem {
  @Schema(description = "Indicates the type of problem/issue", nullable = true)
  Coding issueCode;
}
