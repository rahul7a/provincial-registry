package com.lblw.vphx.phms.domain.patient;

import com.lblw.vphx.phms.domain.common.Gender;
import java.time.LocalDate;
import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764526945276599&cot=14">Patient</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Patient {
  private String systemId;
  private String firstName;
  private String lastName;
  private LocalDate dateOfBirth;
  private Gender gender;
  private PatientType type;

  // TODO: Rename to provincialId
  private String provincialIdentifier;


}
