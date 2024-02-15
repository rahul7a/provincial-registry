package com.lblw.vphx.phms.common.internal.vocab.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Constants class for maintaining vocab service / reference data service related constants. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VocabConstants {

  public static final String SYSTEM = "SYSTEM";
  public static final String CODE_UNKNOWN = "UNK";
  public static final String CODE_UNKNOWN_ENG = "Unknown";
  public static final String CODE_UNKNOWN_FRE = "Inconnu";
  public static final String CODE_NULL_FLAVOUR = "NI";
  public static final String CODE_NOT_AVALIABLE = "NA";
  public static final String UNKNOWN_CODE_ENG_PREFIX = "Unknown Code: ";
  public static final String UNKNOWN_CODE_FRE_PREFIX = "Code Inconnu: ";
  public static final String OUT_OF_PROVINCE_LOCATION_ENG = "Out of %s Location";
  public static final String OUT_OF_PROVINCE_LOCATION_FRE = "Lieu hors %s";
  public static final String UNRESOLVED_LOCATION_ENG = "Unresolved Location";
  public static final String UNRESOLVED_LOCATION_FRE = "Lieu non resolu";

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Params {
    public static final String PROVINCE_QUERY_PARAM = "province";
    public static final String CERX_CODE_QUERY_PARAM = "cerx-code";
    public static final String HW_CODE_QUERY_PARAM = "hw-code";
  }
}
