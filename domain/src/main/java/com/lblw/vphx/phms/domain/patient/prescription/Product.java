package com.lblw.vphx.phms.domain.patient.prescription;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Product {
  /** Name of the medicine or compound that was dispensed. */
  private String tradeName;

  /** The type of treatment for this prescription (eg. acute, chronic, if needed, etc.). */
  private String category;
}
