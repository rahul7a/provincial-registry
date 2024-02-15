package com.lblw.vphx.phms.domain.common.alert;

import com.lblw.vphx.phms.domain.common.Province;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProvincialSystem {

  private String systemIdentifier;
  private String name;
  private ProvincialSystemType type;
  private Province province;
  private List<ServiceEndpoint> serviceEndpoints;
  private ProvincialSystemStatus status;
}
