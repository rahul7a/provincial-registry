package com.lblw.vphx.phms.transformation.response.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseTransformerHelper;
import com.lblw.vphx.phms.transformation.utils.TestUtils;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

/** This test class tests transformation logic for Location Details */
@ExtendWith(MockitoExtension.class)
public class LocationDetailsResponseTransformerTest {

  @Mock CodeableConceptService codeableConceptService;
  private LocationDetailsResponseTransformer locationDetailsResponseTransformer;
  private String requestCorrelationId;
  private String provincialSystemRawRequest;
  private String provincialSystemRawResponse;
  private String provincialSystemRawRequestTime;
  private String provincialSystemRawResponseTime;

  /**
   * Creates an instance of {@link ProvincialLocationDetailsResponse} from json success response
   * expectations.
   *
   * @param pathToJsonFile Path to the expectation json file. For instance,
   *     "patientconsent/expectation/success.json"
   * @param requestCorrelationId request correlation id
   * @param provincialSystemRawRequest provincial System Raw Request
   * @param provincialSystemRawResponse provincial System Raw Response
   * @param provincialSystemRawRequestTime provincial System Raw Request time
   * @param provincialSystemRawResponseTime provincial System Raw Response time
   * @return {@link ProvincialLocationDetailsResponse} expected instance of
   *     ProvincialLocationDetailsResponse
   */
  public static ProvincialLocationDetailsResponse getExpectedLocationDetails(
      @NonNull String pathToJsonFile,
      String requestCorrelationId,
      Province province,
      String provincialSystemRawRequest,
      String provincialSystemRawResponse,
      String provincialSystemRawRequestTime,
      String provincialSystemRawResponseTime) {

    ProvincialLocationDetailsResponse provincialLocationDetailsResponse = null;
    try {
      provincialLocationDetailsResponse =
          (ProvincialLocationDetailsResponse)
              TestUtils.loadJsonToClass(pathToJsonFile, ProvincialLocationDetailsResponse.class);
    } catch (IOException e) {
      Assertions.assertEquals(true, e.getMessage().isEmpty());
    }

    // Populating the @JsonIgnore

    ProvincialResponseAcknowledgement provincialResponseAcknowledgement =
        new ProvincialResponseAcknowledgement();
    provincialResponseAcknowledgement.setAuditEvent(
        AuditEvent.builder()
            .provincialRequestControl(
                ProvincialRequestControl.builder()
                    .province(province)
                    .requestId(requestCorrelationId)
                    .build())
            .rawRequest(provincialSystemRawRequest)
            .requestTimestamp(provincialSystemRawRequestTime)
            .rawResponse(provincialSystemRawResponse)
            .responseTimestamp(provincialSystemRawResponseTime)
            .build());

    if (provincialLocationDetailsResponse.getProvincialResponsePayload() != null) {
      provincialLocationDetailsResponse
          .getProvincialResponsePayload()
          .setProvincialResponseAcknowledgement(provincialResponseAcknowledgement);
    } else {
      provincialLocationDetailsResponse.setProvincialResponsePayload(
          ProvincialLocationDetails.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  ProvincialRequestControl.builder()
                                      .requestId(requestCorrelationId)
                                      .province(province)
                                      .build())
                              .build())
                      .build())
              .build());
    }
    return provincialLocationDetailsResponse;
  }

  private static Stream<Arguments> provideTestArguments() {
    return Stream.of(
        Arguments.of(
            "locationdetails/expectation/success.json",
            "eventCorrelationId",
            "locationdetails/dsq.response/success.xml"),
        Arguments.of(
            "locationdetails/expectation/ramq_permitnumber.json",
            "eventCorrelationId",
            "locationdetails/dsq.response/ramq_permitnumber.xml"),
        Arguments.of(
            "locationdetails/expectation/ramq_pharmacysuccess.json",
            "eventCorrelationId",
            "locationdetails/dsq.response/ramq_pharmacysuccess.xml"),
        Arguments.of(
            "locationdetails/expectation/ramq_pharmacysuccess.json",
            "eventCorrelationId",
            "locationdetails/dsq.response/ramq_pharmacysuccess.xml"));
  }

  @BeforeEach
  public void beforeEach() {
    locationDetailsResponseTransformer =
        new LocationDetailsResponseTransformer(
            new CommonResponseTransformerHelper(), codeableConceptService);
  }

  @ParameterizedTest
  @MethodSource("provideTestArguments")
  void whenSuccessXmlResponse_ThenSuccessResponseIsReceived(
      String pathToJsonFile, String requestCorrelationId, String hl7XmlResponse) throws Exception {
    when(codeableConceptService.findSystemLocationTypeCodingByProvincialCode(any(), any()))
        .then((answer) -> Optional.of(Coding.builder().code(answer.getArgument(1)).build()));
    final ProvincialLocationDetailsResponse expectedProvincialLocationDetailsResponse =
        getExpectedLocationDetails(
            pathToJsonFile,
            requestCorrelationId,
            Province.QC,
            provincialSystemRawRequest,
            provincialSystemRawResponse,
            provincialSystemRawRequestTime,
            provincialSystemRawResponseTime);

    testTemplate(hl7XmlResponse, null, expectedProvincialLocationDetailsResponse);
  }

  @Test
  void
      whenSuccessXmlResponseWithNullFlavorLocationTypes_ThenSuccessResponseIsReceivedWithNullFlavorMappings()
          throws Exception {
    when(codeableConceptService.findSystemLocationTypeCodingByProvincialCode(any(), any()))
        .then(
            (answer) ->
                Optional.of(
                    Coding.builder()
                        .code("UNK")
                        .province(Province.ALL)
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
                        .build()));
    String pathToJsonFile = "locationdetails/expectation/locationTypeNullFlavorsSuccess.json";
    requestCorrelationId = "eventCorrelationId";
    final ProvincialLocationDetailsResponse expectedProvincialLocationDetailsResponse =
        getExpectedLocationDetails(
            pathToJsonFile,
            requestCorrelationId,
            Province.QC,
            provincialSystemRawRequest,
            provincialSystemRawResponse,
            provincialSystemRawRequestTime,
            provincialSystemRawResponseTime);

    testTemplate(
        "locationdetails/dsq.response/locationTypeNullFlavorsSuccess.xml",
        null,
        expectedProvincialLocationDetailsResponse);
  }

  @Test
  void whenLocationDetailsForPermitIsNotFound_ThenFailureResponseIsReceived() throws Exception {
    String pathToJsonFile = "locationdetails/expectation/permitnumberfailure.json";
    requestCorrelationId = "eventCorrelationId";
    final ProvincialLocationDetailsResponse expectedProvincialLocationDetailsResponse =
        getExpectedLocationDetails(
            pathToJsonFile,
            requestCorrelationId,
            Province.QC,
            provincialSystemRawRequest,
            provincialSystemRawResponse,
            provincialSystemRawRequestTime,
            provincialSystemRawResponseTime);

    testTemplate(
        "locationdetails/dsq.response/permitnumberfailure.xml",
        null,
        expectedProvincialLocationDetailsResponse);
  }

  /**
   * This is a simply a template for the test function for response.
   *
   * @param hl7XmlResponse {@link ProvincialLocationDetailsResponse}
   * @param detectedIssueText Detected issue text
   * @param expectedProvincialLocationDetailsResponse {@link ProvincialLocationDetailsResponse}
   * @throws Exception thrown when testing.
   */
  private void testTemplate(
      String hl7XmlResponse,
      String detectedIssueText,
      final ProvincialLocationDetailsResponse expectedProvincialLocationDetailsResponse)
      throws Exception {
    String hl7_response = TestUtils.loadXmlFileContentsAsString(hl7XmlResponse);

    // CUT

    StepVerifier.create(
            locationDetailsResponseTransformer
                .transform(hl7_response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.LOCATION_DETAILS).readOnly()))
        .assertNext(
            actualResponse -> {
              ProvincialLocationDetailsResponse observedProvincialLocationDetailsResponse =
                  (ProvincialLocationDetailsResponse) actualResponse;
              assertThat(observedProvincialLocationDetailsResponse).isNotNull();
              performAssertions(
                  observedProvincialLocationDetailsResponse,
                  expectedProvincialLocationDetailsResponse);
            })
        .verifyComplete();
    // TEST

  }

  /**
   * Performs assertion on the observed and the expected transformation objects
   *
   * @param observedProvincialLocationDetailsResponse {@link ProvincialLocationDetailsResponse}
   *     received from the code under test
   * @param expectedProvincialLocationDetailsResponse {@link ProvincialLocationDetailsResponse}
   *     expected
   */
  private void performAssertions(
      ProvincialLocationDetailsResponse observedProvincialLocationDetailsResponse,
      ProvincialLocationDetailsResponse expectedProvincialLocationDetailsResponse) {

    assertThat(observedProvincialLocationDetailsResponse.getResponseHeader())
        .usingRecursiveComparison()
        .isEqualTo(expectedProvincialLocationDetailsResponse.getResponseHeader());

    assertOnResponseBodyTransmissionWrapper(
        observedProvincialLocationDetailsResponse, expectedProvincialLocationDetailsResponse);

    assertThat(observedProvincialLocationDetailsResponse.getResponseControlAct())
        .usingRecursiveComparison()
        .isEqualTo(expectedProvincialLocationDetailsResponse.getResponseControlAct());

    if (observedProvincialLocationDetailsResponse.getProvincialResponsePayload() != null) {
      assertThat(observedProvincialLocationDetailsResponse.getProvincialResponsePayload())
          .usingRecursiveComparison()
          .isEqualTo(expectedProvincialLocationDetailsResponse.getProvincialResponsePayload());
    }
  }

  /**
   * Performs assertion on ResponseBodyTransmissionWrapper after extracting out running List of
   * {@link AcknowledgementDetails} as the recursive assertion function fails to assert on contents
   * of the lists, which is of relevance for this tests.
   *
   * @param observedProvincialLocationDetailsResponse Observed {@link
   *     ProvincialLocationDetailsResponse}
   * @param expectedProvincialLocationDetailsResponse Expected {@link
   *     ProvincialLocationDetailsResponse}
   */
  private void assertOnResponseBodyTransmissionWrapper(
      ProvincialLocationDetailsResponse observedProvincialLocationDetailsResponse,
      ProvincialLocationDetailsResponse expectedProvincialLocationDetailsResponse) {
    final List<AcknowledgementDetails> observedAcknowledgementDetails =
        observedProvincialLocationDetailsResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    observedProvincialLocationDetailsResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    final List<AcknowledgementDetails> expectedAcknowledgementDetails =
        expectedProvincialLocationDetailsResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    expectedProvincialLocationDetailsResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    if (observedAcknowledgementDetails != null && expectedAcknowledgementDetails != null) {
      assertThat(observedAcknowledgementDetails)
          .containsExactlyInAnyOrderElementsOf(expectedAcknowledgementDetails);
    }

    assertThat(observedProvincialLocationDetailsResponse.getResponseBodyTransmissionWrapper())
        .usingRecursiveComparison()
        .isEqualTo(expectedProvincialLocationDetailsResponse.getResponseBodyTransmissionWrapper());
  }
}
