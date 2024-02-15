package com.lblw.vphx.phms.domain.prescription;

import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.common.SigDescription;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class PrescriptionSig {
  private LanguageCode language;
  private boolean defaultSigFlag;
  private List<String> code;
  private List<SigDescription> descriptions;
  private String descriptionEn;
  private String descriptionFr;
}
