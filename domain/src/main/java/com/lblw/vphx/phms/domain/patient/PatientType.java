package com.lblw.vphx.phms.domain.patient;

import java.util.Arrays;
import lombok.Getter;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764526945276599&cot=14">Patient</a>
 */
@Getter
public enum PatientType {
  HUMAN(1),
  ANIMAL(2);

  private final int value;

  PatientType(int value) {
    this.value = value;
  }

  /**
   * find PatientType {@link PatientType} from ordinalValue
   *
   * @param value {@link Integer}
   * @return PatientType
   */
  public static PatientType toPatientType(int value) {
    return Arrays.stream(values())
        .filter(patientType -> patientType.value == value)
        .findAny()
        .orElse(null);
  }
}
