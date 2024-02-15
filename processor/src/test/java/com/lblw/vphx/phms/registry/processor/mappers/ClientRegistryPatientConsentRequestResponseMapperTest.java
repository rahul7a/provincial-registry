package com.lblw.vphx.phms.registry.processor.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** The type Client registry patient consent request response mapper test. */
@ExtendWith(MockitoExtension.class)
class ClientRegistryPatientConsentRequestResponseMapperTest {

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
    ClientRegistryPatientConsentRequestResponseMapper
        clientRegistryPatientConsentRequestResponseMapper =
            new ClientRegistryPatientConsentRequestResponseMapper(
                provincialRequestProperties, uuidGenerator);
    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");

    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .requestId("Correlation_Id")
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .province(Province.QC)
            .build();

    ProvincialPatientConsentCriteria provincialPatientConsentCriteria =
        ProvincialPatientConsentCriteria.builder()
            .firstName("First_Name")
            .lastName("last_Name")
            .patientIdentifier("Patient_Identifier")
            .effectiveDateTime("20220110")
            .provincialRequestControl(provincialRequestControl)
            .build();

    ProvincialPatientConsentRequest provincialPatientConsentRequest =
        clientRegistryPatientConsentRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialPatientConsentCriteria);

    RequestBodyTransmissionWrapper requestBodyTransmissionWrapper =
        provincialPatientConsentRequest.getRequestBodyTransmissionWrapper();

    // assert

    assertNotNull(requestBodyTransmissionWrapper);
    assertEquals(
        "mockUUID",
        requestBodyTransmissionWrapper.getTransmissionUniqueIdentifier(),
        "transmissionUniqueIdentifier");
    assertEquals(
        provincialRequestControl.getRequestId(),
        provincialPatientConsentRequest.getRequestControlAct().getEventCorrelationId());
    assertEquals(
        provincialPatientConsentCriteria.getFirstName(),
        provincialPatientConsentRequest.getProvincialRequestPayload().getFirstName());
    assertEquals(
        provincialPatientConsentCriteria.getLastName(),
        provincialPatientConsentRequest.getProvincialRequestPayload().getLastName());
    assertEquals(
        provincialPatientConsentCriteria.getPatientIdentifier(),
        provincialPatientConsentRequest.getProvincialRequestPayload().getPatientIdentifier());
    assertEquals(
        provincialPatientConsentCriteria.getProvincialRequestControl(),
        provincialPatientConsentRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl());
    assertEquals(
        provincialPatientConsentCriteria.getEffectiveDateTime(),
        provincialPatientConsentRequest.getProvincialRequestPayload().getEffectiveDateTime());
    assertEquals(
        provincialPatientConsentCriteria.getProvincialRequestControl().getProvince(),
        provincialPatientConsentRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getProvince());
    assertEquals(
        provincialPatientConsentCriteria.getProvincialRequestControl().getPharmacy().getName(),
        provincialPatientConsentRequest
            .getProvincialRequestPayload()
            .getProvincialRequestControl()
            .getPharmacy()
            .getName());
  }
}
