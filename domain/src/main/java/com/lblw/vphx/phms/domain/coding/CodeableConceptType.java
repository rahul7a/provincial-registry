package com.lblw.vphx.phms.domain.coding;

/**
 * Codeable Concept Type Enum
 *
 * <p>rdsVocabKey: corresponds to reference data service vocabulary name
 */
public enum CodeableConceptType {
  LOCATION("LocationType"),
  PROVIDER_SPECIALITY("ProviderSpecialty"),
  PROVIDER_ROLE("ProviderRoleType"),
  PROVIDER_ROLE_OID("ProviderRoleTypeOID"),
  TREATMENT_TYPE("TreatmentType"),
  ROUTE_OF_ADMINISTRATION("RouteOfAdmin"),
  COMPOUND_FORM("CompoundForm"),
  ADMINISTRATION_SITE("AdministrationSite"),
  MANAGED_ISSUE_CODE("ActDetectedIssueCode"),
  MANAGED_ISSUE_PRIORITY_CODE("IssuePriority"),
  MANAGED_ISSUE_SEVERITY_CODE("IssueSeverity"),
  PACK_SIZE_UOM("PackSizeUoM"),
  DURATION_LENGTH_UNIT("DurationLengthUnit"),
  ADMINISTRABLE_DRUG_FORM("AdministrableDrugForm"),
  DRUG_FORM("DrugForm"),
  ACT_CARE_EVENT_TYPE("ActCareEventType"),
  SERVICE_DELIVERY_LOCATION_ROLE_TYPE("ServiceDeliveryLocationRoleType");

  /** Corresponding rds-key for vocabulary mapping */
  private String rdsVocabKey;

  CodeableConceptType(String rdsVocabKey) {
    this.rdsVocabKey = rdsVocabKey;
  }

  /**
   * @return {@link String} rdsVocabKey
   */
  public String getRdsVocabKey() {
    return rdsVocabKey;
  }
}
