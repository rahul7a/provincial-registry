package com.lblw.vphx.phms.domain.common;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class LocalizedValue {
  /** ISO format */
  private String language;

  private String value;
}
