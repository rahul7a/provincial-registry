package com.lblw.vphx.phms.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderConstants {
  public static final String X_PROVINCE_CODE_HEADER = "x-province-code";
  public static final String X_REQUEST_ID_HEADER = "x-request-id";
  public static final String X_RESPONSE_ID_HEADER = "x-response-id";
  public static final String X_REQUEST_ID_REQUIRED = "x-request-id is required";
  public static final String LOG_REQUEST_ID = "%s request-id: %s";
  public static final String MOCK_ENABLED_HEADER = "mock-enabled";
}
