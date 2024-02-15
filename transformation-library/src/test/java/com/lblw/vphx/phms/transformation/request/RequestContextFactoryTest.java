package com.lblw.vphx.phms.transformation.request;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.location.request.LocationIdentifierType;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.provider.request.ProviderIdentifierType;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.context.Context;

@ExtendWith(SpringExtension.class)
@Import(RequestContextFactory.class)
class RequestContextFactoryTest {
  @Autowired RequestContextFactory requestContextFactory;
  @MockBean CodeableConceptService codeableConceptService;

  Request request;
  MessageProcess messageProcess;

  @Test
  void
      givenPermitNumberLocationIdentifierType_whenLocationSummary_thenSetAppropriatelocationSummaryIDRoot() {
    messageProcess = MessageProcess.LOCATION_SEARCH;
    var provincialLocationSearchRequest = (ProvincialLocationSearchRequest) request;
    provincialLocationSearchRequest =
        ProvincialLocationSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialLocationSearchCriteria.builder()
                    .provincialLocationIdentifierType(LocationIdentifierType.PERMIT_NUMBER)
                    .provincialLocationIdentifierValue("89100")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();

    Context context =
        requestContextFactory.createContext(provincialLocationSearchRequest, messageProcess);
    Assert.assertEquals("2.16.124.10.101.1.60.101", context.getVariable("locationSummaryIdRoot"));
    Assert.assertEquals("89100", context.getVariable("locationSummaryIdExtension"));
  }

  @Test
  void givenBillingProviderIdentifierType_whenProviderSearch_thenSetAppropriateProviderIDRoot() {
    messageProcess = MessageProcess.PROVIDER_SEARCH;
    var provincialProviderSearchRequest = (ProvincialProviderSearchRequest) request;
    provincialProviderSearchRequest =
        ProvincialProviderSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialProviderSearchCriteria.builder()
                    .providerIdentifierType(ProviderIdentifierType.BILLING)
                    .providerIdentifierValue("121501")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();

    Context context =
        requestContextFactory.createContext(provincialProviderSearchRequest, messageProcess);
    Assert.assertEquals("2.16.124.10.101.1.60.105", context.getVariable("providerIDRoot"));
    Assert.assertEquals("121501", context.getVariable("providerIDExtension"));
  }

  @Test
  void givenSinProviderIdentifierType_whenProviderSearch_thenSetAppropriateProviderIDRoot() {
    messageProcess = MessageProcess.PROVIDER_SEARCH;
    var provincialProviderSearchRequest = (ProvincialProviderSearchRequest) request;
    provincialProviderSearchRequest =
        ProvincialProviderSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialProviderSearchCriteria.builder()
                    .providerIdentifierType(ProviderIdentifierType.SIN)
                    .providerIdentifierValue("277128294")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();

    Context context =
        requestContextFactory.createContext(provincialProviderSearchRequest, messageProcess);
    Assert.assertEquals("2.16.840.1.113883.4.272", context.getVariable("providerIDRoot"));
    Assert.assertEquals("277128294", context.getVariable("providerIDExtension"));
  }
}
