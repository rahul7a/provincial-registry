package com.lblw.vphx.phms.transformation.response.transformers.locationsummary.mappers;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.location.ProvincialLocation;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProvincialLocationSummariesMapper extends ProvincialLocationSummaries {
  private final CodeableConceptService codeableConceptService;

  @XPathTarget
  public void bindProvincialLocationSummaries(
      @XPathTarget.Binding(xPath = "controlActEvent/subject[x]")
          BindMany<ProvincialLocation> binder) {
    if (Objects.isNull(binder)) {
      return;
    }

    super.setProvincialLocations(
        binder
            .apply(() -> new ProvincialLocationMapper(codeableConceptService))
            .collect(Collectors.toList()));
  }
}
