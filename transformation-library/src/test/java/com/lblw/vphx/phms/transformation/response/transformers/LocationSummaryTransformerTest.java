package com.lblw.vphx.phms.transformation.response.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
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
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.locationsummary.LocationSummaryResponseXPathTransformer;
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
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

/** This test class tests transformation logic for Location Summary. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CommonResponseTransformerHelper.class})
public class LocationSummaryTransformerTest {
  private CodeableConceptService codeableConceptService;
  private LocationSummaryResponseXPathTransformer locationSummaryResponseTransformer;
  private String requestCorrelationId;
  private String provincialSystemRawRequest;
  private String provincialSystemRawResponse;
  private String provincialSystemRawRequestTime;
  private String provincialSystemRawResponseTime;

  /**
   * Creates an instance of {@link ProvincialLocationSearchResponse} from json success response
   * expectations.
   *
   * @param pathToJsonFile Path to the expectation json file. For instance,
   *     "patientconsent/expectation/success.json"
   * @param requestCorrelationId request correlation id
   * @param provincialSystemRawRequest provincial System Raw Request
   * @param provincialSystemRawResponse provincial System Raw Response
   * @param provincialSystemRawRequestTime provincial System Raw Request time
   * @param provincialSystemRawResponseTime provincial System Raw Response time
   * @return {@link ProvincialPatientConsent} expected instance of ProvincialPatientConsent
   */
  public static ProvincialLocationSearchResponse getExpectedLocationSummary(
      @NonNull String pathToJsonFile,
      String requestCorrelationId,
      Province province,
      String provincialSystemRawRequest,
      String provincialSystemRawResponse,
      String provincialSystemRawRequestTime,
      String provincialSystemRawResponseTime) {

    ProvincialLocationSearchResponse provincialLocationSearchResponse = null;
    try {
      provincialLocationSearchResponse =
          (ProvincialLocationSearchResponse)
              TestUtils.loadJsonToClass(pathToJsonFile, ProvincialLocationSearchResponse.class);
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

    provincialLocationSearchResponse
        .getProvincialResponsePayload()
        .setProvincialResponseAcknowledgement(provincialResponseAcknowledgement);

    return provincialLocationSearchResponse;
  }

  private static Stream<Arguments> provideTestArguments() {
    return Stream.of(
        Arguments.of(
            "locationsummary/expectation/success.json",
            "eventCorrelationId",
            "locationsummary/dsq.response/success.xml"),
        Arguments.of(
            "locationsummary/expectation/pharmacyBillingSuccess.json",
            "eventCorrelationId",
            "locationsummary/dsq.response/pharmacyBillingSuccess.xml"),
        Arguments.of(
            "locationsummary/expectation/healthInsurancePermitNumberSuccess.json",
            "eventCorrelationId",
            "locationsummary/dsq.response/healthInsurancePermitNumberSuccess.xml"),
        Arguments.of(
            "locationsummary/expectation/permitNumberSuccess.json",
            "eventCorrelationId",
            "locationsummary/dsq.response/permitNumberSuccess.xml"),
        Arguments.of(
            "locationsummary/expectation/locationSummaryNotFound.json",
            "eventCorrelationId",
            "locationsummary/dsq.response/locationSummaryNotFound.xml"),
        Arguments.of(
            "locationsummary/expectation/permitNumberFailure.json",
            "11111111",
            "locationsummary/dsq.response/permitNumberFailure.xml"),
        Arguments.of(
            "locationsummary/expectation/pharmacyBillingNumberFailure.json",
            "11111111",
            "locationsummary/dsq.response/pharmacyBillingNumberFailure.xml"),
        Arguments.of(
            "locationsummary/expectation/controlActNullwhenTechnicalFailure.json",
            null,
            "locationsummary/dsq.response/technicalFailure.xml"),
        Arguments.of(
            "locationsummary/expectation/healthInsurancePermitNumberFailure.json",
            "11111111",
            "locationsummary/dsq.response/healthInsurancePermitNumberFailure.xml"));
  }

  @BeforeEach
  void beforeEach() {
    codeableConceptService = mock(CodeableConceptRefDataService.class);
    locationSummaryResponseTransformer =
        new LocationSummaryResponseXPathTransformer(codeableConceptService);
  }

  @ParameterizedTest
  @MethodSource("provideTestArguments")
  void whenSuccessXmlResponse_ThenSuccessResponseIsReceived(
      String pathToJsonFile, String requestCorrelationId, String hl7XmlResponse) throws Exception {
    final ProvincialLocationSearchResponse expectedProvincialLocationSearchResponse =
        getExpectedLocationSummary(
            pathToJsonFile,
            requestCorrelationId,
            Province.QC,
            provincialSystemRawRequest,
            provincialSystemRawResponse,
            provincialSystemRawRequestTime,
            provincialSystemRawResponseTime);
    when(codeableConceptService.findSystemLocationTypeCodingByProvincialCode(any(), any()))
        .thenReturn(Optional.of(Coding.builder().code("LAB").build()));
    testTemplate(hl7XmlResponse, null, expectedProvincialLocationSearchResponse);
  }

  private void testTemplate(
      String hl7XmlResponse,
      String detectedIssueText,
      final ProvincialLocationSearchResponse expectedProvincialLocationSearchResponse)
      throws Exception {
    String hl7_response = TestUtils.loadXmlFileContentsAsString(hl7XmlResponse);

    StepVerifier.create(
            locationSummaryResponseTransformer
                .transform(hl7_response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.LOCATION_SEARCH).readOnly()))
        .assertNext(
            actualResponse -> {
              ProvincialLocationSearchResponse observedProvincialLocationSearchResponse =
                  (ProvincialLocationSearchResponse) actualResponse;
              assertThat(observedProvincialLocationSearchResponse).isNotNull();
              performAssertions(
                  observedProvincialLocationSearchResponse,
                  expectedProvincialLocationSearchResponse);
            })
        .verifyComplete();
  }

  @Test
  void
      whenSuccessXmlResponseWithNullFlavorLocationTypes_ThenSuccessResponseIsReceivedWithNullFlavorMappings()
          throws Exception {
    String pathToJsonFile = "locationsummary/expectation/locationTypeNullFlavorsSuccess.json";
    requestCorrelationId = "eventCorrelationId";

    final ProvincialLocationSearchResponse expectedProvincialLocationSearchResponse =
        getExpectedLocationSummary(
            pathToJsonFile,
            requestCorrelationId,
            Province.QC,
            provincialSystemRawRequest,
            provincialSystemRawResponse,
            provincialSystemRawRequestTime,
            provincialSystemRawResponseTime);
    when(codeableConceptService.findSystemLocationTypeCodingByProvincialCode(any(), any()))
        .thenReturn(
            Optional.of(
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
                          LocalizedText.builder().language(LanguageCode.FRA).text("Inconnu").build()
                        })
                    .build()));
    testTemplate(
        "locationsummary/dsq.response/locationTypeNullFlavorsSuccess.xml",
        null,
        expectedProvincialLocationSearchResponse);
  }

  /**
   * Performs assertion on the observed and the expected transformation objects
   *
   * @param observedProvincialLocationSearchResponse {@link ProvincialLocationSearchResponse}
   *     received from the code under test
   * @param expectedProvincialLocationSearchResponse {@link ProvincialLocationSearchResponse}
   *     expected
   */
  private void performAssertions(
      ProvincialLocationSearchResponse observedProvincialLocationSearchResponse,
      ProvincialLocationSearchResponse expectedProvincialLocationSearchResponse) {

    assertThat(observedProvincialLocationSearchResponse.getResponseHeader())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialLocationSearchResponse.getResponseHeader());

    assertOnResponseBodyTransmissionWrapper(
        observedProvincialLocationSearchResponse, expectedProvincialLocationSearchResponse);

    assertThat(observedProvincialLocationSearchResponse.getProvincialResponsePayload())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialLocationSearchResponse.getProvincialResponsePayload());
  }

  /**
   * Performs assertion on ResponseBodyTransmissionWrapper after extracting out running List of
   * {@link AcknowledgementDetails} as the recursive assertion function fails to assert on contents
   * of the lists, which is of relevance for this tests.
   *
   * @param observedProvincialLocationSearchResponse Observed {@link
   *     ProvincialLocationSearchResponse}
   * @param expectedProvincialLocationSearchResponse Expected {@link
   *     ProvincialLocationSearchResponse}
   */
  private void assertOnResponseBodyTransmissionWrapper(
      ProvincialLocationSearchResponse observedProvincialLocationSearchResponse,
      ProvincialLocationSearchResponse expectedProvincialLocationSearchResponse) {
    final List<AcknowledgementDetails> observedAcknowledgementDetails =
        observedProvincialLocationSearchResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    observedProvincialLocationSearchResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    final List<AcknowledgementDetails> expectedAcknowledgementDetails =
        expectedProvincialLocationSearchResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    expectedProvincialLocationSearchResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    if (observedAcknowledgementDetails != null && expectedAcknowledgementDetails != null) {
      assertThat(observedAcknowledgementDetails)
          .containsExactlyInAnyOrderElementsOf(expectedAcknowledgementDetails);
    }

    assertThat(observedProvincialLocationSearchResponse.getResponseBodyTransmissionWrapper())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialLocationSearchResponse.getResponseBodyTransmissionWrapper());
  }
}
