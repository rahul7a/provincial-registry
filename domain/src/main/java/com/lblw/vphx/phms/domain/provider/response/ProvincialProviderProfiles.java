package com.lblw.vphx.phms.domain.provider.response;

import com.lblw.vphx.phms.domain.common.ProvincialProviderProfile;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ProvincialProviderProfiles extends ProvincialResponse {
  private List<ProvincialProviderProfile> provincialProviderProfiles;
}
