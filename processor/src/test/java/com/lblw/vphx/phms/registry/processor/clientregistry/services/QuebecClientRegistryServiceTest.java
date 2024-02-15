package com.lblw.vphx.phms.registry.processor.clientregistry.services;

import static org.mockito.Mockito.*;

import com.lblw.vphx.phms.common.logs.MessageLogger;
import com.lblw.vphx.phms.common.province.client.ProvincialWebClient;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
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
import com.lblw.vphx.phms.registry.processor.helpers.ProvincialResponseRuleEngineHelper;
import com.lblw.vphx.phms.transformation.services.TransformationService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@SpringBootTest(classes = {QuebecClientRegistryService.class, ProvincialRequestProperties.class})
@EnableConfigurationProperties(value = ProvincialRequestProperties.class)
@ActiveProfiles("test")
class QuebecClientRegistryServiceTest {
  @Autowired QuebecClientRegistryService quebecClientRegistryService;
  @MockBean TransformationService transformationService;

  @Autowired ProvincialRequestProperties provincialRequestProperties;

  @MockBean MessageLogger messageLogger;
  @MockBean ProvincialWebClient provincialWebClient;

  @MockBean ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper;

  @BeforeEach
  void beforeEach() {
    Answer<Response<? extends ResponseControlAct, ? extends ProvincialResponse>> identityAnswer =
        answer -> {
          var response = (Response) answer.getArgument(0);
          response
              .getProvincialResponsePayload()
              .setProvincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .operationOutcome(OperationOutcome.builder().build())
                      .build());
          return response;
        };
    when(provincialResponseRuleEngineHelper.validatePatientSearchResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateConsentSearchResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateLocationDetailsResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateLocationSearchResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateProviderSearchResponse(any()))
        .thenAnswer(identityAnswer);
  }

  @Test
  void searchProvincialPatientTest() {
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        ProvincialPatientSearchRequest.builder().build();

    Mono<ProvincialPatientSearchResponse> provincialPatientSearchResponseMono =
        quebecClientRegistryService
            .searchProvincialPatient(provincialPatientSearchRequest)
            .contextWrite(Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

    when(transformationService.transformRequest(Mockito.any())).thenReturn(Mono.just("RawRequest"));

    when(provincialWebClient.callClientRegistry(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Mono.just(ResponseEntity.ok("rawResponse")));

    when(transformationService.transformResponse("rawResponse"))
        .thenReturn(
            Mono.just(
                ProvincialPatientSearchResponse.builder()
                    .provincialResponsePayload(ProvincialPatientProfile.builder().build())
                    .build()));

    StepVerifier.create(provincialPatientSearchResponseMono)
        .assertNext(
            response -> {
              Assertions.assertEquals(ProvincialPatientSearchResponse.builder().build(), response);
                verify(transformationService, times(1)).transformRequest(Mockito.any());
                verify(transformationService, times(1)).transformResponse("rawResponse");
            })
        .verifyComplete();
  }

  @Test
  void searchProvincialProviderTest() {
    ProvincialProviderSearchRequest provincialProviderSearchRequest =
        ProvincialProviderSearchRequest.builder().build();

    Mono<ProvincialProviderSearchResponse> provincialProviderSearchResponseMono =
        quebecClientRegistryService
            .searchProvincialProvider(provincialProviderSearchRequest)
            .contextWrite(Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH));

    when(transformationService.transformRequest(Mockito.any())).thenReturn(Mono.just("RawRequest"));

    when(provincialWebClient.callClientRegistry(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Mono.just(ResponseEntity.ok("rawResponse")));

    when(transformationService.transformResponse("rawResponse"))
        .thenReturn(
            Mono.just(
                ProvincialProviderSearchResponse.builder()
                    .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                    .build()));

    StepVerifier.create(provincialProviderSearchResponseMono)
        .assertNext(
            response -> {
              Assertions.assertEquals(ProvincialProviderSearchResponse.builder().build(), response);
                verify(transformationService, times(1)).transformRequest(Mockito.any());
                verify(transformationService, times(1)).transformResponse("rawResponse");
            })
        .verifyComplete();
  }

  @Test
  void getProvincialPatientConsentTest() {
    ProvincialPatientConsentRequest provincialPatientConsentRequest =
        ProvincialPatientConsentRequest.builder().build();

    Mono<ProvincialPatientConsentResponse> provincialPatientConsentResponseMono =
        quebecClientRegistryService
            .getProvincialPatientConsent(provincialPatientConsentRequest)
            .contextWrite(Context.of(MessageProcess.class, MessageProcess.PATIENT_CONSENT));

    when(transformationService.transformRequest(Mockito.any())).thenReturn(Mono.just("RawRequest"));

    when(provincialWebClient.callClientRegistry(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Mono.just(ResponseEntity.ok("rawResponse")));

    when(transformationService.transformResponse("rawResponse"))
        .thenReturn(
            Mono.just(
                ProvincialPatientConsentResponse.builder()
                    .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                    .build()));

    StepVerifier.create(provincialPatientConsentResponseMono)
        .assertNext(
            response -> {
              Assertions.assertEquals(ProvincialPatientConsentResponse.builder().build(), response);
              verify(transformationService, times(1)).transformRequest(Mockito.any());
              verify(transformationService, times(1)).transformResponse("rawResponse");
            })
        .verifyComplete();
  }

  @Test
  void searchProvincialLocationSummaryTest() {
    ProvincialLocationSearchRequest provincialLocationSearchRequest =
        ProvincialLocationSearchRequest.builder().build();

    Mono<ProvincialLocationSearchResponse> provincialLocationSearchResponseMono =
        quebecClientRegistryService
            .searchProvincialLocationSummary(provincialLocationSearchRequest)
            .contextWrite(Context.of(MessageProcess.class, MessageProcess.LOCATION_SEARCH));

    when(transformationService.transformRequest(Mockito.any())).thenReturn(Mono.just("RawRequest"));

    when(provincialWebClient.callClientRegistry(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Mono.just(ResponseEntity.ok("rawResponse")));

    when(transformationService.transformResponse("rawResponse"))
        .thenReturn(
            Mono.just(
                ProvincialLocationSearchResponse.builder()
                    .provincialResponsePayload(
                        ProvincialLocationSummaries.builder()
                            .provincialLocations(List.of())
                            .build())
                    .build()));

    StepVerifier.create(provincialLocationSearchResponseMono)
        .assertNext(
            response -> {
              Assertions.assertEquals(ProvincialLocationSearchResponse.builder().build(), response);
              verify(transformationService, times(1)).transformRequest(Mockito.any());
              verify(transformationService, times(1)).transformResponse("rawResponse");
            })
        .verifyComplete();
  }

  @Test
  void searchProvincialLocationDetailsTest() {
    ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
        ProvincialLocationDetailsRequest.builder().build();

    Mono<ProvincialLocationDetailsResponse> provincialLocationDetailsResponseMono =
        quebecClientRegistryService
            .searchProvincialLocationDetails(provincialLocationDetailsRequest)
            .contextWrite(Context.of(MessageProcess.class, MessageProcess.LOCATION_DETAILS));

    when(transformationService.transformRequest(Mockito.any())).thenReturn(Mono.just("RawRequest"));

    when(provincialWebClient.callClientRegistry(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Mono.just(ResponseEntity.ok("rawResponse")));

    when(transformationService.transformResponse("rawResponse"))
        .thenReturn(
            Mono.just(
                ProvincialLocationDetailsResponse.builder()
                    .provincialResponsePayload(ProvincialLocationDetails.builder().build())
                    .build()));

    StepVerifier.create(provincialLocationDetailsResponseMono)
        .assertNext(
            response -> {
              Assertions.assertEquals(
                  ProvincialLocationDetailsResponse.builder().build(), response);
              verify(transformationService, times(1)).transformRequest(Mockito.any());
              verify(transformationService, times(1)).transformResponse("rawResponse");
            })
        .verifyComplete();
  }
}
