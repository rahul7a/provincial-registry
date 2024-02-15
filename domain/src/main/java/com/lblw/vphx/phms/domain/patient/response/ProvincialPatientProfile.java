package com.lblw.vphx.phms.domain.patient.response;

import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ProvincialPatientProfile extends ProvincialResponse {
  @Schema(description = "Patient's provincial id", example = "1031201633")
  private SystemIdentifier identifier;

  @Schema(description = "Patient's provincial health number (PHN)", example = "CANM92010128")
  private SystemIdentifier provincialHealthNumber;

  @Schema(description = "Patient's first name", example = "Lorenzo")
  private String firstName;

  @Schema(description = "Patient's last name", example = "Monnet")
  private String lastName;

  @Schema(description = "Patient's date of birth", example = "1992-01-01")
  private LocalDate dateOfBirth;

  @Schema(description = "Patient's gender", example = "F")
  private Gender gender;

  @Schema(description = "Patient's mother's first name", example = "Connie")
  private String motherFirstName;

  @Schema(description = "Patient's mother's last name", example = "Tucker")
  private String motherLastName;

  @Schema(description = "Patient's father's first name", example = "Tim")
  private String fatherFirstName;

  @Schema(description = "Patient's father's last name", example = "Burton")
  private String fatherLastName;

  @Schema(description = "Patient's address")
  private Address address;

  @Schema(description = "Indicates if the patient is deceased", example = "true")
  private Boolean deceasedIndicator;

  @Schema(
      description = "Deceased date, applicable if deceased indicator is 'true'",
      example = "2022-02-12")
  private LocalDate deceasedDate;

  @Schema(description = "Value to match with search result rank")
  private int matchingIndex;
}
