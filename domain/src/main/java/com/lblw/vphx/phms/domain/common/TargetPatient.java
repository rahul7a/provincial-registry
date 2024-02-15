package com.lblw.vphx.phms.domain.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class TargetPatient {
  String provincialPatientIdentifier;
  String provincialPatientIdentifierSystem;
  String firstName;
  String lastName;
}
