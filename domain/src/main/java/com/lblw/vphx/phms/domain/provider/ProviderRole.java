package com.lblw.vphx.phms.domain.provider;

import com.lblw.vphx.phms.domain.common.Role;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @deprecated (this class will be removed)
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Deprecated(forRemoval = true)
public class ProviderRole extends Role {
  private List<ProviderSpeciality> providerSpecialities;
}
