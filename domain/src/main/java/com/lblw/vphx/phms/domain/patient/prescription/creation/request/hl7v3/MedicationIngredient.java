package com.lblw.vphx.phms.domain.patient.prescription.creation.request.hl7v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicationIngredient {
  private MedicationType medicationType;
  private String medicationDrugCode;
  private String medicationDrugTradeName;
}
