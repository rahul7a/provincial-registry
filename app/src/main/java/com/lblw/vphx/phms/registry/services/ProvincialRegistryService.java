package com.lblw.vphx.phms.registry.services;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.registry.processor.processors.ProvincialRegistryProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Provincial registry service provides services for all client registry requests */
@Service
@Slf4j
public class ProvincialRegistryService {

  private final ProvincialRegistryProcessor provincialRegistryProcessor;

  public ProvincialRegistryService(ProvincialRegistryProcessor provincialRegistryProcessor) {
    this.provincialRegistryProcessor = provincialRegistryProcessor;
  }

  /**
   * Returns patient profile using provincial search criteria
   *
   * @param provincialPatientSearchCriteria {@link ProvincialPatientSearchCriteria }
   * @return Mono of provincialPatientProfile {@link ProvincialPatientProfile } received from client
   *     registry
   */
  public Mono<ProvincialPatientProfile> searchProvincialPatient(
      ProvincialPatientSearchCriteria provincialPatientSearchCriteria) {
    return Mono.deferContextual(
            contextView -> {
              provincialPatientSearchCriteria.setProvincialRequestControl(
                  contextView.get(ProvincialRequestControl.class));

              return provincialRegistryProcessor.searchProvincialPatientInClientRegistry(
                  provincialPatientSearchCriteria);
            })
        .map(ProvincialPatientSearchResponse::getProvincialResponsePayload);
  }

  /**
   * Returns provider profile using provincial search criteria
   *
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   * @return Mono of provincialProviderProfiles {@link ProvincialProviderProfiles } received from
   *     client registry
   */
  public Mono<ProvincialProviderProfiles> searchProvincialProvider(
      ProvincialProviderSearchCriteria provincialProviderSearchCriteria) {

    return Mono.deferContextual(
            contextView -> {
              provincialProviderSearchCriteria.setProvincialRequestControl(
                  contextView.get(ProvincialRequestControl.class));

              return provincialRegistryProcessor.searchProvincialProviderInClientRegistry(
                  provincialProviderSearchCriteria);
            })
        .map(ProvincialProviderSearchResponse::getProvincialResponsePayload);
  }

  /**
   * Returns patient consent using provincial patient consent criteria
   *
   * @param provincialPatientConsentCriteria {@link ProvincialPatientConsentCriteria }
   * @return Mono of provincialPatientConsent {@link ProvincialPatientConsent} received from client
   *     registry
   */
  public Mono<ProvincialPatientConsent> getProvincialPatientConsent(
      ProvincialPatientConsentCriteria provincialPatientConsentCriteria) {

    return Mono.deferContextual(
            context -> {
              provincialPatientConsentCriteria.setProvincialRequestControl(
                  context.get(ProvincialRequestControl.class));

              return provincialRegistryProcessor.getProvincialPatientConsentInClientRegistry(
                  provincialPatientConsentCriteria);
            })
        .map(ProvincialPatientConsentResponse::getProvincialResponsePayload);
  }

  /**
   * Returns location summary using provincial location search criteria
   *
   * @param provincialLocationSearchCriteria {@link ProvincialLocationSearchCriteria}
   * @return Mono of provincialLocationSummaries {@link ProvincialLocationSummaries} received from
   *     client registry
   */
  public Mono<ProvincialLocationSummaries> retrieveLocation(
      ProvincialLocationSearchCriteria provincialLocationSearchCriteria) {

    return Mono.deferContextual(
            contextView -> {
              provincialLocationSearchCriteria.setProvincialRequestControl(
                  contextView.get(ProvincialRequestControl.class));

              return provincialRegistryProcessor.searchProvincialLocationInClientRegistry(
                  provincialLocationSearchCriteria);
            })
        .map(ProvincialLocationSearchResponse::getProvincialResponsePayload);
  }

  /**
   * Returns location details using provincial location details search criteria
   *
   * @param provincialLocationDetailsCriteria {@link ProvincialLocationDetailsCriteria}
   * @return Mono of provincialLocationDetails {@link ProvincialLocationDetails} received from
   *     client registry
   */
  public Mono<ProvincialLocationDetails> retrieveLocationDetails(
      ProvincialLocationDetailsCriteria provincialLocationDetailsCriteria) {

    return Mono.deferContextual(
            context -> {
              provincialLocationDetailsCriteria.setProvincialRequestControl(
                  context.get(ProvincialRequestControl.class));
              return provincialRegistryProcessor.searchProvincialLocationDetailsInClientRegistry(
                  provincialLocationDetailsCriteria);
            })
        .map(ProvincialLocationDetailsResponse::getProvincialResponsePayload);
  }
}
