package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.Coding;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class User {
  private String systemId;
  private String idpUserId;
  private String firstName;
  private String lastName;
  private String permitNumber;
  private String provincialId;
  private Coding providerRole;
}
