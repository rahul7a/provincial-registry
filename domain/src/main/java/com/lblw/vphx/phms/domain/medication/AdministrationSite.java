package com.lblw.vphx.phms.domain.medication;

import lombok.*;

/**
 * @see <a href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764538202381609&cot=14">
 *     AdministrationSite</a>
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@ToString
public class AdministrationSite {
  private String code;
}
