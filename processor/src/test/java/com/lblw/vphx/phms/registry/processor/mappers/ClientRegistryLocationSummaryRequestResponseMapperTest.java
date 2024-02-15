package com.lblw.vphx.phms.registry.processor.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.location.request.LocationIdentifierType;
import com.lblw.vphx.phms.domain.location.request.LocationRequestAddress;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** The type Client registry location summary request response mapper test. */
@ExtendWith(MockitoExtension.class)
class ClientRegistryLocationSummaryRequestResponseMapperTest {

  @Mock private ProvincialRequestProperties provincialRequestProperties;
  @Mock private UUIDGenerator uuidGenerator;
  /**
   * Convert search criteria to request test.
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
  void convertSearchCriteriaToRequestTest() throws Exception {
    ClientRegistryLocationSummaryRequestResponseMapper
        clientRegistryLocationSummaryRequestResponseMapper =
            new ClientRegistryLocationSummaryRequestResponseMapper(
                provincialRequestProperties, uuidGenerator);

    LocationRequestAddress locationRequestAddress =
        LocationRequestAddress.builder()
            .streetAddressLine("Street_Line")
            .city("City")
            .country("Country")
            .postalCode("Postal_Code")
            .build();
    LocationIdentifierType locationIdentifierType = LocationIdentifierType.PERMIT_NUMBER;
    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .requestId("Correlation_Id")
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .province(Province.QC)
            .build();

    ProvincialLocationSearchCriteria provincialLocationSearchCriteria =
        ProvincialLocationSearchCriteria.builder()
            .provincialLocationIdentifierValue("Identifier")
            .provincialLocationIdentifierType(locationIdentifierType)
            .locationName("Location_name")
            .locationRequestAddress(locationRequestAddress)
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");

    ProvincialLocationSearchRequest provincialLocationSearchRequest =
        clientRegistryLocationSummaryRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialLocationSearchCriteria);

    RequestBodyTransmissionWrapper requestBodyTransmissionWrapper =
        provincialLocationSearchRequest.getRequestBodyTransmissionWrapper();

    // assert

    assertNotNull(provincialLocationSearchRequest);
    assertNotNull(requestBodyTransmissionWrapper);
    assertEquals(
        "mockUUID",
        requestBodyTransmissionWrapper.getTransmissionUniqueIdentifier(),
        "transmissionUniqueIdentifier");
    assertEquals(
        provincialRequestControl.getRequestId(),
        provincialLocationSearchRequest.getRequestControlAct().getEventCorrelationId());
    assertEquals(
        provincialLocationSearchCriteria,
        provincialLocationSearchRequest.getProvincialRequestPayload());
    assertEquals(
        provincialLocationSearchCriteria.getLocationName(),
        provincialLocationSearchRequest.getProvincialRequestPayload().getLocationName());
    assertEquals(
        provincialLocationSearchCriteria.getLocationType(),
        provincialLocationSearchRequest.getProvincialRequestPayload().getLocationType());
    assertEquals(
        provincialLocationSearchCriteria.getLocationRequestAddress(),
        provincialLocationSearchRequest.getProvincialRequestPayload().getLocationRequestAddress());
    assertEquals(
        provincialLocationSearchCriteria.getProvincialRequestControl(),
        provincialLocationSearchRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl());
    assertEquals(
        provincialLocationSearchCriteria.getProvincialLocationIdentifierType(),
        provincialLocationSearchRequest
            .getProvincialRequestPayload()
            .getProvincialLocationIdentifierType());
    assertEquals(
        provincialLocationSearchCriteria.getProvincialLocationIdentifierValue(),
        provincialLocationSearchRequest
            .getProvincialRequestPayload()
            .getProvincialLocationIdentifierValue());
    assertEquals(
        provincialLocationSearchCriteria.getProvincialRequestControl().getProvince(),
        provincialLocationSearchRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getProvince());
    assertEquals(
        provincialLocationSearchCriteria.getProvincialRequestControl().getPharmacy().getName(),
        provincialLocationSearchRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getPharmacy()
            .getName());
  }
}
