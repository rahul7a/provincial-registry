package com.lblw.vphx.phms.registry.processor.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** The type Client registry location details request response mapper test. */
@ExtendWith(MockitoExtension.class)
class ClientRegistryLocationDetailsRequestResponseMapperTest {
  @Mock private ProvincialRequestProperties provincialRequestProperties;
  @Mock private UUIDGenerator uuidGenerator;

  /**
   * Convert search criteria to request.
   *
   * @throws Exception
   */
  @Test
  void convertSearchCriteriaToRequest() throws Exception {
    ProvincialRequestProperties provincialRequestProperties = new ProvincialRequestProperties();

    ProvincialRequestProperties.Request request = new ProvincialRequestProperties.Request();
    ProvincialRequestProperties.Sender sender =
        new ProvincialRequestProperties.Sender(
            "extension", "name", "processingCode", "pharmacyGroup", "locationId", "versionNumber");
    request.setSender(sender);
    provincialRequestProperties.setRequest(request);
    ClientRegistryLocationDetailsRequestResponseMapper
        clientRegistryLocationDetailsRequestResponseMapper =
            new ClientRegistryLocationDetailsRequestResponseMapper(
                provincialRequestProperties, uuidGenerator);
    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .requestId("Correlation_Id")
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .province(Province.QC)
            .build();

    ProvincialLocationDetailsCriteria provincialLocationDetailsCriteria =
        ProvincialLocationDetailsCriteria.builder()
            .identifier("Random_id")
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");

    ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
        clientRegistryLocationDetailsRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialLocationDetailsCriteria);

    // assert

    assertNotNull(provincialLocationDetailsRequest);
    assertNotNull(provincialLocationDetailsRequest.getRequestBodyTransmissionWrapper());
    assertEquals(
        "mockUUID",
        provincialLocationDetailsRequest
            .getRequestBodyTransmissionWrapper()
            .getTransmissionUniqueIdentifier(),
        "transmissionUniqueIdentifier");
    assertEquals(
        provincialRequestControl.getRequestId(),
        provincialLocationDetailsRequest.getRequestControlAct().getEventCorrelationId());
    assertEquals(
        provincialRequestProperties.getRequest().getSender().getControlActRoot(),
        provincialLocationDetailsRequest.getRequestControlAct().getEventRoot());
    assertEquals(
        provincialLocationDetailsCriteria,
        provincialLocationDetailsRequest.getProvincialRequestPayload());
    assertEquals(
        provincialLocationDetailsCriteria.getIdentifier(),
        provincialLocationDetailsRequest.getProvincialRequestPayload().getIdentifier());
    assertEquals(
        provincialLocationDetailsCriteria.getProvincialRequestControl(),
        provincialLocationDetailsRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl());
    assertEquals(
        provincialLocationDetailsCriteria.getProvincialRequestControl().getProvince(),
        provincialLocationDetailsRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getProvince());
    assertEquals(
        provincialLocationDetailsCriteria.getProvincialRequestControl().getPharmacy().getName(),
        provincialLocationDetailsRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getPharmacy()
            .getName());
  }
}
