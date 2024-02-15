package com.lblw.vphx.phms.domain.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Status type for a Operation Outcom {@link OperationOutcome}
 *
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764530754071336&cot=14">Operation
 *     Status</a>
 */
public enum Status {
  ACCEPT,
  ACCEPT_WARNING,
  REJECT,
  DROPPED;

  // TODO: To be updated/removed with LTPHVP-25888
  @JsonCreator
  public static Status fromValue(String value) {
    try {
      return Status.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
