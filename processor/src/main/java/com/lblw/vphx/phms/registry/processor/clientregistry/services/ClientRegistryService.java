package com.lblw.vphx.phms.registry.processor.clientregistry.services;

import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Defines contract for interacting with the provincial DSQ Client Registry Service. */
@Service
public interface ClientRegistryService {

  /**
   * This interacts with DSQ Client Registry Service, in order to query for a provincial patient
   * using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialPatientSearchRequest {@link ProvincialPatientSearchRequest} encapsulates the
   *     HL7 V3's Search provincial patient template, populated with search criteria.
   * @return {@link ProvincialPatientSearchResponse} encapsulates the HL7 V3 Response for Provincial
   *     patient template.
   */
  Mono<ProvincialPatientSearchResponse> searchProvincialPatient(
      ProvincialPatientSearchRequest provincialPatientSearchRequest);
  /**
   * This interacts with DSQ Client Registry Service, in order to query for a provincial provider
   * using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialProviderSearchRequest {@link ProvincialProviderSearchRequest} encapsulates the
   *     HL7 V3's Search provincial provider template, populated with search criteria.
   * @return {@link ProvincialProviderSearchResponse} encapsulates the HL7 V3 Response for
   *     Provincial provider template.
   */
  Mono<ProvincialProviderSearchResponse> searchProvincialProvider(
      ProvincialProviderSearchRequest provincialProviderSearchRequest);
  /**
   * This interacts with DSQ Client Registry Service, in order to query for a provincial patient
   * consent using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialPatientConsentRequest {@link ProvincialPatientConsentRequest} encapsulates the
   *     HL7 V3's provincial patient Consent template, populated with criteria.
   * @return {@link ProvincialProviderSearchResponse} encapsulates the HL7 V3 Response for
   *     Provincial patient Consent template.
   */
  Mono<ProvincialPatientConsentResponse> getProvincialPatientConsent(
      ProvincialPatientConsentRequest provincialPatientConsentRequest);

  /**
   * This interacts with DSQ Client Registry Service, in order to query for a provincial location
   * Summary using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialLocationSearchRequest {@link ProvincialLocationSearchRequest} encapsulates the
   *     HL7 V3's Search provincial location template, populated with search criteria.
   * @return {@link ProvincialLocationSearchResponse} encapsulates the HL7 V3 Response for
   *     Provincial location template.
   */
  Mono<ProvincialLocationSearchResponse> searchProvincialLocationSummary(
      ProvincialLocationSearchRequest provincialLocationSearchRequest);

  /**
   * This interacts with DSQ Client Registry Service, in order to query for a provincial location
   * Details using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialLocationDetailsRequest {@link ProvincialLocationDetailsRequest} encapsulates
   *     the HL7 V3's Search provincial location template, populated with search criteria.
   * @return {@link ProvincialLocationDetailsResponse} encapsulates the HL7 V3 Response for
   *     Provincial location template.
   */
  Mono<ProvincialLocationDetailsResponse> searchProvincialLocationDetails(
      ProvincialLocationDetailsRequest provincialLocationDetailsRequest);
}
