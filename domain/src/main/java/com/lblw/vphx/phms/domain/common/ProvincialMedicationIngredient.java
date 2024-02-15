package com.lblw.vphx.phms.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProvincialMedicationIngredient {
  private ProvincialMedication medication;
  private Quantity quantity;
}
