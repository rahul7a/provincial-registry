package com.lblw.vphx.phms.common.internal.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.constants.GQLConstants;
import com.lblw.vphx.phms.common.exceptions.InternalAPIClientException;
import com.lblw.vphx.phms.common.exceptions.InternalProcessorException;
import com.lblw.vphx.phms.common.internal.client.InternalAPIClient;
import com.lblw.vphx.phms.common.internal.gql.GraphQLRequest;
import com.lblw.vphx.phms.common.internal.gql.GraphQLResponse;
import com.lblw.vphx.phms.common.internal.gql.GraphQLUtils;
import com.lblw.vphx.phms.common.security.services.OAuthClientService;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.medication.Medication;
import com.lblw.vphx.phms.domain.patient.Patient;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacist;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.prescriber.Prescriber;
import com.lblw.vphx.phms.domain.prescription.Prescription;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Primary
public class InternalLookupServiceImpl implements InternalLookupService {
  private final GraphQLUtils graphQLUtils;
  private final InternalAPIClient apiClient;
  private final OAuthClientService oAuthClientService;
  /**
   * public constructor
   *
   * @param apiClient {@link InternalAPIClient}
   * @param graphQLUtils {@link GraphQLUtils}
   * @param oAuthClientService {@link OAuthClientService}
   */
  public InternalLookupServiceImpl(
      InternalAPIClient apiClient,
      GraphQLUtils graphQLUtils,
      OAuthClientService oAuthClientService) {
    this.apiClient = apiClient;
    this.graphQLUtils = graphQLUtils;
    this.oAuthClientService = oAuthClientService;
  }

  @Override
  public Mono<Pharmacy> fetchPharmacy(@NonNull String pharmacyId) {
    return oAuthClientService
        .getJWT()
        .flatMap(
            jwt -> {
              String graphQLQuery =
                  graphQLUtils.getSchemaFromFileName(
                      CommonConstants.GET_PHARMACY_PROV_DATA_BY_PHARMACY_ID);
              String graphQLVariables =
                  graphQLUtils
                      .getSchemaFromFileName(
                          CommonConstants.GET_PHARMACY_PROV_DATA_BY_PHARMACY_ID_QUERY_VARIABLE)
                      .replace(CommonConstants.FETCH_PHARMACY_ID, pharmacyId);
              GraphQLRequest request =
                  GraphQLRequest.builder()
                      .query(graphQLQuery)
                      .variables(graphQLVariables)
                      .responseClass(GraphQLResponse.class)
                      .build();
              request.setAuthToken(CommonConstants.BEARER.concat(jwt));

              return apiClient
                  .postGQL(request)
                  .map(
                      response -> {
                        JsonNode pharmacyNode =
                            graphQLUtils.parseData(
                                response
                                    .getData()
                                    .get(
                                        CommonConstants.STORE_PHARMACY_PROVINCIAL_DATA_PHARMACY_ID),
                                JsonNode.class);
                        var pharmacy = graphQLUtils.parseData(pharmacyNode, Pharmacy.class);
                        if (pharmacy == null) {
                          log.info("Pharmacy data does not exist for {}", pharmacyId);
                          throw new InternalProcessorException(
                              String.format(
                                  ErrorConstants.EXCEPTION_WHILE_FETCHING_PHARMACY_DETAILS,
                                  pharmacyId));
                        }
                        pharmacy.setName(pharmacy.getProvincialLocation().getName());
                        pharmacy
                            .getProvincialLocation()
                            .setLocationType(
                                Pharmacy.LocationType.builder()
                                    .province(buildProvince(pharmacyNode))
                                    .build());

                        return pharmacy;
                      })
                  .onErrorMap(
                      cause -> {
                        var errorMessage =
                            String.format(
                                ErrorConstants.EXCEPTION_WHILE_FETCHING_PHARMACY_DETAILS,
                                pharmacyId);
                        log.error(errorMessage);
                        return new InternalProcessorException(errorMessage, cause);
                      });
            });
  }
  /**
   * buildProvince from systemPharmacyNode {@link JsonNode}
   *
   * @param systemPharmacyNode System Pharmacy Json Node
   * @return Province {@link Province}
   */
  private Province buildProvince(JsonNode systemPharmacyNode) {
    if (Objects.isNull(systemPharmacyNode)
        || Objects.isNull(systemPharmacyNode.get(CommonConstants.PROVINCIAL_LOCATION))
        || Objects.isNull(
            systemPharmacyNode
                .get(CommonConstants.PROVINCIAL_LOCATION)
                .get(CommonConstants.LOCATION_TYPE))
        || Objects.isNull(
            systemPharmacyNode
                .get(CommonConstants.PROVINCIAL_LOCATION)
                .get(CommonConstants.LOCATION_TYPE)
                .get(CommonConstants.PROVINCE_CODE))) {
      return null;
    }
    return Province.valueOf(
        systemPharmacyNode
            .get(CommonConstants.PROVINCIAL_LOCATION)
            .get(CommonConstants.LOCATION_TYPE)
            .get(CommonConstants.PROVINCE_CODE)
            .asText());
  }

  @Override
  public Prescriber fetchPrescriber(String prescriberId) {
    return null;
  }

  @Override
  public Patient fetchPatient(String patientId) {
    return null;
  }

  @Override
  public Medication fetchMedication(String medicationId) {
    return null;
  }

  @Override
  public Prescription fetchPrescription(String prescriptionId) {
    return null;
  }

  @Override
  public Mono<PrescriptionTransaction> fetchPrescriptionTransaction(
      String prescriptionTransactionId) {
    return Mono.deferContextual(
        context -> {
          final String query =
              graphQLUtils.getSchemaFromFileName(CommonConstants.PRESCRIPTION_TRANSACTION_QUERY);
          final String variables =
              graphQLUtils
                  .getSchemaFromFileName(CommonConstants.PRESCRIPTION_TRANSACTION_QUERY_VARIABLE)
                  .replace(CommonConstants.TXN_ID, prescriptionTransactionId);
          GraphQLRequest request =
              GraphQLRequest.builder()
                  .query(query)
                  .variables(variables)
                  .responseClass(GraphQLResponse.class)
                  .build();
          request.setAuthToken(context.get(HttpHeaders.AUTHORIZATION));

          return apiClient
              .postGQL(request)
              .map(
                  response -> {
                    PrescriptionTransaction transaction =
                        graphQLUtils.parseData(
                            response.getData().get(CommonConstants.PRESCRIPTION_TRANSACTION),
                            PrescriptionTransaction.class);
                    transaction.getPrescription().setPrescriberProvincialLocationName("mock");
                    return transaction;
                  })
              .onErrorMap(
                  cause -> {
                    var errorMessage =
                        String.format(
                            GQLConstants.GQL_DATA_FETCHING_ERROR,
                            CommonConstants.PRESCRIPTION_TRANSACTION,
                            cause.getMessage());
                    log.error(errorMessage);
                    return new InternalProcessorException(errorMessage, cause);
                  });
        });
  }

  @Override
  public Map<String, Object> retrieveStoreObjects(String pharmacyId) {
    return new HashMap<>();
  }

  @Override
  public Mono<Pharmacist> fetchSupervisingPharmacist(
      String pharmacyId, String workstationHostname) {
    return Mono.deferContextual(
        context -> {
          final String query =
              graphQLUtils.getSchemaFromFileName(
                  CommonConstants
                      .POST_SUPERVISING_PHARMACIST_BY_PHARMACY_ID_AND_WORKSTATION_HOSTNAME);

          final String variables =
              graphQLUtils
                  .getSchemaFromFileName(
                      CommonConstants
                          .POST_SUPERVISING_PHARMACIST_BY_PHARMACY_ID_AND_WORKSTATION_HOSTNAME_QUERY_VARIABLE)
                  .replace(CommonConstants.FETCH_PHARMACY_ID, pharmacyId)
                  .replace(CommonConstants.WORKSTATION_HOSTNAME_ID, workstationHostname);

          GraphQLRequest graphQLRequest =
              GraphQLRequest.builder()
                  .query(query)
                  .variables(variables)
                  .responseClass(GraphQLResponse.class)
                  .build();
          graphQLRequest.setAuthToken(context.get(HttpHeaders.AUTHORIZATION));

          return apiClient
              .postGQL(graphQLRequest)
              .map(
                  response -> {
                    if (response.getErrors() != null) {
                      var errorMessage =
                          String.format(
                              GQLConstants.GQL_DATA_FETCHING_ERROR,
                              CommonConstants.STORE_POST_SUPERVISING_PHARMACIST,
                              graphQLUtils.convertToString(response.getErrors()));
                      log.error(errorMessage);
                      throw new InternalAPIClientException(errorMessage);
                    }
                    return graphQLUtils.parseData(
                            response
                                    .getData()
                                    .get(CommonConstants.STORE_POST_SUPERVISING_PHARMACIST),
                            Pharmacist.class);
                  })
              .onErrorMap(
                  cause -> {
                    var errorMessage =
                        String.format(
                            GQLConstants.GQL_DATA_FETCHING_ERROR,
                            CommonConstants.STORE_POST_SUPERVISING_PHARMACIST,
                            cause.getMessage());
                    log.error(errorMessage);
                    return new InternalProcessorException(errorMessage, cause);
                  });
        });
  }

  /**
   * This Method is used to retrieve information of default pharmacist using Pharmacy Id via graphql
   *
   * @param pharmacyId {@link String}
   * @return {@link Pharmacist}
   */
  @Override
  public Mono<Pharmacist> fetchDefaultPharmacist(String pharmacyId) {
    return Mono.deferContextual(
        context -> {
          final String query =
              graphQLUtils.getSchemaFromFileName(
                  CommonConstants.GET_DEFAULT_PHARMACIST_BY_PHARMACY_ID);

          final String variables =
              graphQLUtils
                  .getSchemaFromFileName(
                      CommonConstants.GET_DEFAULT_PHARMACIST_BY_PHARMACY_ID_QUERY_VARIABLE)
                  .replace(CommonConstants.FETCH_PHARMACY_ID, pharmacyId);

          GraphQLRequest graphQLRequest =
              GraphQLRequest.builder()
                  .query(query)
                  .variables(variables)
                  .responseClass(GraphQLResponse.class)
                  .build();
          graphQLRequest.setAuthToken(context.get(HttpHeaders.AUTHORIZATION));

          return apiClient
              .postGQL(graphQLRequest)
              .map(
                  response -> {
                    if (response.getErrors() != null) {
                      var errorMessage =
                          String.format(
                              GQLConstants.GQL_DATA_FETCHING_ERROR,
                              CommonConstants.STORE_GET_DEFAULT_PHARMACIST,
                              graphQLUtils.convertToString(response.getErrors()));
                      log.error(errorMessage);
                      throw new InternalAPIClientException(errorMessage);
                    }
                    return graphQLUtils.parseData(
                        response.getData().get(CommonConstants.STORE_GET_DEFAULT_PHARMACIST),
                        Pharmacist.class);
                  })
              .onErrorMap(
                  cause -> {
                    var errorMessage =
                        String.format(
                            GQLConstants.GQL_DATA_FETCHING_ERROR,
                            CommonConstants.STORE_GET_DEFAULT_PHARMACIST,
                            cause.getMessage());
                    log.error(errorMessage);
                    return new InternalProcessorException(errorMessage, cause);
                  });
        });
  }
}
