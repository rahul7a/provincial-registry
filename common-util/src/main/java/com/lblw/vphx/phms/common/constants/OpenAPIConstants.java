package com.lblw.vphx.phms.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/***
 * Class for maintaining dis-service related constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenAPIConstants {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class HTTPConstants {
    // API response descriptions
    public static final String OK = "OK";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";
    public static final String BAD_REQUEST = "Bad Request";
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class StatusCode {
    public static final String OK = "200";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String BAD_REQUEST = "400";
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Tags {
    public static final String PRESCRIPTION_TAGS = "Prescription APIs";
    public static final String PRESCRIPTION_TRANSACTION_TAGS = "Prescription Transaction APIs";
    public static final String PROVINCIAL_CLIENT_REGISTRY_TRANSACTIONS_TAGS = "Provincial Client Registry Transactions";
  }
}
