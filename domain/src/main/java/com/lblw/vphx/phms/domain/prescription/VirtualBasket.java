package com.lblw.vphx.phms.domain.prescription;

import java.time.Instant;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class VirtualBasket {
  private Instant promiseTime;
}
