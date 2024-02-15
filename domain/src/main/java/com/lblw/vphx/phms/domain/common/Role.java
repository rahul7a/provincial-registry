package com.lblw.vphx.phms.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class Role {
  private String code;
  private String name;
  private SystemIdentifier identifier;
}
