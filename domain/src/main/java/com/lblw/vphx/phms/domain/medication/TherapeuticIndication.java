package com.lblw.vphx.phms.domain.medication;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class TherapeuticIndication {
  private String systemIdentifier;
  private String cdcoId;
  private String source;
  private String therapeuticObjective;
}
