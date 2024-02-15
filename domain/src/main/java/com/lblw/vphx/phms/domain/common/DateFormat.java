package com.lblw.vphx.phms.domain.common;

public class DateFormat {

  /** format used in all Phms REST API */
  public static final String SYSTEM_DATE_FORMAT = "yyyy-MM-dd";
  /** format used in all QC hl7 transactions */
  public static final String QC_DATE_FORMAT = "yyyyMMdd";
  /** format used in all QC hl7 transactions */
  public static final String QC_DATE_TIME_FORMAT = "yyyyMMddHHmmss";
  /** format used in datetime with Offset */
  public static final String QC_DATE_TIME_FORMAT_WITH_OFFSET = "yyyyMMddHHmmssZ";
  /** format used in provincial enablement datetime with Offset */
  public static final String PROVINCIAL_ENABLEMENT_DATE_TIME_FORMAT_WITH_OFFSET =
      "yyyyMMddHHmmss.SSSZ";
  public static final String PROVINCIAL_ENABLEMENT_HYPHEN_DELIMITED_DATE_TIME_FORMAT_WITH_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  private DateFormat() {}
}
