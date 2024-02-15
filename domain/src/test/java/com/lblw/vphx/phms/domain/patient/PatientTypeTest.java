package com.lblw.vphx.phms.domain.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lblw.vphx.phms.domain.common.Gender;
import org.junit.jupiter.api.Test;

class PatientTypeTest {

  @Test
  void toPatientType() {
    assertThat(PatientType.toPatientType(1)).isEqualTo(PatientType.HUMAN);
    assertThat(PatientType.toPatientType(2)).isEqualTo(PatientType.ANIMAL);
    assertNull(Gender.toGender(3));
  }
}
