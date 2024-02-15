package com.lblw.vphx.phms.common.constants;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/***
 * Common constants class for maintaining dis-service related constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class APIConstants {

  // API endpoints
  public static final String PRESCRIPTION_SEARCH_URI = "/provincial-prescriptions";
  public static final String PRESCRIPTION_SEARCH_CONTINUATION_URI =
      "/provincial-prescriptions/continuation";
  public static final String PRESCRIPTION_TRANSACTION_SEARCH_URI =
      "/provincial-prescriptions/transactions";
  public static final String PRESCRIPTION_TRANSACTION_SEARCH_CONTINUATION_URI =
      "/provincial-prescriptions/transactions/continuation";
  public static final String PRESCRIPTION_DETAILS_URI = "/provincial-prescriptions/details";
  public static final String PRESCRIPTION_CREATION_URI = "/provincial-prescriptions/create";
  public static final String PRESCRIPTION_TRANSACTION_CREATION_URI =
      "/provincial-prescriptions/transactions/create";
  public static final String PRESCRIPTION_CANCELLATION_URI = "/provincial-prescriptions/cancel";
  public static final String PRESCRIPTION_DISCONTINUE_URI = "/provincial-prescriptions/discontinue";

  public static final String PRESCRIPTION_TRANSACTION_CANCELLATION_URI =
      "/provincial-prescriptions/transactions/cancel";
  public static final String PROVIDER_SEARCH_URI = "/provincial-registry/provider/search";
  public static final String PATIENT_SEARCH_URI = "/provincial-registry/patient/search";
  public static final String PATIENT_CONSENT_URI = "/provincial-registry/patient/consent";
  public static final String LOCATION_DETAILS_URI = "/provincial-registry/location-details";
  // TODO: rename location to locations
  public static final String LOCATION_SEARCH_URI = "/provincial-registry/location";
  public static final String NON_PRESCRIBED_PRESCRIPTION_URI =
      "/provincial-prescriptions/non-prescribed-prescription/create";
  public static final List<String> DIS_ENDPOINTS =
      List.of(
          PRESCRIPTION_CREATION_URI,
          PRESCRIPTION_TRANSACTION_CREATION_URI,
          PRESCRIPTION_SEARCH_URI,
          PRESCRIPTION_SEARCH_CONTINUATION_URI,
          PRESCRIPTION_TRANSACTION_SEARCH_CONTINUATION_URI,
          PRESCRIPTION_TRANSACTION_SEARCH_URI,
          PRESCRIPTION_DETAILS_URI,
          PRESCRIPTION_CANCELLATION_URI,
          PRESCRIPTION_DISCONTINUE_URI,
          PRESCRIPTION_TRANSACTION_CANCELLATION_URI,
          NON_PRESCRIBED_PRESCRIPTION_URI);
  public static final List<String> DSQ_ENDPOINTS =
      List.of(
          PROVIDER_SEARCH_URI,
          PATIENT_SEARCH_URI,
          PATIENT_CONSENT_URI,
          LOCATION_DETAILS_URI,
          LOCATION_SEARCH_URI);
}
