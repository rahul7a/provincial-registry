package com.lblw.vphx.phms.transformation.response.transformers.locationsummary.mappers;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.databind.BindOne;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.Telecom;
import com.lblw.vphx.phms.domain.location.LocationAddress;
import com.lblw.vphx.phms.domain.location.ProvincialLocation;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
public class ProvincialLocationMapper extends ProvincialLocation {
  private final CodeableConceptService codeableConceptService;

  @XPathTarget
  public void bindIdentifier(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/serviceDeliveryLocation/id[@root=\"2.16.840.1.113883.4.273\"]/@extension")
          String identifier) {
    if (StringUtils.isBlank(identifier)) {
      return;
    }
    super.setIdentifier(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.LOCATION)
            .value(identifier)
            .assigner(HL7Constants.QC)
            .system(HL7Constants.NIU_O)
            .build());
  }

  @XPathTarget
  public void bindName(
      @XPathTarget.Binding(
              xPath = "registrationRequest/subject/serviceDeliveryLocation/name/text()")
          String name) {
    if (StringUtils.isBlank(name)) {
      return;
    }
    super.setName(name);
  }

  @XPathTarget
  public Mono<Void> bindLocationType(
      @XPathTarget.Binding(xPath = "registrationRequest/subject/serviceDeliveryLocation/code/@code")
          String provincialCode,
      @XPathTarget.Binding(
              xPath = "registrationRequest/subject/serviceDeliveryLocation/code/@nullFlavor")
          String nullFlavorCode) {
    return Mono.fromRunnable(
            () -> {
              String code = null;

              if (StringUtils.isNotBlank(nullFlavorCode)) {
                code = nullFlavorCode;
              } else if (StringUtils.isNotBlank(provincialCode)) {
                code = provincialCode;
              }

              codeableConceptService
                  .findSystemLocationTypeCodingByProvincialCode(Province.QC, code)
                  .ifPresent(super::setLocationType);
            })
        .then()
        .subscribeOn(Schedulers.boundedElastic());
  }

  @XPathTarget
  public void bindAddress(
      @XPathTarget.Binding(xPath = "registrationRequest/subject/serviceDeliveryLocation/addr")
          BindOne<LocationAddress> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setAddress(binder.apply(LocationAddressMapper::new));
  }

  @XPathTarget
  public void bindRegion(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/serviceDeliveryLocation/indirectAuthority/territorialAuthority/id[@root=\"2.16.124.10.101.1.60.1.10\"]/@extension")
          String region) {
    if (StringUtils.isBlank(region)) {
      return;
    }
    super.setRegion(region);
  }

  @XPathTarget
  public void bindTelecom() {
    super.setTelecom(Telecom.builder().build());
  }
}
