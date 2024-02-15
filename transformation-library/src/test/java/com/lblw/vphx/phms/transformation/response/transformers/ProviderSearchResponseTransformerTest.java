package com.lblw.vphx.phms.transformation.response.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.providersearch.ProviderSearchResponseXPathTransformer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xml.sax.SAXParseException;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CommonResponseXPathTransformerHelper.class})
class ProviderSearchResponseTransformerTest {
  private final ExpectedTestProviderResponseFactory expectedTestProviderResponseFactory =
      new ExpectedTestProviderResponseFactory();
  private CodeableConceptService codeableConceptService;
  private ProviderSearchResponseXPathTransformer providerSearchResponseTransformer;

  @BeforeEach
  public void beforeEachTest() {
    codeableConceptService = mock(CodeableConceptRefDataService.class);
    providerSearchResponseTransformer =
        new ProviderSearchResponseXPathTransformer(
            codeableConceptService, new CommonResponseXPathTransformerHelper());
  }

  @Test
  void whenSuccessResponse_ThenValidateTransformationResponse() throws Exception {
    when(codeableConceptService.findSystemRoleCodingByProvincialRoleCode(any(), any()))
        .thenReturn(Optional.of(Coding.builder().build()));
    when(codeableConceptService.findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(
            any(), any()))
        .thenReturn(Optional.of(Coding.builder().build()));
    testTemplate("providerprofile/dsq.response/success.xml", "success", null);
  }

  @Test
  void whenEmptyResponse_ThenValidateException() throws Exception {
    StepVerifier.create(
            providerSearchResponseTransformer
                .transform("")
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH).readOnly()))
        .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException);
  }

  @Test
  void whenInvalidXMLResponse_ThenValidateException() throws Exception {
    StepVerifier.create(
            providerSearchResponseTransformer
                .transform("test:test")
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH).readOnly()))
        .verifyErrorMatches(throwable -> throwable instanceof SAXParseException);
  }

  private void testTemplate(String hl7XmlResponse, String useCaseName, String detectedIssueText)
      throws Exception {
    String hl7_response = loadHL7ResponseForTestingFrom(hl7XmlResponse);

    // TEST

    final ProvincialProviderSearchResponse expectedProvincialProviderSearchResponse =
        expectedTestProviderResponseFactory.buildExpectedProvincialProviderSearchResponse(
            useCaseName);

    StepVerifier.create(
            providerSearchResponseTransformer
                .transform(hl7_response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH).readOnly()))
        .assertNext(
            actualResponse -> {
              ProvincialProviderSearchResponse observedProvincialProviderSearchResponse =
                  (ProvincialProviderSearchResponse) actualResponse;
              assertThat(observedProvincialProviderSearchResponse).isNotNull();
              performAssertionsOnHeader(
                  observedProvincialProviderSearchResponse,
                  expectedProvincialProviderSearchResponse,
                  detectedIssueText);
              performAssertionOnTransmissionWrapper(
                  observedProvincialProviderSearchResponse,
                  expectedProvincialProviderSearchResponse,
                  detectedIssueText);
              performAssertionOnControlAct(
                  observedProvincialProviderSearchResponse,
                  expectedProvincialProviderSearchResponse,
                  detectedIssueText);
            })
        .verifyComplete();
    /// @TOOD: add more location assertions
  }

  private String loadHL7ResponseForTestingFrom(String fileName) throws IOException {
    Path path = Paths.get("src/test/resources", fileName);
    return Files.readString(path);
  }

  private void performAssertionsOnHeader(
      ProvincialProviderSearchResponse observedProvincialProviderSearchResponse,
      ProvincialProviderSearchResponse expectedProvincialProviderSearchResponse,
      String detectedIssueText) {

    assertThat(observedProvincialProviderSearchResponse.getResponseHeader())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialProviderSearchResponse.getResponseHeader());
  }

  private void performAssertionOnTransmissionWrapper(
      ProvincialProviderSearchResponse observedProvincialProviderSearchResponse,
      ProvincialProviderSearchResponse expectedProvincialProviderSearchResponse,
      String detectedIssueText) {
    assertThat(observedProvincialProviderSearchResponse.getResponseBodyTransmissionWrapper())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialProviderSearchResponse.getResponseBodyTransmissionWrapper());
  }

  private void performAssertionOnControlAct(
      ProvincialProviderSearchResponse observedProvincialProviderSearchResponse,
      ProvincialProviderSearchResponse expectedProvincialProviderSearchResponse,
      String detectedIssueText) {
    assertThat(observedProvincialProviderSearchResponse.getResponseControlAct())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialProviderSearchResponse.getResponseControlAct());
  }
}
