package com.lblw.vphx.phms.domain.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Priority type for issue request {@link Issue} */
public enum Priority {
  ERROR,
  WARNING,
  INFORMATION;

  // TODO: To be updated/removed with LTPHVP-25888
  @JsonCreator
  public static Priority fromValue(String value) {
    try {
      return Priority.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
