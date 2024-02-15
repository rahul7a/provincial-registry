package com.lblw.vphx.phms.domain.common;

import com.lblw.vphx.phms.domain.coding.LanguageCode;
import java.util.Map;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class SigDescription {
  Map<LanguageCode, String> code;
  Map<LanguageCode, String> name;
}
