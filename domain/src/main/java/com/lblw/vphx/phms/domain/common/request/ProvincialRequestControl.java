package com.lblw.vphx.phms.domain.common.request;

import com.lblw.vphx.phms.domain.common.Language;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.User;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class ProvincialRequestControl {
  private String requestId;
  private Province province;
  private User user;
  private Pharmacy pharmacy;
  private Language language;
}
