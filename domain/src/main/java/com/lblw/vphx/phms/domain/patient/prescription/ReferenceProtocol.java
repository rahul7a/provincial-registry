package com.lblw.vphx.phms.domain.patient.prescription;

import lombok.*;

/**
 * Can represent one of:
 *
 * <pre>
 * 2.16.124.10.101.1.60.7.501: Indicator of a prescription prescribed in the context of a research protocol and the protocol code,
 * 2.16.124.10.101.1.60.7.600: Indicator for prescription in a special access program,
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ReferenceProtocol {
  private String identifier;

  private String code;
}
