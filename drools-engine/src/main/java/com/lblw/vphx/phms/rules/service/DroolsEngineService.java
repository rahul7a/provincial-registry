package com.lblw.vphx.phms.rules.service;

import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.rules.model.DroolsEngineResponseValidation;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/** Exposed service used to validate the requests and responses for the Provincial Patient Search */
@Service
@Slf4j
public class DroolsEngineService {

  private static final String PATIENT = "PATIENT";
  private static final String PATIENT_CONSENT = "PATIENT CONSENT";
  private static final String PROVIDER = "PROVIDER";
  private static final String LOCATION_SUMMARY = "LOCATION SUMMARY";
  private static final String LOCATION_DETAILS = "LOCATION DETAILS";
  private final AgendaEventListener agendaEventListener;
  private KieContainer kieContainer;
  private CodeableConceptValidationService codeableConceptValidationService;

  /**
   * Class Constructor
   *
   * @param kieContainer {@link KieContainer}
   * @param codeableConceptValidationService {@link CodeableConceptValidationService}
   * @param agendaEventListener {@link AgendaEventListener}
   */
  public DroolsEngineService(
      KieContainer kieContainer,
      CodeableConceptValidationService codeableConceptValidationService,
      AgendaEventListener agendaEventListener) {
    this.kieContainer = kieContainer;
    this.codeableConceptValidationService = codeableConceptValidationService;
    this.agendaEventListener = agendaEventListener;
  }

  /**
   * Prepares an instance of {@link OperationOutcome} from the given {@link
   * ProvincialPatientSearchResponse} after validating the same. In case the {@link
   * ProvincialPatientSearchResponse} is null, then it returns a failed {@link OperationOutcome}
   *
   * @param provincialPatientSearchResponse {@link ProvincialPatientSearchResponse}
   * @return the prepared {@link OperationOutcome}
   */
  public OperationOutcome processPatientResponse(
      ProvincialPatientSearchResponse provincialPatientSearchResponse) {
    log.debug("Validating provincialPatientSearchResponse");
    if (provincialPatientSearchResponse == null) {
      log.debug(
          "building default acknowledgement for null value of  provincialPatientSearchResponse");
      return buildDefaultOperationOutcome();
    }

    DroolsEngineResponseValidation droolsEngineResponseValidation =
        buildDroolsEngineResponseValidation(provincialPatientSearchResponse, Province.QC, PATIENT);

    // Get matching index only if profile is present
    if (provincialPatientSearchResponse.getProvincialResponsePayload() != null) {
      droolsEngineResponseValidation.setMatchingIndex(
          provincialPatientSearchResponse.getProvincialResponsePayload().getMatchingIndex());
    }

    return executeDroolsSession(droolsEngineResponseValidation);
  }

  /**
   * Prepares an instance of {@link OperationOutcome} from the given {@link
   * ProvincialProviderSearchResponse} after validating the same. In case the {@link
   * ProvincialProviderSearchResponse} is null, then it returns a failed {@link OperationOutcome}
   *
   * @param provincialProviderSearchResponse {@link ProvincialProviderSearchResponse} to validate
   * @return the prepared {@link OperationOutcome}
   */
  public OperationOutcome processProviderResponse(
      ProvincialProviderSearchResponse provincialProviderSearchResponse) {
    log.debug("Validating  provincialProviderSearchResponse");
    if (provincialProviderSearchResponse == null) {
      log.debug(
          "building default acknowledgement for null value of  provincialProviderSearchResponse");

      return buildDefaultOperationOutcome();
    }

    DroolsEngineResponseValidation droolsEngineResponseValidation =
        buildDroolsEngineResponseValidation(
            provincialProviderSearchResponse, Province.QC, PROVIDER);

    return executeDroolsSession(droolsEngineResponseValidation);
  }

  /**
   * Prepares an instance of {@link OperationOutcome} from the given {@link
   * ProvincialPatientConsentResponse} after validating the same. In case the {@link
   * ProvincialPatientConsentResponse} is null, then it returns a failed {@link OperationOutcome}
   *
   * @param provincialPatientConsentResponse {@link ProvincialPatientConsentResponse} to validate
   * @return the prepared {@link OperationOutcome}
   */
  public OperationOutcome processProvincialPatientConsentResponse(
      ProvincialPatientConsentResponse provincialPatientConsentResponse) {
    log.debug("Validating provincialPatientConsentResponse");

    if (provincialPatientConsentResponse == null) {
      log.debug(
          "building default acknowledgement for null value of  provincialPatientConsentResponse");

      return buildDefaultOperationOutcome();
    }

    DroolsEngineResponseValidation droolsEngineResponseValidation =
        buildDroolsEngineResponseValidation(
            provincialPatientConsentResponse, Province.QC, PATIENT_CONSENT);

    // Get Consent Validity Start DateTime only if Consent Token is present
    if (provincialPatientConsentResponse.getProvincialResponsePayload() != null) {
      droolsEngineResponseValidation.setConsentValidityStartDateTime(
          provincialPatientConsentResponse
              .getProvincialResponsePayload()
              .getConsentValidityStartDateTime());
    }

    return executeDroolsSession(droolsEngineResponseValidation);
  }

  /**
   * Prepares an instance of {@link OperationOutcome} from the given {@link
   * ProvincialLocationSearchResponse} after validating the same. In case the {@link
   * ProvincialLocationSearchResponse} is null, then it returns a failed {@link OperationOutcome}
   *
   * @param provincialLocationSearchResponse {@link ProvincialPatientConsentResponse} to validate
   * @return the prepared {@link OperationOutcome}
   */
  public OperationOutcome processProvincialLocationSummariesResponse(
      ProvincialLocationSearchResponse provincialLocationSearchResponse) {
    log.debug("Validating  provincialLocationSearchResponse ");

    if (provincialLocationSearchResponse == null) {
      log.debug(
          "building default acknowledgement for null value of provincialLocationSearchResponse");

      return buildDefaultOperationOutcome();
    }

    DroolsEngineResponseValidation droolsEngineResponseValidation =
        buildDroolsEngineResponseValidation(
            provincialLocationSearchResponse, Province.QC, LOCATION_SUMMARY);

    return executeDroolsSession(droolsEngineResponseValidation);
  }

  /**
   * Prepares an instance of {@link OperationOutcome} from the given {@link
   * ProvincialLocationDetailsResponse} after validating the same. In case the {@link
   * ProvincialLocationDetailsResponse} is null, then it returns a failed {@link OperationOutcome}
   *
   * @param provincialLocationDetailsResponse {@link ProvincialLocationDetailsResponse} to validate
   * @return the prepared {@link OperationOutcome}
   */
  public OperationOutcome processProvincialLocationDetailsResponse(
      ProvincialLocationDetailsResponse provincialLocationDetailsResponse) {
    log.debug("Validating  provincialLocationDetailsResponse");
    if (provincialLocationDetailsResponse == null) {
      log.debug(
          "building default acknowledgement for null value of provincialLocationDetailsResponse");
      return buildDefaultOperationOutcome();
    }

    DroolsEngineResponseValidation droolsEngineResponseValidation =
        buildDroolsEngineResponseValidation(
            provincialLocationDetailsResponse, Province.QC, LOCATION_DETAILS);

    return executeDroolsSession(droolsEngineResponseValidation);
  }

  /**
   * Prepares an instance of {@link OperationOutcome} from the given {@link
   * ProvincialLocationSearchRequest} after validating the same. In case the {@link
   * ProvincialLocationSearchRequest} is null, then it returns a failed {@link OperationOutcome}
   *
   * @param request {@link Request}
   * @return the prepared {@link OperationOutcome}
   */
  public OperationOutcome processRequest(
      Request<? extends ProvincialRequest> request) {
    log.debug("Validating  Request");

    if (request == null) {
      log.debug("building default acknowledgement for null value of Request");
      return buildDefaultOperationOutcome();
    }
    return executeDroolsSession(request);
  }

  /**
   * Builds {@link DroolsEngineResponseValidation} object from {@link Response}, province and
   * identifierType
   *
   * @param response {@link Response} response object type.
   * @param province Province name
   * @param identifierType identifierType
   * @return Prepared {@link DroolsEngineResponseValidation} instance.
   */
  @NonNull
  private DroolsEngineResponseValidation buildDroolsEngineResponseValidation(
      @NonNull Response<? extends ResponseControlAct, ? extends ProvincialResponse> response,
      @NonNull Province province,
      @NonNull String identifierType) {
    DroolsEngineResponseValidation droolsEngineResponseValidation =
        new DroolsEngineResponseValidation(province, identifierType);

    if (response.getResponseBodyTransmissionWrapper() != null) {
      droolsEngineResponseValidation.setAcknowledgementCode(
          response.getResponseBodyTransmissionWrapper().getAcknowledgeTypeCode());

      populateFromAcknowledgementDetails(droolsEngineResponseValidation, response);
    }

    // Get query ack code only if query ack parent is present
    final QueryAcknowledgement queryAcknowledgement = response.getQueryAcknowledgement();
    if (queryAcknowledgement != null) {
      droolsEngineResponseValidation.setQueryResponseCode(
          queryAcknowledgement.getQueryResponseCode());
    }
    droolsEngineResponseValidation.setDetectedIssues(response.getDetectedIssues());
    return droolsEngineResponseValidation;
  }

  /**
   * Populates into {@link DroolsEngineResponseValidation} the relevant values from the {@link
   * Response}'s {@link AcknowledgementDetails}
   *
   * <p>Assumption that we will only be dealing with one acknowledgement detail node
   *
   * @param droolsEngineResponseValidation {@link DroolsEngineResponseValidation}
   * @param response {@link Response}
   */
  private void populateFromAcknowledgementDetails(
      DroolsEngineResponseValidation droolsEngineResponseValidation,
      Response<? extends ResponseControlAct, ? extends ProvincialResponse> response) {
    List<AcknowledgementDetails> acknowledgementDetailsList =
        response.getResponseBodyTransmissionWrapper().getAcknowledgementDetails();

    if (CollectionUtils.isNotEmpty(acknowledgementDetailsList)) {
      AcknowledgementDetails acknowledgementDetails = acknowledgementDetailsList.get(0);

      droolsEngineResponseValidation.setAcknowledgementDetailsCode(
          acknowledgementDetails.getCode());
      droolsEngineResponseValidation.setAcknowledgementDetailsTypeCode(
          acknowledgementDetails.getTypeCode());
      droolsEngineResponseValidation.setAcknowledgementDetailsLocation(
          acknowledgementDetails.getLocation());
    }
  }

  /**
   * This applies the Drools engine's rules on the {@link DroolsEngineResponseValidation} object to
   * prepare {@link OperationOutcome}
   *
   * @param droolsEngineResponseValidation {@link DroolsEngineResponseValidation}
   * @return prepared {@link OperationOutcome}
   */
  @NonNull
  private OperationOutcome executeDroolsSession(
      @NonNull DroolsEngineResponseValidation droolsEngineResponseValidation) {
    OperationOutcome operationOutcome =
        OperationOutcome.builder().issues(new ArrayList<>()).status(null).build();
    KieSession kieSession = kieContainer.newKieSession();
    kieSession.insert(droolsEngineResponseValidation);
    kieSession.addEventListener(agendaEventListener);
    kieSession.insert(operationOutcome);
    kieSession.fireAllRules();
    kieSession.dispose();
    return operationOutcome;
  }

  /**
   * This applies the Drools engine's rules on the {@link DroolsEngineResponseValidation} object to
   * prepare {@link OperationOutcome}
   *
   * @param request {@link DroolsEngineResponseValidation}
   * @return prepared {@link OperationOutcome}
   */
  @NonNull
  private OperationOutcome executeDroolsSession(
      @NonNull Request<? extends ProvincialRequest> request) {
    OperationOutcome operationOutcome =
        OperationOutcome.builder().issues(new ArrayList<>()).status(null).build();
    KieSession kieSession = kieContainer.newKieSession();
    kieSession.setGlobal("codeableConceptValidationService", codeableConceptValidationService);
    kieSession.insert(request);
    kieSession.insert(operationOutcome);
    kieSession.addEventListener(agendaEventListener);
    kieSession.fireAllRules();
    kieSession.dispose();
    return operationOutcome;
  }

  /**
   * Build and return operation outcome for default error scenario {@link OperationOutcome}
   *
   * @return operation outcome for error
   */
  private OperationOutcome buildDefaultOperationOutcome() {
    return OperationOutcome.builder()
        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
        .classification(ResponseClassification.SYSTEM)
        .issues(new ArrayList<>())
        .build();
  }
}
