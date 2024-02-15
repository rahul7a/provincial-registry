package com.lblw.vphx.phms.domain.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @see <a href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530754071469&cot=14">
 *     Response Classification</a>
 *     <p>/** Code for an issue request {@link Issue}
 */
public enum ResponseClassification {
  SYSTEM,
  BUSINESS_DATA,
  CONSENT,
  DUR,
  ADJUDICATION,
  INFORMATION,
  HELD,
  TERMINATED;

  // TODO: To be updated/removed with LTPHVP-25888
  @JsonCreator
  public static ResponseClassification fromValue(String value) {
    try {
      return ResponseClassification.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
