package com.lblw.vphx.phms.registry.processor.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test cases to unit test {@link ClientRegistryPatientRequestResponseMapper}
 * ClientRegistryRequestMapper
 */
@ExtendWith(MockitoExtension.class)
class ClientRegistryPatientRequestResponseMapperTest {
  @Mock private ProvincialRequestProperties provincialRequestProperties;
  @Mock private UUIDGenerator uuidGenerator;

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
  void whenSearchCriteriaAddressIsAnEmptyObject_ThenSearchRequestAddressAsNull() throws Exception {
    ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper =
        new ClientRegistryPatientRequestResponseMapper(provincialRequestProperties, uuidGenerator);

    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .province(Province.QC)
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .requestId("Correlation_ID")
            .build();
    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .firstName("FirstName")
            .lastName("LastName")
            .provincialHealthNumber("Random_Number")
            .fatherFirstName("Father_FirstName")
            .fatherLastName("Father_LastName`")
            .motherFirstName("Mother_FirstName")
            .motherLastName("Mother_LastName")
            .gender(Gender.M)
            .address(null)
            .dateOfBirth(LocalDate.of(2010, 11, 01))
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialPatientSearchCriteria);
    assertNotNull(provincialPatientSearchRequest);
    assertThat(provincialPatientSearchCriteria.getAddress()).isNull();
    assertThat(provincialPatientSearchRequest.getProvincialRequestPayload().getAddress()).isNull();
  }

  @Test
  void whenSearchCriteriaGenderObjectIsEmpty_ThenSearchRequestPayloadGenderObjectIsNull()
      throws Exception {
    ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper =
        new ClientRegistryPatientRequestResponseMapper(provincialRequestProperties, uuidGenerator);

    Address address =
        Address.builder()
            .city("City")
            .streetAddressLine("Street_Line")
            .postalCode("Postal_Code")
            .province("QC")
            .build();
    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .province(Province.QC)
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .requestId("Correlation_ID")
            .build();
    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .firstName("FirstName")
            .lastName("LastName")
            .provincialHealthNumber("Random_Number")
            .fatherFirstName("Father_FirstName")
            .fatherLastName("Father_LastName`")
            .motherFirstName("Mother_FirstName")
            .motherLastName("Mother_LastName")
            .gender(null)
            .dateOfBirth(LocalDate.of(2010, 11, 01))
            .address(address)
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialPatientSearchCriteria);
    assertNotNull(provincialPatientSearchRequest);
    assertThat(provincialPatientSearchCriteria.getGender()).isNull();
    assertThat(provincialPatientSearchRequest.getProvincialRequestPayload().getGender()).isNull();
  }

  @Test
  void whenSearchCriteriaGenderIsMale_ThenSearchRequestPayloadGenderIsMale() throws Exception {
    ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper =
        new ClientRegistryPatientRequestResponseMapper(provincialRequestProperties, uuidGenerator);

    Address address =
        Address.builder()
            .city("City")
            .streetAddressLine("Street_Line")
            .postalCode("Postal_Code")
            .province("QC")
            .build();
    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .province(Province.QC)
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .requestId("Correlation_ID")
            .build();
    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .firstName("FirstName")
            .lastName("LastName")
            .provincialHealthNumber("Random_Number")
            .fatherFirstName("Father_FirstName")
            .fatherLastName("Father_LastName`")
            .motherFirstName("Mother_FirstName")
            .motherLastName("Mother_LastName")
            .gender(Gender.M)
            .dateOfBirth(LocalDate.of(2010, 11, 01))
            .address(address)
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialPatientSearchCriteria);
    assertNotNull(provincialPatientSearchRequest);
    assertEquals(Gender.M, provincialPatientSearchCriteria.getGender());
    assertEquals(
        Gender.M, provincialPatientSearchRequest.getProvincialRequestPayload().getGender());
  }

  @Test
  void
      whenSearchCriteriaFirstNameAndLastNameIsEmpty_ThenSearchRequestPayloadFirstNameLastNameIsNull()
          throws Exception {
    ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper =
        new ClientRegistryPatientRequestResponseMapper(provincialRequestProperties, uuidGenerator);

    Address address =
        Address.builder()
            .city("City")
            .streetAddressLine("Street_Line")
            .postalCode("Postal_Code")
            .province("QC")
            .build();
    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .province(Province.QC)
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .requestId("Correlation_ID")
            .build();
    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .firstName(null)
            .lastName(null)
            .provincialHealthNumber("Random_Number")
            .fatherFirstName("Father_FirstName")
            .fatherLastName("Father_LastName`")
            .motherFirstName("Mother_FirstName")
            .motherLastName("Mother_LastName")
            .gender(Gender.M)
            .dateOfBirth(LocalDate.of(2010, 11, 01))
            .address(address)
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialPatientSearchCriteria);
    assertNotNull(provincialPatientSearchRequest);
    assertThat(provincialPatientSearchCriteria.getFirstName()).isNull();
    assertThat(provincialPatientSearchCriteria.getLastName()).isNull();
    assertThat(provincialPatientSearchRequest.getProvincialRequestPayload().getFirstName())
        .isNull();
    assertThat(provincialPatientSearchRequest.getProvincialRequestPayload().getLastName()).isNull();
  }

  @Test
  void
      whenSearchCriteriaFirstNameAndLastNameIsNonEmpty_ThenSearchRequestPayloadFirstNameLastNameIsNonNull()
          throws Exception {
    ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper =
        new ClientRegistryPatientRequestResponseMapper(provincialRequestProperties, uuidGenerator);

    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .province(Province.QC)
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .requestId("Correlation_ID")
            .build();
    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .firstName("First_Name")
            .lastName("Last_Name")
            .provincialHealthNumber(null)
            .fatherFirstName(null)
            .fatherLastName(null)
            .motherFirstName(null)
            .motherLastName(null)
            .gender(null)
            .dateOfBirth(null)
            .address(null)
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialPatientSearchCriteria);
    assertNotNull(provincialPatientSearchRequest);
    assertEquals("First_Name", provincialPatientSearchCriteria.getFirstName());
    assertEquals("Last_Name", provincialPatientSearchCriteria.getLastName());
    assertEquals(
        "First_Name", provincialPatientSearchRequest.getProvincialRequestPayload().getFirstName());
    assertEquals(
        "Last_Name", provincialPatientSearchRequest.getProvincialRequestPayload().getLastName());
  }

  @Test
  void whenSearchCriteriaIsAnNonEmptyObject_ThenSearchRequestAsNonNull() throws Exception {
    ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper =
        new ClientRegistryPatientRequestResponseMapper(provincialRequestProperties, uuidGenerator);

    Address address =
        Address.builder()
            .city("City")
            .streetAddressLine("Street_Line")
            .postalCode("Postal_Code")
            .province("QC")
            .build();
    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .province(Province.QC)
            .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
            .requestId("Correlation_ID")
            .build();
    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .firstName("FirstName")
            .lastName("LastName")
            .provincialHealthNumber("Random_Number")
            .fatherFirstName("Father_FirstName")
            .fatherLastName("Father_LastName`")
            .motherFirstName("Mother_FirstName")
            .motherLastName("Mother_LastName")
            .gender(Gender.M)
            .dateOfBirth(LocalDate.of(2010, 11, 01))
            .address(address)
            .provincialRequestControl(provincialRequestControl)
            .build();

    Mockito.when(uuidGenerator.generateUUID()).thenReturn("mockUUID");
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(
            provincialPatientSearchCriteria);
    assertNotNull(provincialPatientSearchRequest);
    assertEquals(
        provincialPatientSearchCriteria.getProvincialRequestControl().getRequestId(),
        provincialPatientSearchRequest.getRequestControlAct().getEventCorrelationId());
    assertEquals(
        provincialPatientSearchCriteria.getFirstName(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getFirstName());
    assertEquals(
        provincialPatientSearchCriteria.getLastName(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getLastName());
    assertEquals(
        provincialPatientSearchCriteria.getFatherFirstName(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getFatherFirstName());
    assertEquals(
        provincialPatientSearchCriteria.getFatherLastName(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getFatherLastName());
    assertEquals(
        provincialPatientSearchCriteria.getGender(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getGender());
    assertEquals(
        provincialPatientSearchCriteria.getMotherFirstName(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getMotherFirstName());
    assertEquals(
        provincialPatientSearchCriteria.getMotherLastName(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getMotherLastName());
    assertEquals(
        provincialPatientSearchCriteria.getDateOfBirth(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getDateOfBirth());
    assertEquals(
        provincialPatientSearchCriteria.getProvincialHealthNumber(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getProvincialHealthNumber());
    assertEquals(
        provincialPatientSearchCriteria.getAddress().getStreetAddressLine(),
        provincialPatientSearchRequest
            .getProvincialRequestPayload()
            .getAddress()
            .getStreetAddressLine());
    assertEquals(
        provincialPatientSearchCriteria.getAddress().getProvince(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getAddress().getProvince());
    assertEquals(
        provincialPatientSearchCriteria.getAddress().getCity(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getAddress().getCity());
    assertEquals(
        provincialPatientSearchCriteria.getAddress().getCountry(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getAddress().getCountry());
    assertEquals(
        provincialPatientSearchCriteria.getAddress().getPostalCode(),
        provincialPatientSearchRequest.getProvincialRequestPayload().getAddress().getPostalCode());
    assertNotNull(provincialPatientSearchRequest.getRequestBodyTransmissionWrapper());
    assertEquals(
        "mockUUID",
        provincialPatientSearchRequest
            .getRequestBodyTransmissionWrapper()
            .getTransmissionUniqueIdentifier(),
        "transmissionUniqueIdentifier");
  }
}
