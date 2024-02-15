package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.Coding;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Quantity {
  /**
   * @deprecated This field is no longer acceptable.
   *     <p>Use {@link Quantity#unitCode} instead.
   */
  @Deprecated String unit;

  @Schema(description = "Unit code for the quantity")
  Coding unitCode;

  @Schema(description = "Value of the quantity")
  Double value;
}
