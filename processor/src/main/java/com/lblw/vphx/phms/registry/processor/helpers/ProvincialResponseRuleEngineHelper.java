package com.lblw.vphx.phms.registry.processor.helpers;

import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.Status;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.registry.processor.clientregistry.services.QuebecClientRegistryService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.Collections;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * This is a helper to {@link QuebecClientRegistryService} and helps validate response objects using
 * Drools
 */
@Component
@Slf4j
public class ProvincialResponseRuleEngineHelper {
  private DroolsEngineService droolsEngineService;

  /**
   * public constructor
   *
   * @param droolsEngineService {@link DroolsEngineService}
   */
  public ProvincialResponseRuleEngineHelper(DroolsEngineService droolsEngineService) {
    this.droolsEngineService = droolsEngineService;
  }

  /**
   * Calls drools service to validate patient search response. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param patientSearchResponse {@link ProvincialPatientSearchResponse} to be validated
   * @return {@link ProvincialPatientSearchResponse} that has been validated
   */
  public ProvincialPatientSearchResponse validatePatientSearchResponse(
      ProvincialPatientSearchResponse patientSearchResponse) {

    if (patientSearchResponse == null) {
      patientSearchResponse = ProvincialPatientSearchResponse.builder().build();
    }
    if (isOperationOutcomeNull(patientSearchResponse)) {
      return patientSearchResponse;
    }

    ProvincialPatientProfile provincialPatientProfile =
        patientSearchResponse.getProvincialResponsePayload();

    if (provincialPatientProfile == null) {
      provincialPatientProfile = ProvincialPatientProfile.builder().build();
    }

    ProvincialResponseAcknowledgement responseAcknowledgement =
        provincialPatientProfile.getProvincialResponseAcknowledgement();

    final OperationOutcome ruleEngineOperationOutcome =
        droolsEngineService.processPatientResponse(patientSearchResponse);
    responseAcknowledgement.setOperationOutcome(ruleEngineOperationOutcome);

    provincialPatientProfile.setProvincialResponseAcknowledgement(responseAcknowledgement);
    patientSearchResponse.setProvincialResponsePayload(provincialPatientProfile);

    return patientSearchResponse;
  }

  /**
   * Calls drools service to validate provider search response. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param provincialProviderSearchResponse {@link ProvincialProviderSearchResponse} to be
   *     validated
   * @return {@link ProvincialProviderSearchResponse} that has been validated
   */
  public ProvincialProviderSearchResponse validateProviderSearchResponse(
      ProvincialProviderSearchResponse provincialProviderSearchResponse) {

    if (provincialProviderSearchResponse == null) {
      provincialProviderSearchResponse = ProvincialProviderSearchResponse.builder().build();
    }
    if (isOperationOutcomeNull(provincialProviderSearchResponse)) {
      return provincialProviderSearchResponse;
    }

    ProvincialProviderProfiles provincialProviderProfiles =
        provincialProviderSearchResponse.getProvincialResponsePayload();

    if (provincialProviderProfiles == null) {
      provincialProviderProfiles = ProvincialProviderProfiles.builder().build();
    }

    ProvincialResponseAcknowledgement responseAcknowledgement =
        provincialProviderProfiles.getProvincialResponseAcknowledgement();

    final OperationOutcome ruleEngineOperationOutcome =
        droolsEngineService.processProviderResponse(provincialProviderSearchResponse);
    responseAcknowledgement.setOperationOutcome(ruleEngineOperationOutcome);

    provincialProviderProfiles.setProvincialResponseAcknowledgement(responseAcknowledgement);
    provincialProviderSearchResponse.setProvincialResponsePayload(provincialProviderProfiles);

    return provincialProviderSearchResponse;
  }

  /**
   * Calls drools service to validate Patient Consent response. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param provincialPatientConsentResponse {@link ProvincialPatientConsentResponse} to be
   *     validated
   * @return {@link ProvincialProviderSearchResponse} that has been validated
   */
  public ProvincialPatientConsentResponse validateConsentSearchResponse(
      ProvincialPatientConsentResponse provincialPatientConsentResponse) {

    if (provincialPatientConsentResponse == null) {
      provincialPatientConsentResponse = ProvincialPatientConsentResponse.builder().build();
    }
    if (isOperationOutcomeNull(provincialPatientConsentResponse)) {
      return provincialPatientConsentResponse;
    }

    ProvincialPatientConsent provincialPatientConsent =
        provincialPatientConsentResponse.getProvincialResponsePayload();

    if (provincialPatientConsent == null) {
      provincialPatientConsent = ProvincialPatientConsent.builder().build();
    }

    ProvincialResponseAcknowledgement responseAcknowledgement =
        provincialPatientConsent.getProvincialResponseAcknowledgement();

    final OperationOutcome ruleEngineOperationOutcome =
        droolsEngineService.processProvincialPatientConsentResponse(
            provincialPatientConsentResponse);
    responseAcknowledgement.setOperationOutcome(ruleEngineOperationOutcome);
    provincialPatientConsent.setProvincialResponseAcknowledgement(responseAcknowledgement);
    provincialPatientConsentResponse.setProvincialResponsePayload(provincialPatientConsent);

    return provincialPatientConsentResponse;
  }

  /**
   * Calls drools service to validate Location search response. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param provincialLocationSearchResponse {@link ProvincialLocationSearchResponse} to be
   *     validated
   * @return {@link ProvincialLocationSearchResponse} that has been validated
   */
  public ProvincialLocationSearchResponse validateLocationSearchResponse(
      ProvincialLocationSearchResponse provincialLocationSearchResponse) {

    if (Objects.isNull(provincialLocationSearchResponse)) {
      provincialLocationSearchResponse = ProvincialLocationSearchResponse.builder().build();
    }

    ProvincialLocationSummaries provincialLocationSummaries =
        provincialLocationSearchResponse.getProvincialResponsePayload();

    if (Objects.isNull(provincialLocationSummaries)) {
      provincialLocationSummaries = ProvincialLocationSummaries.builder().build();
    }
    if (isOperationOutcomeNull(provincialLocationSearchResponse)) {
      return provincialLocationSearchResponse;
    }
    final ProvincialResponseAcknowledgement responseAcknowledgement =
        provincialLocationSummaries.getProvincialResponseAcknowledgement();
    final OperationOutcome ruleEngineOperationOutcome =
        droolsEngineService.processProvincialLocationSummariesResponse(
            provincialLocationSearchResponse);

    if (ruleEngineOperationOutcome.getStatus().getCode().equals(Status.REJECT)) {
      provincialLocationSummaries.setProvincialLocations(null);
    }
    responseAcknowledgement.setOperationOutcome(ruleEngineOperationOutcome);
    provincialLocationSummaries.setProvincialResponseAcknowledgement(responseAcknowledgement);
    provincialLocationSearchResponse.setProvincialResponsePayload(provincialLocationSummaries);

    return provincialLocationSearchResponse;
  }

  /**
   * Calls drools service to validate Location details response. Updates the response
   * acknowledgement with status and any error messages or codes accordingly
   *
   * @param provincialLocationDetailsResponse {@link ProvincialLocationDetailsResponse} to be
   *     validated
   * @return {@link ProvincialLocationDetailsResponse} that has been validated
   */
  public ProvincialLocationDetailsResponse validateLocationDetailsResponse(
      ProvincialLocationDetailsResponse provincialLocationDetailsResponse) {

    if (provincialLocationDetailsResponse == null) {
      provincialLocationDetailsResponse = ProvincialLocationDetailsResponse.builder().build();
    }

    ProvincialLocationDetails provincialLocationDetails =
        provincialLocationDetailsResponse.getProvincialResponsePayload();

    if (provincialLocationDetails == null) {
      provincialLocationDetails = ProvincialLocationDetails.builder().build();
    }
    if (isOperationOutcomeNull(provincialLocationDetailsResponse)) {
      return provincialLocationDetailsResponse;
    }

    ProvincialResponseAcknowledgement responseAcknowledgement =
        provincialLocationDetails.getProvincialResponseAcknowledgement();

    final OperationOutcome ruleEngineOperationOutcome =
        droolsEngineService.processProvincialLocationDetailsResponse(
            provincialLocationDetailsResponse);
    responseAcknowledgement.setOperationOutcome(ruleEngineOperationOutcome);

    provincialLocationDetails.setProvincialResponseAcknowledgement(responseAcknowledgement);
    provincialLocationDetailsResponse.setProvincialResponsePayload(provincialLocationDetails);

    return provincialLocationDetailsResponse;
  }

  /**
   * Calls drools service to validate Location details request. Updates the response *
   * acknowledgement with status and any error messages or codes accordingly
   *
   * @param provincialLocationDetailsRequest {@link ProvincialLocationDetailsRequest }
   * @return ProvincialLocationDetailsResponse {@link ProvincialLocationDetailsResponse} that has
   *     been validated
   */
  public ProvincialLocationDetailsResponse validateLocationDetailsRequest(
      @Nullable ProvincialLocationDetailsRequest provincialLocationDetailsRequest) {

    // TODO: Need to implement this method in reactive way and set the messageProcess from context
    // in OperationOutcome

    var payload = ProvincialLocationDetails.builder().build();
    payload.setProvincialResponseAcknowledgement(
        ProvincialResponseAcknowledgement.builder().build());
    final OperationOutcome ruleEngineOperationOutcome =
        droolsEngineService.processRequest(provincialLocationDetailsRequest);
    payload
        .getProvincialResponseAcknowledgement()
        .setAuditEvent(
            AuditEvent.builder()
                .provincialRequestControl(
                    provincialLocationDetailsRequest == null
                        ? null
                        : provincialLocationDetailsRequest
                            .getProvincialRequestPayload()
                            .getProvincialRequestControl())
                .build());

    payload.getProvincialResponseAcknowledgement().setOperationOutcome(ruleEngineOperationOutcome);

    return ProvincialLocationDetailsResponse.builder().provincialResponsePayload(payload).build();
  }

  /**
   * Calls drools service to validate Provider search request. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param provincialProviderSearchRequest {@link ProvincialProviderSearchRequest }
   * @return ProvincialProviderSearchResponse {@link ProvincialProviderSearchResponse} that has been
   *     validated
   */
  public ProvincialProviderSearchResponse validateProviderSearchRequest(
      @Nullable ProvincialProviderSearchRequest provincialProviderSearchRequest) {
    var ruleEngineOperationOutcome =
        droolsEngineService.processRequest(provincialProviderSearchRequest);
    var payload =
        ProvincialProviderProfiles.builder()
            .provincialResponseAcknowledgement(ProvincialResponseAcknowledgement.builder().build())
            .build();
    payload
        .getProvincialResponseAcknowledgement()
        .setAuditEvent(
            AuditEvent.builder()
                .provincialRequestControl(
                    provincialProviderSearchRequest == null
                        ? null
                        : provincialProviderSearchRequest
                            .getProvincialRequestPayload()
                            .getProvincialRequestControl())
                .build());
    payload.getProvincialResponseAcknowledgement().setOperationOutcome(ruleEngineOperationOutcome);

    return ProvincialProviderSearchResponse.builder().provincialResponsePayload(payload).build();
  }

  /**
   * Calls drools service to validate patient consent request. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param provincialPatientConsentRequest {@link ProvincialPatientConsentRequest}
   * @return ProvincialPatientConsentResponse {@link ProvincialPatientConsentResponse} that has been
   *     validated
   */
  public ProvincialPatientConsentResponse validatePatientConsentRequest(
      @Nullable ProvincialPatientConsentRequest provincialPatientConsentRequest) {
    var ruleEngineOperationOutcome =
        droolsEngineService.processRequest(provincialPatientConsentRequest);
    var payload =
        ProvincialPatientConsent.builder()
            .provincialResponseAcknowledgement(ProvincialResponseAcknowledgement.builder().build())
            .build();
    payload
        .getProvincialResponseAcknowledgement()
        .setAuditEvent(
            AuditEvent.builder()
                .provincialRequestControl(
                    provincialPatientConsentRequest == null
                        ? null
                        : provincialPatientConsentRequest
                            .getProvincialRequestPayload()
                            .getProvincialRequestControl())
                .build());
    payload.getProvincialResponseAcknowledgement().setOperationOutcome(ruleEngineOperationOutcome);

    return ProvincialPatientConsentResponse.builder().provincialResponsePayload(payload).build();
  }

  /**
   * Calls drools service to validate patient search request. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param provincialPatientSearchRequest {@link ProvincialPatientSearchRequest}
   * @return ProvincialPatientSearchResponse {@link ProvincialPatientSearchResponse} that has been
   *     validated
   */
  public ProvincialPatientSearchResponse validatePatientSearchRequest(
      @Nullable ProvincialPatientSearchRequest provincialPatientSearchRequest) {
    var ruleEngineOperationOutcome =
        droolsEngineService.processRequest(provincialPatientSearchRequest);
    var payload =
        ProvincialPatientProfile.builder()
            .provincialResponseAcknowledgement(ProvincialResponseAcknowledgement.builder().build())
            .build();
    payload
        .getProvincialResponseAcknowledgement()
        .setAuditEvent(
            AuditEvent.builder()
                .provincialRequestControl(
                    provincialPatientSearchRequest == null
                        ? null
                        : provincialPatientSearchRequest
                            .getProvincialRequestPayload()
                            .getProvincialRequestControl())
                .build());
    payload.getProvincialResponseAcknowledgement().setOperationOutcome(ruleEngineOperationOutcome);

    return ProvincialPatientSearchResponse.builder().provincialResponsePayload(payload).build();
  }

  /**
   * Calls drools service to validate location search request. Updates the response acknowledgement
   * with status and any error messages or codes accordingly
   *
   * @param provincialLocationSearchRequest {@link ProvincialLocationSearchRequest}
   * @return ProvincialLocationSearchResponse {@link ProvincialLocationSearchResponse} that has been
   *     validated
   */
  public ProvincialLocationSearchResponse validateLocationSearchRequest(
      @Nullable ProvincialLocationSearchRequest provincialLocationSearchRequest) {
    var ruleEngineOperationOutcome =
        droolsEngineService.processRequest(provincialLocationSearchRequest);
    var payload =
        ProvincialLocationSummaries.builder()
            .provincialResponseAcknowledgement(
                ProvincialResponseAcknowledgement.builder()
                    .auditEvent(AuditEvent.builder().build())
                    .operationOutcome(OperationOutcome.builder().build())
                    .build())
            .provincialLocations(Collections.emptyList())
            .build();
    payload.setProvincialLocations(null);
    payload
        .getProvincialResponseAcknowledgement()
        .getAuditEvent()
        .setProvincialRequestControl(
            provincialLocationSearchRequest == null
                ? null
                : provincialLocationSearchRequest
                    .getProvincialRequestPayload()
                    .getProvincialRequestControl());
    payload.getProvincialResponseAcknowledgement().setOperationOutcome(ruleEngineOperationOutcome);

    return ProvincialLocationSearchResponse.builder().provincialResponsePayload(payload).build();
  }

  /**
   * This function used to check whether Operation outCome is blank or not
   *
   * @param response {@link Response}
   * @return true or false {@link Boolean}
   */
  private boolean isOperationOutcomeNull(Response<?, ?> response) {
    return response
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
        != null;
  }
}
