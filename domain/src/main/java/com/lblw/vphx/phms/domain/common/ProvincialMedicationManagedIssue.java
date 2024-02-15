package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.Coding;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
@ToString
public class ProvincialMedicationManagedIssue {
  private Coding code;
  private Coding priorityCode;
  private Coding severityCode;
}
