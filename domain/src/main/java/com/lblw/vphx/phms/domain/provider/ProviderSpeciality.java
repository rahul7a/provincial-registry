package com.lblw.vphx.phms.domain.provider;

import com.lblw.vphx.phms.domain.common.EffectiveDateRange;
import lombok.*;

/**
 * @deprecated (this class will be removed)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Deprecated(forRemoval = true)
public class ProviderSpeciality {
  private String code;
  private String name;
  private EffectiveDateRange effectiveDateRange;
}
