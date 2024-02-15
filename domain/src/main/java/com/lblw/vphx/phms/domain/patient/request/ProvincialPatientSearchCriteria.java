package com.lblw.vphx.phms.domain.patient.request;

import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProvincialPatientSearchCriteria extends ProvincialRequest {
  @Schema(description = "Provincial health number", required = true)
  private String provincialHealthNumber;

  @Schema(description = "Provincial patient id")
  private String patientIdentifier;

  @Schema(description = "Patient's first name", required = true)
  private String firstName;

  @Schema(description = "Patient's last name", required = true)
  private String lastName;

  @Schema(description = "Date of birth")
  private LocalDate dateOfBirth;

  @Schema(description = "Patient gender")
  private Gender gender;

  @Schema(description = "Address")
  private Address address;

  @Schema(description = "Mother's first name")
  private String motherFirstName;

  @Schema(description = "Mother's last name")
  private String motherLastName;

  @Schema(description = "Father's first name")
  private String fatherFirstName;

  @Schema(description = "Father's last name")
  private String fatherLastName;
}
