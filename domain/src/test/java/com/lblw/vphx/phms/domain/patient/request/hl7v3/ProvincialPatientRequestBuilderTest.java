package com.lblw.vphx.phms.domain.patient.request.hl7v3;

import static org.junit.jupiter.api.Assertions.*;

import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestHeader;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProvincialPatientRequestBuilderTest {

  @Test
  void test() {

    RequestHeader requestHeader = RequestHeader.builder().eSignature("eSigned").build();

    RequestBodyTransmissionWrapper requestBodyTransmissionWrapper =
        RequestBodyTransmissionWrapper.builder()
            .transmissionCreationDateTime("00001")
            .transmissionUniqueIdentifier("uniqueId")
            .processingCode("D")
            .senderApplicationId("senderApplicationId")
            .senderRoot("senderRoot")
            .build();

    RequestControlAct requestControlAct =
        RequestControlAct.builder()
            .eventCorrelationId("eventCorrelationId")
            .eventRoot("eventRoot")
            .build();

    ProvincialRequestControl provincialRequestControl =
        ProvincialRequestControl.builder()
            .pharmacy(Pharmacy.builder().name("pharmacyId").build())
            .requestId("correlationId")
            .build();

    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .lastName("lastName")
            .firstName("firstName")
            .dateOfBirth(LocalDate.parse("2001-01-13"))
            .gender(Gender.F)
            .fatherLastName("fatherLastName")
            .fatherFirstName("fatherFirstName")
            .motherLastName("motherLastName")
            .motherFirstName("motherFirstName")
            .provincialHealthNumber("healthNumber")
            .address(
                Address.builder()
                    .streetAddressLine("streetAddressLine")
                    .city("city")
                    .postalCode("postalCode")
                    .province("province")
                    .country("CA")
                    .build())
            .provincialRequestControl(provincialRequestControl)
            .build();

    provincialPatientSearchCriteria.setProvincialRequestControl(provincialRequestControl);

    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        ProvincialPatientSearchRequest.builder()
            .requestHeader(requestHeader)
            .requestBodyTransmissionWrapper(requestBodyTransmissionWrapper)
            .requestControlAct(requestControlAct)
            .provincialRequestPayload(provincialPatientSearchCriteria)
            .build();

    assertAll(
        () -> {
          var header = provincialPatientSearchRequest.getRequestHeader();
          assertAll(
              "header",
              () -> assertNotNull(header),
              () -> assertEquals("eSigned", header.getESignature(), "signature"));
        },
        () -> {
          var transmissionWrapper =
              provincialPatientSearchRequest.getRequestBodyTransmissionWrapper();
          assertAll(
              "transmission wrapper",
              () -> assertNotNull(transmissionWrapper),
              () ->
                  assertEquals(
                      "00001",
                      transmissionWrapper.getTransmissionCreationDateTime(),
                      "createdDateTime"),
              () ->
                  assertEquals(
                      "uniqueId",
                      transmissionWrapper.getTransmissionUniqueIdentifier(),
                      "transmissionUniqueIdentifier"),
              () -> assertEquals("D", transmissionWrapper.getProcessingCode(), "processingCode"),
              () ->
                  assertEquals(
                      "senderApplicationId",
                      transmissionWrapper.getSenderApplicationId(),
                      "senderApplicationId"),
              () -> assertEquals("senderRoot", transmissionWrapper.getSenderRoot(), "senderRoot"));
        },
        () -> {
          var controlAct = provincialPatientSearchRequest.getRequestControlAct();
          assertAll(
              "control act",
              () -> assertNotNull(controlAct),
              () -> assertEquals("eventRoot", controlAct.getEventRoot(), "eventRoot"),
              () ->
                  assertEquals(
                      "eventCorrelationId",
                      controlAct.getEventCorrelationId(),
                      "eventCorrelationId"));
        },
        () -> {
          var searchCriteria = provincialPatientSearchRequest.getProvincialRequestPayload();
          assertAll(
              "search criteria",
              () -> assertNotNull(searchCriteria),
              () ->
                  assertEquals(
                      "healthNumber",
                      searchCriteria.getProvincialHealthNumber(),
                      "provincialHealthNumber"),
              () -> assertEquals("firstName", searchCriteria.getFirstName(), "firstName"),
              () -> assertEquals("lastName", searchCriteria.getLastName(), "lastName"),
              () -> assertEquals(Gender.F, searchCriteria.getGender(), "gender"),
              () ->
                  assertEquals(
                      LocalDate.parse("2001-01-13"),
                      searchCriteria.getDateOfBirth(),
                      "dateOfBirth"),
              () ->
                  assertEquals(
                      "fatherFirstName", searchCriteria.getFatherFirstName(), "fatherFirstName"),
              () ->
                  assertEquals(
                      "fatherLastName", searchCriteria.getFatherLastName(), "fatherLastName"),
              () ->
                  assertEquals(
                      "motherFirstName", searchCriteria.getMotherFirstName(), "motherFirstName"),
              () ->
                  assertEquals(
                      "motherLastName", searchCriteria.getMotherLastName(), "motherLastName"));
          var address = searchCriteria.getAddress();
          assertAll(
              "locationRequestAddress",
              () ->
                  assertEquals(
                      "streetAddressLine", address.getStreetAddressLine(), "streetAddressLine"),
              () -> assertEquals("city", address.getCity(), "city"),
              () -> assertEquals("province", address.getProvince(), "province"),
              () -> assertEquals("CA", address.getCountry(), "country"),
              () -> assertEquals("postalCode", address.getPostalCode(), "postalCode"));
        });
  }
}
