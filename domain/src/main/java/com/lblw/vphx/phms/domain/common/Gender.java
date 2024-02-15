package com.lblw.vphx.phms.domain.common;

import java.util.Arrays;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764526945276599&cot=14">Patient</a>
 */
public enum Gender {
  M(1),
  F(2);

  private final int value;

  Gender(int value) {
    this.value = value;
  }

  /**
   * find Gender {@link Gender} from ordinalValue
   *
   * @param value {@link Integer}
   * @return PatientType
   */
  public static Gender toGender(int value) {
    return Arrays.stream(values()).filter(gender -> gender.value == value).findAny().orElse(null);
  }
}
