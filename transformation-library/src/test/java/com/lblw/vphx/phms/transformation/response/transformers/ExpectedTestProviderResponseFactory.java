package com.lblw.vphx.phms.transformation.response.transformers;

import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseHeader;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponseControlAct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public class ExpectedTestProviderResponseFactory {
  public ProvincialProviderSearchResponse buildExpectedProvincialProviderSearchResponse(
      String caseName) {
    String expectedPropertiesFile = null;
    switch (caseName) {
      case "success":
        expectedPropertiesFile = "providerprofile/expectation/success.properties";
        break;
    }
    return buildExpectedProvincialProviderSearchResponse(
        loadExpectedProperties(expectedPropertiesFile));
  }

  private Properties loadExpectedProperties(String propertyPath) {
    Properties prop = new Properties();
    try {
      prop.load(
          ExpectedTestResponseFactory.class.getClassLoader().getResourceAsStream(propertyPath));
    } catch (IOException e) {
      // do nothing. This is test.
    }
    return prop;
  }

  private Integer getMatchingIndex(Properties properties, String s) {
    final Object obj = properties.get(s);
    if (obj == null) {
      return null;
    }
    return (int) Float.parseFloat((String) obj);
  }

  private String get(Properties properties, String s) {
    final Object obj = properties.get(s);
    if (obj == null || ((String) obj).equalsIgnoreCase("null")) {
      return null;
    }
    return (String) obj;
  }

  private ProvincialProviderSearchResponse buildExpectedProvincialProviderSearchResponse(
      Properties properties) {
    return ProvincialProviderSearchResponse.builder()
        .responseHeader(buildResponseHeader(properties))
        .responseControlAct(buildResponseBodyControlAct(properties))
        .responseBodyTransmissionWrapper(buildResponseBodyTransmissionWrapper(properties))
        .build();
  }

  private ResponseHeader buildResponseHeader(Properties properties) {
    return ResponseHeader.builder()
        .sessionId(get(properties, "ResponseHeader.sessionId"))
        .trackingId(get(properties, "ResponseHeader.trackingId"))
        .transactionId(get(properties, "ResponseHeader.transactionId"))
        .build();
  }

  private ResponseBodyTransmissionWrapper buildResponseBodyTransmissionWrapper(
      Properties properties) {
    return ResponseBodyTransmissionWrapper.builder()
        .acknowledgeTypeCode(get(properties, "ResponseBodyTransmissionWrapper.ackTypeCode"))
        .acknowledgementDetails(buildExpectedAcknowledgementDetails(properties))
        .transmissionUniqueIdentifier(
            get(properties, "ResponseBodyTransmissionWrapper.transmissionUUID"))
        .build();
  }

  private List<AcknowledgementDetails> buildExpectedAcknowledgementDetails(Properties properties) {
    final String acknowledgementDetailsExists = get(properties, "AcknowledgementDetails");
    if (acknowledgementDetailsExists == null) {
      return Collections.emptyList();
    }
    int numberOfAcknowledgementDetails = 0;
    try {
      numberOfAcknowledgementDetails = Integer.parseInt(acknowledgementDetailsExists);
    } catch (NumberFormatException nfe) {
      numberOfAcknowledgementDetails = 0;
    }
    if (numberOfAcknowledgementDetails == 0) {
      return List.of(
          AcknowledgementDetails.builder()
              .typeCode(get(properties, "AcknowledgementDetails.typeCode"))
              .code(get(properties, "AcknowledgementDetails.code"))
              .location(get(properties, "AcknowledgementDetails.location"))
              .text(get(properties, "AcknowledgementDetails.text"))
              .build());
    }
    List<AcknowledgementDetails> acknowledgementDetails = new ArrayList<>();
    for (int index = 0; index < numberOfAcknowledgementDetails; index++) {
      String propertyPrefix = "AcknowledgementDetails[" + index + "]";
      final AcknowledgementDetails acknowledgementDetailInstance =
          AcknowledgementDetails.builder()
              .typeCode(get(properties, propertyPrefix + ".typeCode"))
              .code(get(properties, propertyPrefix + ".code"))
              .location(get(properties, propertyPrefix + ".location"))
              .text(get(properties, propertyPrefix + ".text"))
              .build();
      acknowledgementDetails.add(acknowledgementDetailInstance);
    }
    return acknowledgementDetails;
  }

  private ProvincialProviderSearchResponseControlAct buildResponseBodyControlAct(
      Properties properties) {
    return ProvincialProviderSearchResponseControlAct.builder()
        .eventRoot(get(properties, "ProvincialProviderSearchResponseBodyControlAct.rootId"))
        .eventCorrelationId(
            get(properties, "ProvincialProviderSearchResponseBodyControlAct.eventRootUid"))
        .provincialProviderSearchCriteria(
            buildProvincialProviderSearchCriteriaInResponse(properties))
        .build();
  }

  private ProvincialProviderSearchCriteria buildProvincialProviderSearchCriteriaInResponse(
      Properties properties) {

    final String providerSearchCriteria = get(properties, "ProviderSearchCriteria");
    if (providerSearchCriteria.equalsIgnoreCase("null")) {
      return ProvincialProviderSearchCriteria.builder().build();
    }
    final String gender = get(properties, "ProviderSearchCriteria.gender");
    return ProvincialProviderSearchCriteria.builder()
        .firstName(get(properties, "ProviderSearchCriteria.firstName"))
        .lastName(get(properties, "ProviderSearchCriteria.lastName"))
        .gender(StringUtils.isBlank(gender) ? null : Gender.valueOf(gender))
        .build();
  }
}
