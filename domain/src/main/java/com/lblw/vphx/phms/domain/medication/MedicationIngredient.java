package com.lblw.vphx.phms.domain.medication;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicationIngredient {
  private Medication medication;
  private Double quantity;
}
