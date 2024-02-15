package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.LanguageCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class helps to find the Language information
 *
 * @see <a
 *     href="https://miro.com/app/board/o9J_l8yNn14=/?moveToWidget=3458764527727759726&cot=14">Language</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Language {
  private LanguageCode code;
}
