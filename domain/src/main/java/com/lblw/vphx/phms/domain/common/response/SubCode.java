package com.lblw.vphx.phms.domain.common.response;

/** Sub-code for an issue request {@link Issue} */
public enum SubCode {
    SYSTEM_TIMEOUT,
    SYSTEM_AUTHORIZATION,
    SYSTEM_OUTAGE,
    SYSTEM_MISSING_CODE,
    BUSINESS_DATA_REQUIRED_FIELD_MISSING,
    BUSINESS_DATA_INVALID_DATA,
    CONSENT_MISSING,
    CONSENT_EXPIRED,
    INFO_ALL_DONE
}
