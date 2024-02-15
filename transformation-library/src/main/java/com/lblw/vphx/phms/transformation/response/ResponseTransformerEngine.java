package com.lblw.vphx.phms.transformation.response;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.response.transformers.DefaultResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.LocationDetailsResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.locationsummary.LocationSummaryResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientconsent.PatientConsentResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientsearch.PatientSearchResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.providersearch.ProviderSearchResponseXPathTransformer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** This class helps to select the Response Transformer depending on the Transaction Type */
@Component
@Slf4j
public class ResponseTransformerEngine<
    R extends Response<? extends ResponseControlAct, ? extends ProvincialResponse>> {

  private final PatientSearchResponseXPathTransformer patientSearchResponseXPathTransformer;
  private final ProviderSearchResponseXPathTransformer providerSearchResponseTransformer;
  private final PatientConsentResponseXPathTransformer patientConsentResponseXPathTransformer;
  private final LocationDetailsResponseTransformer locationDetailsResponseTransformer;
  private final DefaultResponseTransformer defaultResponseTransformer;
  private final CodeableConceptService codeableConceptService;

  /**
   * This is a public constructor
   *
   * @param patientSearchResponseXPathTransformer {@link PatientSearchResponseXPathTransformer}
   * @param providerSearchResponseTransformer {@link ProviderSearchResponseXPathTransformer}
   * @param patientConsentResponseXPathTransformer {@link PatientConsentResponseXPathTransformer}
   * @param locationDetailsResponseTransformer {@link LocationDetailsResponseTransformer}
   * @param defaultResponseTransformer {@link DefaultResponseTransformer}
   * @param codeableConceptService {@link CodeableConceptService}
   */
  public ResponseTransformerEngine(
      PatientSearchResponseXPathTransformer patientSearchResponseXPathTransformer,
      ProviderSearchResponseXPathTransformer providerSearchResponseTransformer,
      PatientConsentResponseXPathTransformer patientConsentResponseXPathTransformer,
      LocationDetailsResponseTransformer locationDetailsResponseTransformer,
      DefaultResponseTransformer defaultResponseTransformer,
      CodeableConceptService codeableConceptService) {
    this.patientSearchResponseXPathTransformer = patientSearchResponseXPathTransformer;
    this.providerSearchResponseTransformer = providerSearchResponseTransformer;
    this.patientConsentResponseXPathTransformer = patientConsentResponseXPathTransformer;
    this.locationDetailsResponseTransformer = locationDetailsResponseTransformer;
    this.defaultResponseTransformer = defaultResponseTransformer;
    this.codeableConceptService = codeableConceptService;
  }

  /**
   * This method helps to select response selector depending on transaction type
   *
   * @param response {@link String} response object from DSQ
   * @return transformed object depending on Transaction Type
   */
  public Mono<R> transform(String response) {

    return Mono.deferContextual(Mono::just)
        .flatMap(
            contextView -> {
              if (response == null || contextView.isEmpty()) {
                log.error("TransformationException occurred for null response or empty context");
                throw new IllegalArgumentException("Null response or Empty Context is provided.");
              }
              MessageProcess messageProcess = contextView.get(MessageProcess.class);
              var transformedResponseMono =
                  (Mono<R>) selectResponseTransformer(messageProcess).transform(response);
              return transformedResponseMono.doOnNext(this::logNonAcknowledgementAcceptResponse);
            })
        .onErrorMap(
            e -> {
              var exceptionMessage =
                  ResponseTransformerEngine.class.getName()
                      + ": Exception occurred while doing transformation of XML response";
              var exception = new TransformationException(exceptionMessage, e);
              log.error(exceptionMessage, exception);
              return exception;
            });
  }

  /**
   * This method helps to select ResponseTransformer depending on Transaction Type
   *
   * @param messageProcess {@link MessageProcess} object of Transaction
   * @return responseTransformer {@link ResponseTransformer} depending on Transaction type.
   */
  private ResponseTransformer<
          ? extends Response<? extends ResponseControlAct, ? extends ProvincialResponse>>
      selectResponseTransformer(MessageProcess messageProcess) {
    switch (messageProcess) {
      case PATIENT_SEARCH:
        return patientSearchResponseXPathTransformer;
      case PROVIDER_SEARCH:
        return providerSearchResponseTransformer;
      case PATIENT_CONSENT:
        return patientConsentResponseXPathTransformer;
      case LOCATION_SEARCH:
        return new LocationSummaryResponseXPathTransformer(codeableConceptService);
      case LOCATION_DETAILS:
        return locationDetailsResponseTransformer;
      default:
        // Refer the DefaultResponseTransformer#transform method
        return defaultResponseTransformer;
    }
  }

  /**
   * Logs the transformed response object when acknowledgement code is non 'AA'
   *
   * @param transformedResponse {@link Response} transformed response
   */
  private void logNonAcknowledgementAcceptResponse(
      @NonNull
          Response<? extends ResponseControlAct, ? extends ProvincialResponse>
              transformedResponse) {
    if (transformedResponse.getResponseBodyTransmissionWrapper() != null
        && transformedResponse.getResponseBodyTransmissionWrapper().getAcknowledgeTypeCode() != null
        && !HL7Constants.AA_RESPONSE_ACK_TYPE_CODE.equalsIgnoreCase(
            transformedResponse
                .getResponseBodyTransmissionWrapper()
                .getAcknowledgeTypeCode()
                .trim())) {
      log.info(
          "Provincial Response Acknowledgement Details (non-AA): {} ",
          transformedResponse.getResponseBodyTransmissionWrapper().getAcknowledgementDetails());
      if (transformedResponse.getQueryAcknowledgement() != null)
        log.info(
            "Provincial Response Query Acknowledgement (non-AA): {} ",
            transformedResponse.getQueryAcknowledgement());
    }
  }
}
