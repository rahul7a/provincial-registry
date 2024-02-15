package com.lblw.vphx.phms.transformation.response.transformers.locationsummary.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.BindOne;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.Telecom;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.*;
import com.lblw.vphx.phms.domain.location.LocationAddress;
import com.lblw.vphx.phms.domain.location.ProvincialLocation;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseHeaderMapper;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocationSummaryResponseMapperTest {
  private LocationSummaryResponseMapper locationSummaryResponseMapper;
  private CodeableConceptService codeableConceptService;

  @BeforeEach
  void buildLocationSummaryResponseMapper() {
    locationSummaryResponseMapper = new LocationSummaryResponseMapper(codeableConceptService);
  }

  @Test
  void whenBinding_thenSetResponseHeader() {
    BindOne<ResponseHeader> bindOne =
        (supplier) ->
            ResponseHeaderMapper.builder()
                .transactionId("transactionId")
                .trackingId("trackingId")
                .sessionId("sessionId")
                .build();
    locationSummaryResponseMapper.bindResponseHeader(bindOne);

    assertEquals("trackingId", locationSummaryResponseMapper.getResponseHeader().getTrackingId());
    assertEquals(
        "transactionId", locationSummaryResponseMapper.getResponseHeader().getTransactionId());
    assertEquals("sessionId", locationSummaryResponseMapper.getResponseHeader().getSessionId());
  }

  @Test
  void givenNullResponseHeaderBinder_whenBinding_thenNotSetResponseHeader() {
    BindOne<ResponseHeader> bindOne = null;
    locationSummaryResponseMapper.bindResponseHeader(bindOne);

    assertNull(locationSummaryResponseMapper.getResponseHeader());
  }

  @Test
  void whenBinding_thenSetResponseBodyTransmissionWrapper() {

    BindOne<ResponseBodyTransmissionWrapper> bindOne =
        (supplier) ->
            ResponseBodyTransmissionWrapper.builder()
                .transmissionUniqueIdentifier("transmissionUniqueIdentifier")
                .acknowledgeTypeCode("AE")
                .acknowledgementDetails(
                    Arrays.asList(
                        AcknowledgementDetails.builder()
                            .code("E")
                            .text("text")
                            .typeCode("typeCode")
                            .location("location")
                            .build(),
                        AcknowledgementDetails.builder()
                            .code("E")
                            .text("acknowledgementText")
                            .typeCode("typeCode")
                            .location("location")
                            .build()))
                .build();

    locationSummaryResponseMapper.bindResponseBodyTransmissionWrapper(bindOne);

    assertEquals(
        "transmissionUniqueIdentifier",
        locationSummaryResponseMapper
            .getResponseBodyTransmissionWrapper()
            .getTransmissionUniqueIdentifier());
    assertEquals(
        "AE",
        locationSummaryResponseMapper
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgeTypeCode());

    assertEquals(
        Arrays.asList(
            AcknowledgementDetails.builder()
                .code("E")
                .text("text")
                .typeCode("typeCode")
                .location("location")
                .build(),
            AcknowledgementDetails.builder()
                .code("E")
                .text("acknowledgementText")
                .typeCode("typeCode")
                .location("location")
                .build()),
        locationSummaryResponseMapper
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails());
  }

  @Test
  void whenBinding_thenNotSetResponseBodyTransmissionWrapper() {
    BindOne<ResponseBodyTransmissionWrapper> bindOne = null;
    locationSummaryResponseMapper.bindResponseBodyTransmissionWrapper(bindOne);

    assertNull(locationSummaryResponseMapper.getResponseBodyTransmissionWrapper());
  }

  @Test
  void whenBinding_thenSetQueryAck() {

    BindOne<QueryAcknowledgement> bindOne =
        (supplier) ->
            QueryAcknowledgement.builder()
                .queryResponseCode("OK")
                .resultTotalQuantity("1")
                .resultCurrentQuantity("1")
                .resultRemainingQuantity("0")
                .build();
    locationSummaryResponseMapper.bindQueryAcknowledgementMapper(bindOne);

    assertEquals(
        "OK", locationSummaryResponseMapper.getQueryAcknowledgement().getQueryResponseCode());
    assertEquals(
        "1", locationSummaryResponseMapper.getQueryAcknowledgement().getResultTotalQuantity());
    assertEquals(
        "1", locationSummaryResponseMapper.getQueryAcknowledgement().getResultCurrentQuantity());
    assertEquals(
        "0", locationSummaryResponseMapper.getQueryAcknowledgement().getResultRemainingQuantity());
  }

  @Test
  void givenNullQueryAckConsentBinder_thenNotSetQueryAck() {
    BindOne<QueryAcknowledgement> bindOne = null;
    locationSummaryResponseMapper.bindQueryAcknowledgementMapper(bindOne);
    assertNull(locationSummaryResponseMapper.getQueryAcknowledgement());
  }

  @Test
  void whenBinding_thenSetDetectedIssues() {
    BindMany<DetectedIssue> bindMany =
        (supplier) -> {
          DetectedIssue detectedIssue1 =
              DetectedIssue.builder()
                  .eventCode("VALIDAT")
                  .eventText("Vous devez fournir une seule date de référence")
                  .build();

          DetectedIssue detectedIssue2 =
              DetectedIssue.builder()
                  .eventCode("MISSMAND")
                  .eventText("Paramètre obligatoire manquant")
                  .build();

          return Stream.of(detectedIssue1, detectedIssue2);
        };
    locationSummaryResponseMapper.bindDetectedIssueMapper(bindMany);

    Assertions.assertEquals(
        Arrays.asList(
            DetectedIssue.builder()
                .eventCode("VALIDAT")
                .eventText("Vous devez fournir une seule date de référence")
                .build(),
            DetectedIssue.builder()
                .eventCode("MISSMAND")
                .eventText("Paramètre obligatoire manquant")
                .build()),
        locationSummaryResponseMapper.getDetectedIssues());
  }

  @Test
  void givenNullDetectedIssues_thenNotSetDetectedIssues() {
    BindMany<DetectedIssue> bindMany = null;
    locationSummaryResponseMapper.bindDetectedIssueMapper(bindMany);
    assertNull(locationSummaryResponseMapper.getDetectedIssues());
  }

  @Test
  void whenBinding_thenSetProvincialResponsePayload() {

    String requestId = "requestId";
    BindOne<ProvincialLocationSummaries> bindOne =
        (supplier) -> {
          return ProvincialLocationSummaries.builder()
              .provincialLocations(
                  Arrays.asList(
                      ProvincialLocation.builder()
                          .identifier(
                              SystemIdentifier.builder()
                                  .type(SystemIdentifier.IDENTIFIER_TYPE.LOCATION)
                                  .value("1000038958")
                                  .assigner(HL7Constants.QC)
                                  .system(HL7Constants.NIU_O)
                                  .build())
                          .name("LABORATOIRE MEDICALE DE LEVIS")
                          .locationType(
                              Coding.builder()
                                  .province(Province.ALL)
                                  .code("UNK")
                                  .system("SYSTEM")
                                  .display(
                                      new LocalizedText[] {
                                        LocalizedText.builder()
                                            .language(LanguageCode.ENG)
                                            .text("Unknown")
                                            .build(),
                                        LocalizedText.builder()
                                            .language(LanguageCode.FRA)
                                            .text("Inconnu")
                                            .build()
                                      })
                                  .build())
                          .address(
                              LocationAddress.builder()
                                  .streetAddressLine1("100-4975 BOUL DE LA RIVE-SUD S")
                                  .streetAddressLine2("LEVIS, QC")
                                  .city("LEVIS")
                                  .postalCode("G6V4Z5")
                                  .country("CA")
                                  .build())
                          .region("12")
                          .telecom(Telecom.builder().build())
                          .build(),
                      ProvincialLocation.builder()
                          .identifier(
                              SystemIdentifier.builder()
                                  .type(SystemIdentifier.IDENTIFIER_TYPE.LOCATION)
                                  .value("6000001969")
                                  .assigner(HL7Constants.QC)
                                  .system(HL7Constants.NIU_O)
                                  .build())
                          .name("Eurofins Environex")
                          .locationType(
                              Coding.builder()
                                  .province(Province.ALL)
                                  .code("HOSP")
                                  .system("SYSTEM")
                                  .display(
                                      new LocalizedText[] {
                                        LocalizedText.builder()
                                            .language(LanguageCode.ENG)
                                            .text("Hospital, institutional pharmacy, laboratory")
                                            .build(),
                                        LocalizedText.builder()
                                            .language(LanguageCode.FRA)
                                            .text(
                                                "Centre hospitalier, pharmacie d’établissement, laboratoire (CH)")
                                            .build()
                                      })
                                  .build())
                          .address(
                              LocationAddress.builder()
                                  .streetAddressLine1("2325 Boul Fernand-Lafontaine")
                                  .streetAddressLine2("Longueuil, QC")
                                  .city("Longueuil")
                                  .postalCode("J4N1N7")
                                  .country("CA")
                                  .build())
                          .region("16")
                          .telecom(Telecom.builder().build())
                          .build()))
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  ProvincialRequestControl.builder()
                                      .province(Province.QC)
                                      .requestId(requestId)
                                      .build())
                              .build())
                      .build())
              .build();
        };
    BindOne<Object> controlAct = (supplier) -> new Object();
    locationSummaryResponseMapper.bindProvincialResponsePayload(bindOne, controlAct, "requestId");

    assertEquals(
        Arrays.asList(
            ProvincialLocation.builder()
                .identifier(
                    SystemIdentifier.builder()
                        .type(SystemIdentifier.IDENTIFIER_TYPE.LOCATION)
                        .value("1000038958")
                        .assigner(HL7Constants.QC)
                        .system(HL7Constants.NIU_O)
                        .build())
                .name("LABORATOIRE MEDICALE DE LEVIS")
                .locationType(
                    Coding.builder()
                        .province(Province.ALL)
                        .code("UNK")
                        .system("SYSTEM")
                        .display(
                            new LocalizedText[] {
                              LocalizedText.builder()
                                  .language(LanguageCode.ENG)
                                  .text("Unknown")
                                  .build(),
                              LocalizedText.builder()
                                  .language(LanguageCode.FRA)
                                  .text("Inconnu")
                                  .build()
                            })
                        .build())
                .address(
                    LocationAddress.builder()
                        .streetAddressLine1("100-4975 BOUL DE LA RIVE-SUD S")
                        .streetAddressLine2("LEVIS, QC")
                        .city("LEVIS")
                        .postalCode("G6V4Z5")
                        .country("CA")
                        .build())
                .region("12")
                .telecom(Telecom.builder().build())
                .build(),
            ProvincialLocation.builder()
                .identifier(
                    SystemIdentifier.builder()
                        .type(SystemIdentifier.IDENTIFIER_TYPE.LOCATION)
                        .value("6000001969")
                        .assigner(HL7Constants.QC)
                        .system(HL7Constants.NIU_O)
                        .build())
                .name("Eurofins Environex")
                .locationType(
                    Coding.builder()
                        .province(Province.ALL)
                        .code("HOSP")
                        .system("SYSTEM")
                        .display(
                            new LocalizedText[] {
                              LocalizedText.builder()
                                  .language(LanguageCode.ENG)
                                  .text("Hospital, institutional pharmacy, laboratory")
                                  .build(),
                              LocalizedText.builder()
                                  .language(LanguageCode.FRA)
                                  .text(
                                      "Centre hospitalier, pharmacie d’établissement, laboratoire (CH)")
                                  .build()
                            })
                        .build())
                .address(
                    LocationAddress.builder()
                        .streetAddressLine1("2325 Boul Fernand-Lafontaine")
                        .streetAddressLine2("Longueuil, QC")
                        .city("Longueuil")
                        .postalCode("J4N1N7")
                        .country("CA")
                        .build())
                .region("16")
                .telecom(Telecom.builder().build())
                .build()),
        locationSummaryResponseMapper.getProvincialResponsePayload().getProvincialLocations());

    assertEquals(
        ProvincialResponseAcknowledgement.builder()
            .auditEvent(
                AuditEvent.builder()
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .requestId(requestId)
                            .build())
                    .build())
            .build(),
        locationSummaryResponseMapper
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement());
  }

  @Test
  void givenNullLocationSummaryBinder_whenBinding_thenSetProvincialResponsePayload() {

    String requestId = "requestID";
    BindOne<ProvincialLocationSummaries> bindOne = null;

    locationSummaryResponseMapper.bindProvincialResponsePayload(bindOne, null, "requestID");
    assertEquals(
        ProvincialResponseAcknowledgement.builder()
            .auditEvent(
                AuditEvent.builder()
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .requestId(requestId)
                            .build())
                    .build())
            .build(),
        locationSummaryResponseMapper
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement());
  }
}
