package com.lblw.vphx.phms.domain.medication;

import lombok.*;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764535315857186&cot=14">PackSize</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class PackSize {
  private UOM uom;
  private Double value;
}
