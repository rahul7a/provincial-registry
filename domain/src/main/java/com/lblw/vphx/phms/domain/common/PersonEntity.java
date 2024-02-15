package com.lblw.vphx.phms.domain.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PersonEntity {
  private String firstName;
  private String lastName;
  private Gender gender;
  private String birthTime;
}
