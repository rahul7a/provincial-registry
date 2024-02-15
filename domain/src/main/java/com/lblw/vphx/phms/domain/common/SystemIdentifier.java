package com.lblw.vphx.phms.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task for removal: https://jira.lblw.cloud/browse/LTPHVPHXQC-926
 *
 * @deprecated this value object is to be replaced with provincialId(s) (province as assigner) or
 *     simply id(s) (system as assigner)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Deprecated(forRemoval = true)
public class SystemIdentifier {

  private IDENTIFIER_TYPE type;

  private String value;

  /* Province, example - Qc */
  private String assigner;

  private String system;

  public enum IDENTIFIER_TYPE {
    HEALTH_NUMBER,
    LOCATION,
    LOCATION_PERMIT_NUMBER,
    LOCATION_PHARMACY_BILLING_NUMBER,
    LOCATION_PUBLIC_HEALTHCARE_PERMIT_NUMBER,
    PARENT_LOCATION,
    PATIENT,
    PRESCRIPTION,
    PRESCRIPTION_TRANSACTION,
    PROVIDER,
    PROVIDER_BILLING_NUMBER,
    PROVIDER_LICENSE_NUMBER,
    PROVIDER_SIN_NUMBER,
    PROVIDER_ROLE_CODE,
    REFERENCE_PROTOCOL,
    QUERY_IDENTIFIER
  }
}
