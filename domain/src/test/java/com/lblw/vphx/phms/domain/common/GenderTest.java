package com.lblw.vphx.phms.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class GenderTest {
  @Test
  void toGender() {
    assertThat(Gender.toGender(1)).isEqualTo(Gender.M);
    assertThat(Gender.toGender(2)).isEqualTo(Gender.F);
    assertNull(Gender.toGender(3));
  }
}
