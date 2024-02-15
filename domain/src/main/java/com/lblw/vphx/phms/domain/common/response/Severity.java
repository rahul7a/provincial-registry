package com.lblw.vphx.phms.domain.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Severity type for issue request {@link Issue} */
public enum Severity {
  HIGH,
  MEDIUM,
  LOW;

  // TODO: To be updated/removed with LTPHVP-25888
  @JsonCreator
  public static Severity fromValue(String value) {
    try {
      return Severity.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
