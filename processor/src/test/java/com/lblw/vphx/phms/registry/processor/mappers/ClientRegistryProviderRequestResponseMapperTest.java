package com.lblw.vphx.phms.registry.processor.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.provider.request.ProviderRequestAddress;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** The type Client registry provider request response mapper test. */
// import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ClientRegistryProviderRequestResponseMapperTest {
  @Mock private ProvincialRequestProperties provincialRequestProperties;
  @Mock private UUIDGenerator uuidGenerator;

  /**
   * Convert search criteria to request.
   *
   * @throws Exception
   */
  @BeforeEach
  public void beforeEach() {
    provincialRequestProperties = new ProvincialRequestProperties();
    ProvincialRequestProperties.Request request = new ProvincialRequestProperties.Request();
    ProvincialRequestProperties.Sender sender =
        new ProvincialRequestProperties.Sender(
            "extension", "name", "processingCode", "pharmacyGroup", "locationId", "versionNumber");
    request.setSender(sender);
    provincialRequestProperties.setRequest(request);
  }

  @Test
  void convertSearchCriteriaToRequest() throws Exception {
    ClientRegistryProviderRequestResponseMapper clientRegistryProviderRequestResponseMapper =
        new ClientRegistryProviderRequestResponseMapper(provincialRequestProperties, uuidGenerator);
    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .requestId("Correlation_Id")
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .province(Province.QC)
            .build();

    ProvincialProviderSearchCriteria provincialProviderSearchCriteria =
        ProvincialProviderSearchCriteria.builder()
            .dateOfBirth("20211212")
            .firstName("Karim")
            .gender(Gender.M)
            .lastName("Jotun")
            .roleCode("ijk09")
            .roleSpecialityCode("random_code")
            .providerRequestAddress(
                ProviderRequestAddress.builder().city("city").postalCode("code").build())
            .provincialRequestControl(provincialRequestControl)
            .build();
    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");
    ProvincialProviderSearchRequest provincialProviderSearchRequest =
        clientRegistryProviderRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialProviderSearchCriteria);

    // assert

    RequestBodyTransmissionWrapper requestBodyTransmissionWrapper =
        provincialProviderSearchRequest.getRequestBodyTransmissionWrapper();
    assertNotNull(requestBodyTransmissionWrapper);
    assertEquals(
        "mockUUID",
        requestBodyTransmissionWrapper.getTransmissionUniqueIdentifier(),
        "transmissionUniqueIdentifier");
    assertEquals(
        provincialProviderSearchCriteria.getFirstName(),
        provincialProviderSearchRequest.getProvincialRequestPayload().getFirstName());
    assertEquals(
        provincialProviderSearchCriteria.getGender(),
        provincialProviderSearchRequest.getProvincialRequestPayload().getGender());

    assertEquals(
        provincialProviderSearchCriteria.getProviderRequestAddress(),
        provincialProviderSearchRequest.getProvincialRequestPayload().getProviderRequestAddress());

    assertEquals(
        provincialProviderSearchCriteria.getProviderIdentifierType(),
        provincialProviderSearchRequest.getProvincialRequestPayload().getProviderIdentifierType());
    assertEquals(
        provincialProviderSearchCriteria.getLastName(),
        provincialProviderSearchRequest.getProvincialRequestPayload().getLastName());
    assertEquals(
        provincialProviderSearchCriteria.getRoleCode(),
        provincialProviderSearchRequest.getProvincialRequestPayload().getRoleCode());
    assertEquals(
        provincialProviderSearchCriteria.getRoleSpecialityCode(),
        provincialProviderSearchRequest.getProvincialRequestPayload().getRoleSpecialityCode());
    assertEquals(
        provincialRequestControl.getRequestId(),
        provincialProviderSearchRequest.getRequestControlAct().getEventCorrelationId());
    assertEquals(
        provincialRequestControl.getProvince(),
        provincialProviderSearchRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getProvince());
    assertEquals(
        provincialRequestControl.getPharmacy().getName(),
        provincialProviderSearchRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getPharmacy()
            .getName());
  }
}
