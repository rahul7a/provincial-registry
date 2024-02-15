package com.lblw.vphx.phms.registry.controllers;

import com.lblw.vphx.phms.common.constants.APIConstants;
import com.lblw.vphx.phms.common.constants.HeaderConstants;
import com.lblw.vphx.phms.common.constants.OpenAPIConstants;
import com.lblw.vphx.phms.configurations.OpenAPIConfiguration;
import com.lblw.vphx.phms.domain.common.DomainResponse;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.registry.services.ProvincialRegistryService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.function.Function;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/** Controller class exposing APIs for Provincial Registry calls/services */
@RestController
@Slf4j
@OpenAPIDefinition(
    info =
        @Info(
            title = "PHMS Provincial Registry APIs",
            description =
                "Provincial registry services. PHMS error message code standards: [Error Codes](https://confluence.lblw.cloud/x/I2ViHg)"),
    tags = {
      @Tag(
          name = OpenAPIConstants.Tags.PROVINCIAL_CLIENT_REGISTRY_TRANSACTIONS_TAGS,
          description =
              "Provincial registry APIs to search patient, prescriber and location details.")
    })
public class ProvincialRegistryController {

  private final ProvincialRegistryService provincialRegistryService;
  /**
   * class constructor
   *
   * @param provincialRegistryService {@link ProvincialRegistryService}
   */
  ProvincialRegistryController(ProvincialRegistryService provincialRegistryService) {
    this.provincialRegistryService = provincialRegistryService;
  }

  /**
   * Endpoint for retrieving provincial patient profile from client registry Headers included in
   * response: {@link HeaderConstants#X_RESPONSE_ID_HEADER}
   *
   * @param provincialPatientSearchCriteria {@link ProvincialPatientSearchCriteria} request body
   *     search criteria, cannot be null
   * @param xRequestId {@link String} request header {@link HeaderConstants#X_REQUEST_ID_HEADER},
   *     cannot be blank
   * @return Mono containing {@link DomainResponse<ProvincialPatientProfile>}
   * @exception IllegalArgumentException if provincial health number is null/empty/blank.
   */
  @PostMapping(value = APIConstants.PATIENT_SEARCH_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      security = {@SecurityRequirement(name = OpenAPIConfiguration.OPEN_API_SECURITY_KEY)},
      summary = "Provincial patient search - IRA1",
      tags = OpenAPIConstants.Tags.PROVINCIAL_CLIENT_REGISTRY_TRANSACTIONS_TAGS,
      description =
          "Search for a patient in the provincial client registry, to obtain its unique patient provincial id and details.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = OpenAPIConstants.StatusCode.OK,
            description = OpenAPIConstants.HTTPConstants.OK)
      })
  public Mono<DomainResponse<ProvincialPatientProfile>> searchPatient(
      @Valid @NotNull @RequestBody ProvincialPatientSearchCriteria provincialPatientSearchCriteria,
      @NotEmpty(message = HeaderConstants.X_REQUEST_ID_REQUIRED)
          @RequestHeader(name = HeaderConstants.X_REQUEST_ID_HEADER)
          String xRequestId) {

    return Mono.just(provincialPatientSearchCriteria)
        .transform(
            requestMono ->
                enrichDomainResponse(
                    requestMono, provincialRegistryService::searchProvincialPatient))
        .map(this::mapProvincialResponseToDomainResponse)
        .doOnSubscribe(
            sub ->
                log.info(
                    String.format(
                        HeaderConstants.LOG_REQUEST_ID,
                        APIConstants.PATIENT_SEARCH_URI,
                        xRequestId)));
  }

  /**
   * Endpoint for retrieving provincial provider profile from client registry
   *
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   * @param xRequestId {@link String} request header {@link HeaderConstants#X_REQUEST_ID_HEADER},
   *     cannot be blank
   * @return Mono of response {@link ResponseEntity} containing {@link
   *     DomainResponse<ProvincialProviderProfiles>}
   */
  @PostMapping(
      value = APIConstants.PROVIDER_SEARCH_URI,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      security = {@SecurityRequirement(name = OpenAPIConfiguration.OPEN_API_SECURITY_KEY)},
      summary = "Get a specific provider details - RIA1",
      tags = OpenAPIConstants.Tags.PROVINCIAL_CLIENT_REGISTRY_TRANSACTIONS_TAGS,
      description = "The query is used to obtain the details of one specific provider.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = OpenAPIConstants.StatusCode.OK,
            description = OpenAPIConstants.HTTPConstants.OK)
      })
  public Mono<DomainResponse<ProvincialProviderProfiles>> searchProvider(
      @Valid @NotNull @RequestBody
          ProvincialProviderSearchCriteria provincialProviderSearchCriteria,
      @NotEmpty(message = HeaderConstants.X_REQUEST_ID_REQUIRED)
          @RequestHeader(name = HeaderConstants.X_REQUEST_ID_HEADER)
          String xRequestId) {

    return Mono.just(provincialProviderSearchCriteria)
        .transform(
            requestMono ->
                enrichDomainResponse(
                    requestMono, provincialRegistryService::searchProvincialProvider))
        .map(this::mapProvincialResponseToDomainResponse)
        .doOnSubscribe(
            sub ->
                log.info(
                    String.format(
                        HeaderConstants.LOG_REQUEST_ID,
                        APIConstants.PROVIDER_SEARCH_URI,
                        xRequestId)));
  }

  /**
   * Endpoint for retrieving patient consent from client registry
   *
   * @param provincialPatientConsentCriteria {@link ProvincialPatientConsentCriteria}
   * @param xRequestId {@link String} request header {@link HeaderConstants#X_REQUEST_ID_HEADER},
   *     cannot be blank
   * @return Mono of {@link ResponseEntity} containing {@link
   *     DomainResponse<ProvincialPatientConsent>}
   */
  @PostMapping(
      value = APIConstants.PATIENT_CONSENT_URI,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      security = {@SecurityRequirement(name = OpenAPIConfiguration.OPEN_API_SECURITY_KEY)},
      summary = "Get a patient consent - CTB1",
      tags = OpenAPIConstants.Tags.PROVINCIAL_CLIENT_REGISTRY_TRANSACTIONS_TAGS,
      description =
          "Obtain a consent token (from the consent registry) for a specific patient and for a specific validity period. It is a proof that the patient is giving his consent for the transmission of his health information contained in the provincial registry.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = OpenAPIConstants.StatusCode.OK,
            description = OpenAPIConstants.HTTPConstants.OK)
      })
  public Mono<DomainResponse<ProvincialPatientConsent>> getPatientConsent(
      @Valid @NotNull @RequestBody
          ProvincialPatientConsentCriteria provincialPatientConsentCriteria,
      @NotEmpty(message = HeaderConstants.X_REQUEST_ID_REQUIRED)
          @RequestHeader(name = HeaderConstants.X_REQUEST_ID_HEADER)
          String xRequestId) {

    return Mono.just(provincialPatientConsentCriteria)
        .transform(
            requestMono ->
                enrichDomainResponse(
                    requestMono, provincialRegistryService::getProvincialPatientConsent))
        .map(this::mapProvincialResponseToDomainResponse)
        .doOnSubscribe(
            sub ->
                log.info(
                    String.format(
                        HeaderConstants.LOG_REQUEST_ID,
                        APIConstants.PATIENT_CONSENT_URI,
                        xRequestId)));
  }

  /**
   * Endpoint for retrieving pharmacy location from client registry
   *
   * @param provincialLocationSearchCriteria {@link ProvincialLocationSearchCriteria}
   * @param xRequestId {@link String} request header {@link HeaderConstants#X_REQUEST_ID_HEADER},
   *     cannot be blank
   * @return Mono of {@link ResponseEntity} containing {@link
   *     DomainResponse<ProvincialLocationSummaries>}
   */
  @PostMapping(
      value = APIConstants.LOCATION_SEARCH_URI,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      security = {@SecurityRequirement(name = OpenAPIConfiguration.OPEN_API_SECURITY_KEY)},
      summary = "Search service delivery locations - RLB2",
      tags = OpenAPIConstants.Tags.PROVINCIAL_CLIENT_REGISTRY_TRANSACTIONS_TAGS,
      description =
          "Search in the location registry for a list of locations that corresponds to the search parameters provided. For each location in the list, this transaction will provide its unique location identifier along with other useful identification information.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = OpenAPIConstants.StatusCode.OK,
            description = OpenAPIConstants.HTTPConstants.OK)
      })
  public Mono<DomainResponse<ProvincialLocationSummaries>> retrieveLocation(
      @Valid @NotNull @RequestBody
          ProvincialLocationSearchCriteria provincialLocationSearchCriteria,
      @NotEmpty(message = HeaderConstants.X_REQUEST_ID_REQUIRED)
          @RequestHeader(name = HeaderConstants.X_REQUEST_ID_HEADER)
          String xRequestId) {
    return Mono.just(provincialLocationSearchCriteria)
        .transform(
            requestMono ->
                enrichDomainResponse(requestMono, provincialRegistryService::retrieveLocation))
        .map(this::mapProvincialResponseToDomainResponse)
        .doOnSubscribe(
            sub ->
                log.info(
                    String.format(
                        HeaderConstants.LOG_REQUEST_ID,
                        APIConstants.LOCATION_SEARCH_URI,
                        xRequestId)));
  }

  /**
   * Endpoint for retrieving pharmacy location Details from client registry
   *
   * @param provincialLocationDetailsCriteria {@link ProvincialLocationDetailsCriteria}
   * @param xRequestId {@link String} request header {@link HeaderConstants#X_REQUEST_ID_HEADER#},
   *     cannot be blank
   * @return Mono of {@link ResponseEntity} containing {@link
   *     DomainResponse<ProvincialLocationDetails>}
   */
  @PostMapping(
      value = APIConstants.LOCATION_DETAILS_URI,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      security = {@SecurityRequirement(name = OpenAPIConfiguration.OPEN_API_SECURITY_KEY)},
      summary = "Get a specific service delivery location details - RLB4",
      tags = OpenAPIConstants.Tags.PROVINCIAL_CLIENT_REGISTRY_TRANSACTIONS_TAGS,
      description =
          "The query is used to obtain the details of one specific service delivery location.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = OpenAPIConstants.StatusCode.OK,
            description = OpenAPIConstants.HTTPConstants.OK)
      })
  public Mono<DomainResponse<ProvincialLocationDetails>> retrieveLocationDetails(
      @Valid @NotNull @RequestBody
          ProvincialLocationDetailsCriteria provincialLocationDetailsCriteria,
      @NotEmpty(message = HeaderConstants.X_REQUEST_ID_REQUIRED)
          @RequestHeader(name = HeaderConstants.X_REQUEST_ID_HEADER)
          String xRequestId) {

    return Mono.just(provincialLocationDetailsCriteria)
        .transform(
            requestMono ->
                enrichDomainResponse(
                    requestMono, provincialRegistryService::retrieveLocationDetails))
        .map(this::mapProvincialResponseToDomainResponse)
        .doOnSubscribe(
            sub ->
                log.info(
                    String.format(
                        HeaderConstants.LOG_REQUEST_ID,
                        APIConstants.LOCATION_DETAILS_URI,
                        xRequestId)));
  }

  /**
   * User reaching root url will be redirecting to swagger-ui.html. This will avoid whitelabel error
   * page
   *
   * @return ResponseEntity for swagger-ui
   */
  @Hidden
  @GetMapping("/")
  public Mono<ResponseEntity<String>> redirectToRootContext() {
    return Mono.just(new HttpHeaders())
        .doOnNext(header -> header.add("Location", "/swagger-ui.html"))
        .map(header -> new ResponseEntity<>(null, header, HttpStatus.MOVED_PERMANENTLY));
  }
  /**
   * Utility method to transform and enrich Domain Response
   *
   * @param requestSource the request source
   * @param responseTransformation the transformation operation
   * @return decorated transformation
   */
  private <T, R extends ProvincialResponse> Mono<R> enrichDomainResponse(
      Mono<T> requestSource, Function<T, Mono<R>> responseTransformation) {
    return Mono.deferContextual(
        context -> {
          var provincialRequestControl = context.get(ProvincialRequestControl.class);
          return requestSource
              .flatMap(responseTransformation)
              .flatMap( // TODO: this code will change when HL7V3 replace with Domain Response
                  response -> {
                    response
                        .getProvincialResponseAcknowledgement()
                        .getOperationOutcome()
                        .setMessageProcess(context.getOrDefault(MessageProcess.class, null));
                    response
                        .getProvincialResponseAcknowledgement()
                        .getAuditEvent()
                        .setProvincialRequestControl(provincialRequestControl);
                    return Mono.just(response);
                  });
        });
  }

  /**
   * Method to map provincial Response to domain Response
   *
   * @param provincialResponse {@link ProvincialResponse}
   * @return mapped domainResponse {@link DomainResponse}
   */
  private <T extends ProvincialResponse> DomainResponse<T> mapProvincialResponseToDomainResponse(
      T provincialResponse) {
    var operationOutcome =
        provincialResponse.getProvincialResponseAcknowledgement().getOperationOutcome();
    provincialResponse.setProvincialResponseAcknowledgement(null);
    return DomainResponse.<T>builder()
        .operationOutcomes(List.of(operationOutcome))
        .result(provincialResponse)
        .build();
  }
}
