package com.lblw.vphx.phms.transformation.response.transformers.providersearch.mappers;

import com.lblw.vphx.phms.common.databind.XPathTarget;
import lombok.Getter;

@Getter
public class ProviderSpecialityMapper {
  private String code;
  private String nullFlavorCode;

  @XPathTarget
  public void bindCode(
      @XPathTarget.Binding(xPath = "qualifiedEntity/code/@code") String code,
      @XPathTarget.Binding(xPath = "qualifiedEntity/code/@nullFlavor") String nullFlavorCode) {
    this.code = code;
    this.nullFlavorCode = nullFlavorCode;
  }
}
