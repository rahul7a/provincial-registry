package com.lblw.vphx.phms.transformation.response.transformers.patientsearch.mappers;

import com.lblw.vphx.phms.common.databind.XPathTarget;
import lombok.Getter;

@Getter
public class PersonalRelationshipMapper {
  private String code;
  private String firstName;
  private String lastName;

  @XPathTarget
  public void bindCode(@XPathTarget.Binding(xPath = "code/@code") String code) {
    this.code = code;
  }

  @XPathTarget
  public void bindFirstName(
      @XPathTarget.Binding(xPath = "relationshipHolder/name/given/text()") String firstName) {
    this.firstName = firstName;
  }

  @XPathTarget
  public void bindLastName(
      @XPathTarget.Binding(xPath = "relationshipHolder/name/family/text()") String lastName) {
    this.lastName = lastName;
  }
}
