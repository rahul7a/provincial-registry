package com.lblw.vphx.phms.transformation.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.provider.request.ProviderIdentifierType;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.context.Context;

@ExtendWith(SpringExtension.class)
@Import(ProvincialProviderSearchContextBuilder.class)
class ProvincialProviderSearchContextBuilderTest {

  Request request;

  @MockBean CodeableConceptService codeableConceptService;
  @Autowired ProvincialProviderSearchContextBuilder provincialProviderSearchContextBuilder;

  @Test
  void whenProviderIdentifierIsProviderId_thenProviderIdentifierRootShouldPopulateFromContext() {
    var provincialProviderSearchRequest = (ProvincialProviderSearchRequest) request;
    provincialProviderSearchRequest =
        ProvincialProviderSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialProviderSearchCriteria.builder()
                    .roleCode("roleCode")
                    .providerIdentifierType(ProviderIdentifierType.PROVINCIAL_PROVIDER_ID)
                    .providerIdentifierValue("providerIdentifierValue")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();
    Context context = new Context();

    provincialProviderSearchContextBuilder.setContext(context, provincialProviderSearchRequest);

    assertEquals("2.16.840.1.113883.4.277", context.getVariable(HL7Constants.PROVIDER_ID_ROOT));
    assertEquals(
        "providerIdentifierValue", context.getVariable(HL7Constants.PROVIDER_ID_EXTENSION));
  }

  @Test
  void whenProviderIdentifierIsBilling_thenProviderIdentifierRootShouldPopulateFromContext() {
    var provincialProviderSearchRequest = (ProvincialProviderSearchRequest) request;
    provincialProviderSearchRequest =
        ProvincialProviderSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialProviderSearchCriteria.builder()
                    .roleCode("roleCode")
                    .providerIdentifierType(ProviderIdentifierType.BILLING)
                    .providerIdentifierValue("providerIdentifierValue")
                    .roleSpecialityCode("roleSpecialityCode")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();
    Context context = new Context();

    when(codeableConceptService.findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
            any(), any()))
        .then((answer) -> Optional.of(Coding.builder().code(answer.getArgument(1)).build()));

    provincialProviderSearchContextBuilder.setContext(context, provincialProviderSearchRequest);

    assertEquals("2.16.124.10.101.1.60.105", context.getVariable(HL7Constants.PROVIDER_ID_ROOT));
    assertEquals(
        "providerIdentifierValue", context.getVariable(HL7Constants.PROVIDER_ID_EXTENSION));
    assertEquals("roleSpecialityCode", context.getVariable(HL7Constants.PROVIDER_SPECIALITY_CODE));
  }

  @Test
  void whenProviderIdentifierIsLicense_thenProviderIdentifierRootShouldPopulateFromContext() {
    var provincialProviderSearchRequest = (ProvincialProviderSearchRequest) request;
    provincialProviderSearchRequest =
        ProvincialProviderSearchRequest.builder()
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder().senderRoot("senderRoot").build())
            .requestControlAct(RequestControlAct.builder().eventRoot("eventRoot").build())
            .provincialRequestPayload(
                ProvincialProviderSearchCriteria.builder()
                    .roleCode("providerRoleCode")
                    .providerIdentifierType(ProviderIdentifierType.LICENSE)
                    .providerIdentifierValue("providerIdentifierValue")
                    .roleSpecialityCode("roleSpecialityCode")
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
                            .build())
                    .build())
            .build();
    Context context = new Context();

    when(codeableConceptService.findProvincialRoleCodingBySystemRoleCode(any(), any()))
        .then(
            (answer) ->
                Optional.of(Coding.builder().codingIdentifier(answer.getArgument(1)).build()));

    when(codeableConceptService.findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
            any(), any()))
        .then((answer) -> Optional.of(Coding.builder().code(answer.getArgument(1)).build()));

    provincialProviderSearchContextBuilder.setContext(context, provincialProviderSearchRequest);

    assertEquals("providerRoleCode", context.getVariable(HL7Constants.PROVIDER_ID_ROOT));
    assertEquals(
        "providerIdentifierValue", context.getVariable(HL7Constants.PROVIDER_ID_EXTENSION));
    assertEquals("roleSpecialityCode", context.getVariable(HL7Constants.PROVIDER_SPECIALITY_CODE));
  }
}
