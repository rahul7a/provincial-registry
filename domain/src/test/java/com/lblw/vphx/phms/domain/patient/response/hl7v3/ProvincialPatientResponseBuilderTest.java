package com.lblw.vphx.phms.domain.patient.response.hl7v3;

import static org.junit.jupiter.api.Assertions.*;

import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.*;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProvincialPatientResponseBuilderTest {

  @Test
  void test() {

    var responseHeader =
        ResponseHeader.builder()
            .transactionId("transactionId")
            .sessionId("sessionId")
            .trackingId("trackingId")
            .build();

    var responseTransmissionWrapper =
        ResponseBodyTransmissionWrapper.builder()
            .acknowledgeTypeCode("acknowledgeTypeCode")
            .transmissionUniqueIdentifier("uniqueId")
            .acknowledgementDetails(
                List.of(
                    AcknowledgementDetails.builder()
                        .code("code")
                        .location("location")
                        .typeCode("typeCode")
                        .text("text")
                        .build()))
            .build();

    ProvincialPatientSearchCriteria responseProvincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .lastName("lastName")
            .firstName("firstName")
            .dateOfBirth(LocalDate.parse("2001-01-16"))
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
            .build();

    var responseControlAct =
        ProvincialPatientSearchResponseControlAct.builder()
            .eventRoot("eventRoot")
            .eventCorrelationId("eventCorrelationId")
            .provincialPatientSearchCriteria(responseProvincialPatientSearchCriteria)
            .build();

    var responsePatientProfile =
        ProvincialPatientProfile.builder()
            .identifier(
                SystemIdentifier.builder()
                    .value("1234")
                    .assigner("Qc")
                    .system("NIUU")
                    .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
                    .build())
            .provincialHealthNumber(
                SystemIdentifier.builder()
                    .value("9876")
                    .assigner("Qc")
                    .system("NAM")
                    .type(SystemIdentifier.IDENTIFIER_TYPE.HEALTH_NUMBER)
                    .build())
            .lastName("lastname")
            .firstName("firstName")
            .dateOfBirth(LocalDate.parse("2001-01-16"))
            .gender(Gender.F)
            .fatherFirstName("fatherFirstName")
            .motherLastName("motherLastName")
            .address(
                Address.builder()
                    .streetAddressLine("streetAddressLine")
                    .city("city")
                    .province("Qc")
                    .country("CA")
                    .postalCode("postalCode")
                    .build())
            .deceasedDate(LocalDate.parse("2022-05-19"))
            .deceasedIndicator(Boolean.TRUE)
            .build();

    var responseAcknowledgement =
        ProvincialResponseAcknowledgement.builder()
            .operationOutcome(OperationOutcome.builder().build())
            .auditEvent(AuditEvent.builder().build())
            .build();
    responsePatientProfile.setProvincialResponseAcknowledgement(responseAcknowledgement);

    var responseQueryAck =
        QueryAcknowledgement.builder()
            .queryResponseCode("queryResponseCode")
            .resultCurrentQuantity("resultCurrentQuantity")
            .resultTotalQuantity("resultTotalQuantity")
            .resultRemainingQuantity("resultRemainingQuantity")
            .build();

    var responseDetectedEvent =
        DetectedIssue.builder().eventCode("eventCode").eventText("eventText").build();

    ProvincialPatientSearchResponse response =
        ProvincialPatientSearchResponse.builder()
            .responseHeader(responseHeader)
            .responseBodyTransmissionWrapper(responseTransmissionWrapper)
            .responseControlAct(responseControlAct)
            .provincialResponsePayload(responsePatientProfile)
            .queryAcknowledgement(responseQueryAck)
            .detectedIssues(List.of(responseDetectedEvent))
            .build();

    assertAll(
        () -> {
          var header = response.getResponseHeader();
          assertAll(
              "header",
              () -> assertNotNull(header),
              () -> assertEquals("transactionId", header.getTransactionId(), "transactionId"),
              () -> assertEquals("sessionId", header.getSessionId(), "sessionId"),
              () -> assertEquals("trackingId", header.getTrackingId(), "trackingId"));
        },
        () -> {
          var transmissionWrapper = response.getResponseBodyTransmissionWrapper();
          assertAll(
              "transmission wrapper",
              () ->
                  assertEquals(
                      responseTransmissionWrapper, transmissionWrapper, "transmission wrapper"));
        },
        () -> {
          var controlAct = response.getResponseControlAct();
          assertAll(
              "control act",
              () -> assertNotNull(controlAct),
              () -> assertEquals("eventRoot", controlAct.getEventRoot(), "event root"),
              () ->
                  assertEquals(
                      "eventCorrelationId",
                      controlAct.getEventCorrelationId(),
                      "event correlation id"),
              // () -> assertEquals(responseControlAct, controlAct.getRequestPayload()),
              () ->
                  assertEquals(
                      responseProvincialPatientSearchCriteria,
                      controlAct.getProvincialPatientSearchCriteria(),
                      "patient search criteria"));
        },
        () -> {
          var patientProfile = response.getProvincialResponsePayload();
          assertAll(
              "patient profile",
              () -> assertEquals(responsePatientProfile, patientProfile, "patient profile"));
        },
        () -> {
          var queryAck = response.getQueryAcknowledgement();
          assertAll(
              "query acknowledgment",
              () -> assertEquals(responseQueryAck, queryAck, "query acknowledgment"));
        },
        () -> {
          var detectedIssues = response.getDetectedIssues();
          assertAll(
              "detected issue",
              () ->
                  assertEquals(List.of(responseDetectedEvent), detectedIssues, "detected events"));
        });
  }
}
