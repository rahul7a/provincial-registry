package com.lblw.vphx.phms.transformation.request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.location.request.LocationIdentifierType;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.context.Context;

@ExtendWith(SpringExtension.class)
@Import(ProvincialLocationSummarySearchContextBuilder.class)
class ProvincialLocationSummarySearchContextBuilderTest {
  @MockBean CodeableConceptService codeableConceptService;
  Request request;

  @Autowired
  ProvincialLocationSummarySearchContextBuilder provincialLocationSummarySearchContextBuilder;

  @Test
  void whenLocationIdentifierIsPermitType_thenLocationIdentifierRootShouldPopulateFromContext() {
    var provincialLocationSearchRequest = (ProvincialLocationSearchRequest) request;
    provincialLocationSearchRequest =
        ProvincialLocationSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialLocationSearchCriteria.builder()
                    .provincialLocationIdentifierType(LocationIdentifierType.PERMIT_NUMBER)
                    .provincialLocationIdentifierValue("locationIdentifierValue")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();
    Context context = new Context();

    provincialLocationSummarySearchContextBuilder.setContext(
        context, provincialLocationSearchRequest);

    Assert.assertEquals(
        "2.16.124.10.101.1.60.101", context.getVariable(HL7Constants.LOCATION_SUMMARY_ID_ROOT));
    Assert.assertEquals(
        "locationIdentifierValue", context.getVariable(HL7Constants.LOCATION_SUMMARY_ID_EXTENSION));
  }

  @Test
  void
      whenLocationIdentifierIsBillingNumberType_thenLocationIdentifierRootShouldPopulateFromContext() {
    var provincialLocationSearchRequest = (ProvincialLocationSearchRequest) request;
    provincialLocationSearchRequest =
        ProvincialLocationSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialLocationSearchCriteria.builder()
                    .locationType("locationType")
                    .provincialLocationIdentifierType(
                        LocationIdentifierType.PHARMACY_BILLING_NUMBER)
                    .provincialLocationIdentifierValue("locationIdentifierValue")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();
    Context context = new Context();

    when(codeableConceptService.findProvincialLocationTypeCodingBySystemCode(any(), any()))
        .thenReturn(Optional.of(Coding.builder().build()));

    provincialLocationSummarySearchContextBuilder.setContext(
        context, provincialLocationSearchRequest);

    Assert.assertEquals(
        "2.16.124.10.101.1.60.103", context.getVariable(HL7Constants.LOCATION_SUMMARY_ID_ROOT));
    Assert.assertEquals(
        "locationIdentifierValue", context.getVariable(HL7Constants.LOCATION_SUMMARY_ID_EXTENSION));
  }

  @Test
  void
      whenLocationIdentifierIsHealthInsurancePermitNumber_thenLocationIdentifierRootShouldPopulateFromContext() {
    var provincialLocationSearchRequest = (ProvincialLocationSearchRequest) request;
    provincialLocationSearchRequest =
        ProvincialLocationSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialLocationSearchCriteria.builder()
                    .provincialLocationIdentifierType(
                        LocationIdentifierType.PUBLIC_HEALTH_INSURANCE_PERMIT_NUMBER)
                    .provincialLocationIdentifierValue("locationIdentifierValue")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();
    Context context = new Context();
    provincialLocationSummarySearchContextBuilder.setContext(
        context, provincialLocationSearchRequest);
    Assert.assertEquals(
        "2.16.124.10.101.1.60.104", context.getVariable(HL7Constants.LOCATION_SUMMARY_ID_ROOT));
    Assert.assertEquals(
        "locationIdentifierValue", context.getVariable(HL7Constants.LOCATION_SUMMARY_ID_EXTENSION));
  }
}
