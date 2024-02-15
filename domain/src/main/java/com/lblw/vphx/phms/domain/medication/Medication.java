package com.lblw.vphx.phms.domain.medication;

import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Medication {
  private MedicationType type;
  private String din;
  private String tradeName;
  private String category;
  private List<MedicationIngredient>
      ingredients; // TODO: remove this ingredients and use the ingredients from Compound
  private String systemIdentifier;
  private String source;
  private String legalStatus;
  private String routeOfAdministration;
  private String form;
  private List<TherapeuticIndication> therapeuticIndication;
  private String upc;
  private String tradeNameEnglish;
  private String tradeNameFrench;
  private CompoundType compoundType;
  private PackSize packSize;
  private UOM uom;
  private String ingredientId;
  private String exceptionCode;
}
