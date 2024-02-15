package com.lblw.vphx.phms.common.internal.services;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.constants.GQLConstants;
import com.lblw.vphx.phms.common.exceptions.InternalProcessorException;
import com.lblw.vphx.phms.common.internal.client.InternalAPIClient;
import com.lblw.vphx.phms.common.internal.gql.GraphQLRequest;
import com.lblw.vphx.phms.common.internal.gql.GraphQLResponse;
import com.lblw.vphx.phms.common.internal.gql.GraphQLUtils;
import com.lblw.vphx.phms.common.internal.gql.parser.deserializer.PrescriptionTransactionDeserializer;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.DateFormat;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.patient.Patient;
import com.lblw.vphx.phms.domain.patient.PatientType;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacist;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class InternalLookupServiceTest {
  @Mock InternalAPIClient internalAPIClient;
  @Mock ProvincialRequestControl provincialRequestControl;
  @Mock GraphQLUtils graphQLUtils;
  @InjectMocks InternalLookupServiceImpl service;
  private ObjectMapper objectMapper;

  private String resourceAsString(String resource) {
    try {
      return IOUtils.toString(this.getClass().getResourceAsStream(resource), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Before
  public void beforeEach() {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Test
  public void
      givenPrescriptionTxIdWithNonEmptyPatient_whenGetPrescriptionTxId_thenReturnPrescriptionTransaction() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse("/mocks/PrescriptionTransaction/success.json");
    var patient =
        Patient.builder()
            .systemId("62a0e365c3a71822a9a7f4ab")
            .provincialIdentifier("1000402014")
            .firstName("Lester")
            .lastName("Keeling")
            .dateOfBirth(
                DateUtils.formatDateToLocalDate("2000-01-01", DateFormat.SYSTEM_DATE_FORMAT))
            .gender(Gender.M)
            .type(PatientType.HUMAN)
            .build();
    ObjectMapper prescriptionTransactionObjectMapper = objectMapper.copy();

    prescriptionTransactionObjectMapper.registerModule(
        new SimpleModule()
            .addDeserializer(
                PrescriptionTransaction.class,
                new PrescriptionTransactionDeserializer(objectMapper)));
    PrescriptionTransaction mockPrescriptionTransaction =
        prescriptionTransactionObjectMapper.convertValue(
            graphQLMockResponse.getData().get(CommonConstants.PRESCRIPTION_TRANSACTION),
            PrescriptionTransaction.class);
    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");
    when(graphQLUtils.parseData(any(), any())).thenReturn(mockPrescriptionTransaction);

    StepVerifier.create(
            service
                .fetchPrescriptionTransaction("txId")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            prescriptionTransaction -> {
              assertEquals(
                  "62a0e374ad23912353ee9371", prescriptionTransaction.getSystemIdentifier());
              // Prescription data
              assertEquals(
                  "62a0e374ad23912353ee9372",
                  prescriptionTransaction.getPrescription().getSystemIdentifier());
              // Prescriber data
              assertEquals(
                  "616ee78eeec0906382d6ff9b",
                  prescriptionTransaction.getPrescription().getPrescriber().getSystemIdentifier());
              // Patient data
              assertEquals(patient, prescriptionTransaction.getPrescription().getPatient());
              // Medication data
              assertEquals("02269080", prescriptionTransaction.getMedication().getDin());
              assertEquals(3, prescriptionTransaction.getMedication().getIngredients().size());
              assertEquals(
                  "56326737",
                  prescriptionTransaction
                      .getMedication()
                      .getIngredients()
                      .get(0)
                      .getMedication()
                      .getDin());
              assertEquals(
                  "02269080",
                  prescriptionTransaction
                      .getMedication()
                      .getIngredients()
                      .get(1)
                      .getMedication()
                      .getDin());
              assertEquals(
                  "03269080",
                  prescriptionTransaction
                      .getMedication()
                      .getIngredients()
                      .get(2)
                      .getMedication()
                      .getDin());
            })
        .verifyComplete();
  }

  @Test
  public void
      givenPrescriptionTxIdWithEmptyPatient_whenGetPrescriptionTxId_thenReturnPrescriptionTransaction() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse(
            "/mocks/PrescriptionTransaction/patient_is_null.json");

    ObjectMapper prescriptionTransactionObjectMapper = objectMapper.copy();

    prescriptionTransactionObjectMapper.registerModule(
        new SimpleModule()
            .addDeserializer(
                PrescriptionTransaction.class,
                new PrescriptionTransactionDeserializer(objectMapper)));
    PrescriptionTransaction mockPrescriptionTransaction =
        prescriptionTransactionObjectMapper.convertValue(
            graphQLMockResponse.getData().get(CommonConstants.PRESCRIPTION_TRANSACTION),
            PrescriptionTransaction.class);
    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");
    when(graphQLUtils.parseData(any(), any())).thenReturn(mockPrescriptionTransaction);
    StepVerifier.create(
            service
                .fetchPrescriptionTransaction("txId")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            prescriptionTransaction -> {
              assertEquals(
                  "62a0e374ad23912353ee9371", prescriptionTransaction.getSystemIdentifier());
              // Prescription data
              assertEquals(
                  "62a0e374ad23912353ee9372",
                  prescriptionTransaction.getPrescription().getSystemIdentifier());
              // Prescriber data
              assertEquals(
                  "616ee78eeec0906382d6ff9b",
                  prescriptionTransaction.getPrescription().getPrescriber().getSystemIdentifier());
              // Patient data
              assertNull(prescriptionTransaction.getPrescription().getPatient());
              // Medication data
              assertEquals("02269080", prescriptionTransaction.getMedication().getDin());
              assertNull(prescriptionTransaction.getMedication().getIngredients());
            })
        .verifyComplete();
  }

  @Test
  public void
      givenPrescriptionTxIdEmptyMedication_whenGetPrescriptionTxId_thenReturnPrescriptionTransaction() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse(
            "/mocks/PrescriptionTransaction/medication_is_null.json");
    var patient =
        Patient.builder()
            .systemId("6377dff9f3d21a1f19daadc8")
            .provincialIdentifier("1900402014")
            .firstName("Hanns")
            .lastName("Patie")
            .dateOfBirth(
                DateUtils.formatDateToLocalDate("1978-11-30", DateFormat.SYSTEM_DATE_FORMAT))
            .gender(Gender.M)
            .type(PatientType.HUMAN)
            .build();
    ObjectMapper prescriptionTransactionObjectMapper = objectMapper.copy();

    prescriptionTransactionObjectMapper.registerModule(
        new SimpleModule()
            .addDeserializer(
                PrescriptionTransaction.class,
                new PrescriptionTransactionDeserializer(objectMapper)));
    PrescriptionTransaction mockPrescriptionTransaction =
        prescriptionTransactionObjectMapper.convertValue(
            graphQLMockResponse.getData().get(CommonConstants.PRESCRIPTION_TRANSACTION),
            PrescriptionTransaction.class);
    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");
    when(graphQLUtils.parseData(any(), any())).thenReturn(mockPrescriptionTransaction);

    StepVerifier.create(
            service
                .fetchPrescriptionTransaction("txId")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            prescriptionTransaction -> {
              assertEquals(
                  "6377f1d87f29ed06c77eace5", prescriptionTransaction.getSystemIdentifier());
              // Prescription data
              assertEquals(
                  "6377f1d87f29ed06c77eace6",
                  prescriptionTransaction.getPrescription().getSystemIdentifier());
              // Prescriber data
              assertEquals(
                  "63201c1dcfc7b5fddef12208",
                  prescriptionTransaction.getPrescription().getPrescriber().getSystemIdentifier());
              // Patient data
              assertEquals(patient, prescriptionTransaction.getPrescription().getPatient());
              // Medication data is expected to be null
              assertNull(prescriptionTransaction.getMedication());
            })
        .verifyComplete();
  }

  @Test
  public void givenPrescriptionTxId_whenGetPrescriptionTxFailed_thenThrowException() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse("/mocks/PrescriptionTransaction/error.json");
    ObjectMapper prescriptionTransactionObjectMapper = objectMapper.copy();

    prescriptionTransactionObjectMapper.registerModule(
        new SimpleModule()
            .addDeserializer(
                PrescriptionTransaction.class,
                new PrescriptionTransactionDeserializer(objectMapper)));
    PrescriptionTransaction mockPrescriptionTransaction = PrescriptionTransaction.builder().build();
    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");
    // when(graphQLUtils.parseData(any(), any())).thenReturn(mockPrescriptionTransaction);
    StepVerifier.create(
            service
                .fetchPrescriptionTransaction("txId")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .verifyErrorMatches(
            e ->
                e instanceof RuntimeException
                    && e.getMessage()
                        .equals(
                            String.format(
                                GQLConstants.GQL_DATA_FETCHING_ERROR,
                                CommonConstants.PRESCRIPTION_TRANSACTION,
                                graphQLUtils.convertToString(graphQLMockResponse.getErrors()))));
  }

  // This method is used to create mock response from json path
  private GraphQLResponse buildPrescriptionTransactionMockResponse(String jsonDataPath) {
    String mockData = resourceAsString(jsonDataPath);
    try {
      return objectMapper.convertValue(objectMapper.readTree(mockData), GraphQLResponse.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void
      givenPharmacistIdWithNonEmptyWorkstationHostname_thenReturnSupervisingPharmacistDetails() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse(
            "/mocks/Pharmacist/supervising_pharmacist_success.json");
    ObjectMapper pharmacistObjectMapper = objectMapper.copy();

    Pharmacist mockPharmacist =
        pharmacistObjectMapper.convertValue(
            graphQLMockResponse.getData().get(CommonConstants.STORE_POST_SUPERVISING_PHARMACIST),
            Pharmacist.class);
    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");
    when(graphQLUtils.parseData(any(), any())).thenReturn(mockPharmacist);

    StepVerifier.create(
            service
                .fetchSupervisingPharmacist("fetchPharmacyId", "workstationHostnameId")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            supervisingPharmacist -> {
              // Pharmacist data
              assertEquals("pharmacistFirstName", supervisingPharmacist.getFirstName());
              assertEquals("pharmacistLastName", supervisingPharmacist.getLastName());
              assertEquals("userId", supervisingPharmacist.getIdpUserId());
              assertEquals("123456789", supervisingPharmacist.getLicenceNumber());
              assertEquals("QC", supervisingPharmacist.getLicenceProvince());
              assertEquals("Active", supervisingPharmacist.getState());
              assertEquals(true, supervisingPharmacist.isActive());
              assertEquals(
                  "CPN.01001976.QC.PRS",
                  supervisingPharmacist.getProvincialProvider().getIdentifier().getValue());
              assertEquals(
                  "3310000",
                  supervisingPharmacist.getProvincialProvider().getProviderRole().getCode());
            })
        .verifyComplete();
  }

  @Test
  public void
      givenPharmacistIdWithEmptyWorkstationHostname_thenReturnNullSupervisingPharmacistDetails() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse(
            "/mocks/Pharmacist/supervising_pharmacist_failure.json");
    ObjectMapper pharmacistObjectMapper = objectMapper.copy();

    Pharmacist mockPharmacist =
        pharmacistObjectMapper.convertValue(
            graphQLMockResponse.getData().get(CommonConstants.STORE_POST_SUPERVISING_PHARMACIST),
            Pharmacist.class);
    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");
    when(graphQLUtils.parseData(any(), any())).thenReturn(mockPharmacist);

    StepVerifier.create(
            service
                .fetchSupervisingPharmacist("fetchPharmacyId", "")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            supervisingPharmacist -> {
              // Pharmacist data
              assertEquals(null, supervisingPharmacist.getFirstName());
              assertEquals(null, supervisingPharmacist.getLastName());
              assertEquals(null, supervisingPharmacist.getIdpUserId());
              assertEquals(false, supervisingPharmacist.isActive());
            })
        .verifyComplete();
  }

  @Test
  public void givenPharmacistId_thenReturnDefaultPharmacistDetails() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse(
            "/mocks/Pharmacist/default_pharmacist_success.json");
    ObjectMapper pharmacistObjectMapper = objectMapper.copy();

    Pharmacist mockPharmacist =
        pharmacistObjectMapper.convertValue(
            graphQLMockResponse.getData().get(CommonConstants.STORE_GET_DEFAULT_PHARMACIST),
            Pharmacist.class);
    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");
    when(graphQLUtils.parseData(any(), any())).thenReturn(mockPharmacist);

    StepVerifier.create(
            service
                .fetchDefaultPharmacist("fetchPharmacyId")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            defaultPharmacist -> {
              // Pharmacist data
              assertEquals("account6162", defaultPharmacist.getFirstName());
              assertEquals("new1662", defaultPharmacist.getLastName());
              assertEquals(
                  "12106a08-dab7-4be5-a836-ece43873e727", defaultPharmacist.getIdpUserId());
              assertEquals("321", defaultPharmacist.getLicenceNumber());
              assertEquals("QC", defaultPharmacist.getLicenceProvince());
              assertEquals("ACTIVE", defaultPharmacist.getState());
              assertEquals(
                  "mainPlaceIndicator",
                  defaultPharmacist.getProvincialProvider().getMainPlaceIndicator());
            })
        .verifyComplete();
  }

  @Test
  public void givenPharmacistId_thenReturnNullDefaultPharmacistDetails() {
    GraphQLResponse graphQLMockResponse =
        buildPrescriptionTransactionMockResponse(
            "/mocks/Pharmacist/default_pharmacist_failure.json");
    ObjectMapper pharmacistObjectMapper = objectMapper.copy();

    when(internalAPIClient.postGQL(Mockito.any(GraphQLRequest.class)))
        .thenReturn(Mono.just(graphQLMockResponse));
    when(graphQLUtils.getSchemaFromFileName(any())).thenReturn("schema");

    StepVerifier.create(
            service
                .fetchDefaultPharmacist("fetchPharmacyId")
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .verifyErrorMatches(e -> e instanceof InternalProcessorException);
  }
}
