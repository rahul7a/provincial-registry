package com.lblw.vphx.phms.domain.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764535315857186&cot=14">UOM</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UOM {
  private String code;
  private String nameEnglish;
  private String nameFrench;
}
