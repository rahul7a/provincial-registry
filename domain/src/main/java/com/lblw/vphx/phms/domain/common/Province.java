package com.lblw.vphx.phms.domain.common;

import lombok.Getter;

/** Standard province code used in all sub-domains */
@Getter
public enum Province {
  ALL("ALL", "ALL"),
  AB("Alberta", "Alberta"),
  BC("British Columbia", "Colombie-Britanique"),
  MB("Manitoba", "Manitoba"),
  NB("New Brunswick", "Nouveau-Brunswick"),
  NL("Newfoundland And Labrador", "Terre-Neuve-et-Labrador"),
  NS("Nova Scotia", "Nouvelle-Écosse"),
  NT("North West Territories", "Territoires du Nord-Ouest"),
  NU("Nunavut", "Nunavut"),
  ON("Ontario", "Ontario"),
  PEI("Prince Edward Island", "Île-du-Prince-Édouard"),
  QC("Quebec", "Québec"),
  SK("Saskatchewan", "Saskatchewan"),
  YT("Yukon", "Yukon");

  private final String en;
  private final String fr;

  /**
   * Creates Enum with given province name in French and English text
   *
   * @param en name of province in English
   * @param fr name of province in French
   */
  Province(String en, String fr) {
    this.en = en;
    this.fr = fr;
  }

  /**
   * @return all supported provinces TODO: Make it a List.of(Province.QC)
   */
  public static Province[] getSupportedProvince() {
    return new Province[] {QC};
  }
}
