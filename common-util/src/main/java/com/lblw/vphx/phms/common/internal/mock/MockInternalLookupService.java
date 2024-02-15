package com.lblw.vphx.phms.common.internal.mock;

import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.internal.services.InternalLookupService;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SigDescription;
import com.lblw.vphx.phms.domain.medication.*;
import com.lblw.vphx.phms.domain.patient.Patient;
import com.lblw.vphx.phms.domain.patient.PatientType;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacist;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.prescriber.Prescriber;
import com.lblw.vphx.phms.domain.prescription.Prescription;
import com.lblw.vphx.phms.domain.prescription.PrescriptionSig;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** This Class Mocks the pharmacy information */
@Component
public class MockInternalLookupService implements InternalLookupService {

  private final ProvincialRequestProperties.Security security;
  private final ResourceLoader resourceLoader;

  Map<String, Pharmacy> locationDTOMap = new HashMap<>();
  Map<String, Prescriber> prescriberDTOMap = new HashMap<>();
  Map<String, Medication> medicationDTOMap = new HashMap<>();
  Map<String, Patient> patientDTOMap = new HashMap<>();
  Map<String, Prescription> prescriptionDTOMap = new HashMap<>();
  Map<String, PrescriptionTransaction> prescriptionTransactionDTOMap = new HashMap<>();

  /**
   * Public Constructor. Dependency on Spring's ResourceLoader for loading files from classpath
   * context
   *
   * @param provincialRequestProperties {@link ProvincialRequestProperties}
   * @param resourceLoader {@link ResourceLoader}
   */
  public MockInternalLookupService(
      ProvincialRequestProperties provincialRequestProperties, ResourceLoader resourceLoader) {
    this.security = provincialRequestProperties.getRequest().getSecurity();
    this.resourceLoader = resourceLoader;
  }

  /**
   * This Method is used to retrieve information about medication using system ID
   *
   * @param medicationId {@link String}
   * @return {@link Medication}
   */
  @Override
  public Medication fetchMedication(String medicationId) {
    if (medicationId == null) {
      return null;
    }
    return medicationDTOMap.get(medicationId);
  }

  /**
   * Return {@link Pharmacy} by the key pharmacyId if it exists
   *
   * @param pharmacyId The id of the pharmacy you want to search for.
   * @return A {@link Pharmacy} object.
   */
  @Override
  public Mono<Pharmacy> fetchPharmacy(String pharmacyId) {
    if (pharmacyId == null || pharmacyId.isEmpty()) {
      return Mono.empty();
    }
    return Mono.just(locationDTOMap.getOrDefault(pharmacyId, locationDTOMap.get("-1")));
  }

  /**
   * Return {@link Prescriber} by the key prescriberId if it exists
   *
   * @param prescriberId The id of the prescriber you want to search for.
   * @return A {@link Prescriber} object.
   */
  @Nullable
  @Override
  public Prescriber fetchPrescriber(String prescriberId) {
    if (prescriberId == null) {
      return null;
    }
    return prescriberDTOMap.get(prescriberId);
  }

  /**
   * Return {@link Patient} by the key patientId if it exists
   *
   * @param patientId The id of the patient you want to search for.
   * @return A {@link Patient} object.
   */
  @Nullable
  @Override
  public Patient fetchPatient(String patientId) {
    if (patientId == null) {
      return null;
    }
    return patientDTOMap.get(patientId);
  }

  /**
   * Return {@link Prescription} by the key systemPrescriptionId if it exists
   *
   * @param prescriptionId The id of the prescription you want to search for.
   * @return A {@link Prescription} object.
   */
  @Override
  public Prescription fetchPrescription(String prescriptionId) {
    if (prescriptionId == null) {
      return null;
    }

    return prescriptionDTOMap.get(prescriptionId);
  }

  /**
   * Return {@link PrescriptionTransaction} by the key systemPrescriptionTransactionId if it exists
   *
   * @param prescriptionTransactionId The id of the prescriptionTransaction you want to search for.
   * @return A {@link PrescriptionTransaction} object.
   */
  @Override
  public Mono<PrescriptionTransaction> fetchPrescriptionTransaction(
      String prescriptionTransactionId) {
    if (prescriptionTransactionId == null) {
      return Mono.empty();
    }

    return Mono.just(prescriptionTransactionDTOMap.get(prescriptionTransactionId));
  }

  @Override
  public Map<String, Object> retrieveStoreObjects(String pharmacyId)
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {

    KeyStore keyStore =
        getKeyStore(
            security.getCertificate().getDefaultLocation(),
            security.getCertificate().getPassword().toCharArray());

    return Map.ofEntries(
        Map.entry(CommonConstants.PROVINCIAL_CERTIFICATE, keyStore),
        Map.entry(CommonConstants.PASSWORD, security.getCertificate().getPassword()),
        Map.entry(CommonConstants.ALIAS, security.getCertificate().getAlias()));
  }

  @Override
  public Mono<Pharmacist> fetchSupervisingPharmacist(
      String pharmacyId, String workstationHostname) {
    return null;
  }

  @Override
  public Mono<Pharmacist> fetchDefaultPharmacist(String pharmacyId) {
    return null;
  }

  /**
   * Access and return a key store given its file name and password
   *
   * @param keyStoreFileName file name of the key store
   * @param keyStorePassword Password for the key store
   * @return {@link KeyStore} to access
   * @throws KeyStoreException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   */
  private KeyStore getKeyStore(String keyStoreFileName, char[] keyStorePassword)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore keyStore = KeyStore.getInstance(CommonConstants.PKCS_12);
    InputStream stream = getClass().getClassLoader().getResourceAsStream(keyStoreFileName);
    keyStore.load(stream, keyStorePassword);
    return keyStore;
  }

  /**
   * load mock data
   *
   * @throws IOException
   */
  @PostConstruct
  public void loadMockData() throws IOException {
    loadMockData(
        resourceLoader.getResource(
            CommonConstants.DATA_LOADER_MOCK_DATA_BASE_PATH + "pharmacy_dto.csv"),
        this::mapMockLocationDTO);
    loadMockData(
        resourceLoader.getResource(
            CommonConstants.DATA_LOADER_MOCK_DATA_BASE_PATH + "medication_dto.csv"),
        this::mapMockMedicationDTO);
    loadMockData(
        resourceLoader.getResource(
            CommonConstants.DATA_LOADER_MOCK_DATA_BASE_PATH + "prescriber_dto.csv"),
        this::mapMockPrescriberDTO);
    loadMockData(
        resourceLoader.getResource(
            CommonConstants.DATA_LOADER_MOCK_DATA_BASE_PATH + "patient_dto.csv"),
        this::mapMockPatientDTO);
    loadMockData(
        resourceLoader.getResource(
            CommonConstants.DATA_LOADER_MOCK_DATA_BASE_PATH + "prescription_dto.csv"),
        this::mapMockPrescriptionDTO);
    loadMockData(
        resourceLoader.getResource(
            CommonConstants.DATA_LOADER_MOCK_DATA_BASE_PATH + "prescription_transaction_dto.csv"),
        this::mapMockPrescriptionTransactionDTO);
  }

  private void loadMockData(Resource resource, Consumer<? super CSVRecord> action)
      throws IOException {
    final Reader reader =
        new BufferedReader(
            new InputStreamReader(
                new BOMInputStream(resource.getInputStream()), StandardCharsets.UTF_8));

    CSVFormat.DEFAULT.parse(reader).stream().forEach(action);
  }

  private void mapMockLocationDTO(CSVRecord csvRecord) {
    locationDTOMap.put(
        csvRecord.get(0),
        Pharmacy.builder()
            .id(StringUtils.trimToNull(csvRecord.get(1)))
            .name(StringUtils.trimToNull(csvRecord.get(2)))
            .storeNumber(StringUtils.trimToNull(csvRecord.get(3)))
            .provincialLocation(
                Pharmacy.ProvincialLocation.builder()
                    .locationType(
                        Pharmacy.LocationType.builder()
                            .province(Province.valueOf(StringUtils.trimToNull(csvRecord.get(4))))
                            .build())
                    .telecom(
                        Pharmacy.Telecom.builder()
                            .number((StringUtils.trimToNull(csvRecord.get(5))))
                            .build())
                    .address(
                        Pharmacy.Address.builder()
                            .streetAddressLine1(StringUtils.trimToNull(csvRecord.get(6)))
                            .streetAddressLine2(StringUtils.trimToNull(csvRecord.get(7)))
                            .city(StringUtils.trimToNull(csvRecord.get(8)))
                            .country(StringUtils.trimToNull(csvRecord.get(9)))
                            .postalCode(StringUtils.trimToNull(csvRecord.get(10)))
                            .build())
                    .build())
            .build());
  }

  private void mapMockPatientDTO(CSVRecord csvRecord) {
    String ordinalPatientType = StringUtils.trimToNull(csvRecord.get(6));
    patientDTOMap.put(
        csvRecord.get(0),
        Patient.builder()
            .systemId(StringUtils.trimToNull((csvRecord.get(0))))
            .provincialIdentifier(StringUtils.trimToNull((csvRecord.get(1))))
            .firstName(StringUtils.trimToNull((csvRecord.get(2))))
            .lastName(StringUtils.trimToNull((csvRecord.get(3))))
            .dateOfBirth(DateUtils.parseSystemDateFormatToLocalDate(csvRecord.get(4)))
            .gender(Gender.valueOf(StringUtils.trimToNull(csvRecord.get(5))))
            .type(
                ordinalPatientType != null
                    ? PatientType.toPatientType(Integer.valueOf(ordinalPatientType))
                    : null)
            .build());
  }

  private void mapMockMedicationDTO(CSVRecord csvRecord) {
    var medicationDTO =
        Medication.builder()
            .type(MedicationType.valueOf(StringUtils.trimToNull(csvRecord.get(1))))
            .din(StringUtils.trimToNull(csvRecord.get(2)))
            .category(StringUtils.trimToNull(csvRecord.get(3)))
            .tradeName(StringUtils.trimToNull(csvRecord.get(4)))
            .routeOfAdministration(StringUtils.trimToNull(csvRecord.get(5)))
            .form(StringUtils.trimToNull(csvRecord.get(6)))
            .ingredients(new ArrayList<>())
            .systemIdentifier(StringUtils.trimToNull(csvRecord.get(0)))
            .upc(StringUtils.trimToNull(csvRecord.get(13)))
            .tradeNameEnglish(StringUtils.trimToNull(csvRecord.get(4)))
            .tradeNameFrench(StringUtils.trimToNull(csvRecord.get(4)))
            .compoundType(CompoundType.builder().code(csvRecord.get(14)).build())
            .uom(UOM.builder().code(csvRecord.get(15)).build())
            .packSize(
                StringUtils.trimToNull(csvRecord.get(16)) != null
                    ? PackSize.builder()
                        .value(Double.parseDouble(StringUtils.trimToNull(csvRecord.get(16))))
                        .build()
                    : null)
            .build();

    var ingredients = medicationDTO.getIngredients();

    if (StringUtils.trimToNull(csvRecord.get(7)) != null) {
      ingredients.add(
          MedicationIngredient.builder()
              .medication(
                  Medication.builder()
                      .type(MedicationType.valueOf(StringUtils.trimToNull(csvRecord.get(7))))
                      .tradeName(StringUtils.trimToNull(csvRecord.get(8)))
                      .din(StringUtils.trimToNull(csvRecord.get(9)))
                      .build())
              .build());
    }

    if (StringUtils.trimToNull(csvRecord.get(10)) != null) {
      ingredients.add(
          MedicationIngredient.builder()
              .medication(
                  Medication.builder()
                      .type(MedicationType.valueOf(StringUtils.trimToNull(csvRecord.get(10))))
                      .tradeName(StringUtils.trimToNull(csvRecord.get(11)))
                      .din(StringUtils.trimToNull(csvRecord.get(12)))
                      .build())
              .build());
    }

    medicationDTOMap.put(StringUtils.trimToNull(csvRecord.get(0)), medicationDTO);
  }

  private void mapMockPrescriberDTO(CSVRecord csvRecord) {

    prescriberDTOMap.put(
        csvRecord.get(0),
        Prescriber.builder()
            .systemIdentifier(StringUtils.trimToNull((csvRecord.get(0))))
            .provincialIdentifier(StringUtils.trimToNull(csvRecord.get(1)))
            .firstName(StringUtils.trimToNull(csvRecord.get(2)))
            .lastName(StringUtils.trimToNull(csvRecord.get(3)))
            .licenseNumber(StringUtils.trimToNull(csvRecord.get(4)))
            .prescriberTypeCode(StringUtils.trimToNull(csvRecord.get(5)))
            .licensingProvince(StringUtils.trimToNull(csvRecord.get(6)))
            .activeFlag(Boolean.valueOf(StringUtils.trimToNull(csvRecord.get(7))))
            .build());
  }

  private void mapMockPrescriptionTransactionDTO(CSVRecord csvRecord) {
    prescriptionTransactionDTOMap.put(
        csvRecord.get(0),
        PrescriptionTransaction.builder()
            .systemIdentifier(csvRecord.get(0))
            .supplyDays(
                StringUtils.trimToNull(csvRecord.get(1)) != null
                    ? Integer.parseInt(StringUtils.trimToNull(csvRecord.get(1)))
                    : null)
            .intervalDays(
                StringUtils.trimToNull(csvRecord.get(2)) != null
                    ? Integer.parseInt(StringUtils.trimToNull(csvRecord.get(2)))
                    : null)
            .dispensedQuantity(
                StringUtils.trimToNull(csvRecord.get(3)) != null
                    ? Double.parseDouble(StringUtils.trimToNull(csvRecord.get(3)))
                    : null)
            .build());
  }

  private void mapMockPrescriptionDTO(CSVRecord csvRecord) {

    prescriptionDTOMap.put(
        StringUtils.trimToNull(csvRecord.get(0)),
        Prescription.builder()
            .systemIdentifier(StringUtils.trimToNull(csvRecord.get(0)))
            .rxNumber(StringUtils.trimToNull(csvRecord.get(1)))
            .prescriberProvincialLocationIdentifier(StringUtils.trimToNull(csvRecord.get(2)))
            .prescriberProvincialLocationName(StringUtils.trimToNull(csvRecord.get(3)))
            .refillUntilDate(
                DateUtils.parseSystemDateFormatToLocalDate(
                    StringUtils.trimToNull(csvRecord.get(4))))
            .expiryDate(
                DateUtils.parseSystemDateFormatToLocalDate(
                    StringUtils.trimToNull(csvRecord.get(5))))
            .writtenDate(DateUtils.parseSystemDateFormatToLocalDate(csvRecord.get(4)))
            .refillUntilDate(DateUtils.parseSystemDateFormatToLocalDate(csvRecord.get(5)))
            .expiryDate(DateUtils.parseSystemDateFormatToLocalDate(csvRecord.get(6)))
            .prescribedQuantity(
                StringUtils.trimToNull(csvRecord.get(8)) != null
                    ? Double.parseDouble(StringUtils.trimToNull(csvRecord.get(8)))
                    : null)
            .totalAuthorizedQuantity(
                StringUtils.trimToNull(csvRecord.get(9)) != null
                    ? Double.parseDouble(StringUtils.trimToNull(csvRecord.get(9)))
                    : null)
            .unit(StringUtils.trimToNull(csvRecord.get(10)))
            .prescriptionStatus(List.of(csvRecord.get(11)))
            .trialSupplyFlag(Boolean.valueOf(StringUtils.trimToNull(csvRecord.get(12))))
            .refills(csvRecord.get(7))
            .remainingRefills(csvRecord.get(13))
            .previousFillStatusCode(csvRecord.get(14))
            .fillSubStatus(csvRecord.get(15))
            .authoritativeFlag(Boolean.valueOf(StringUtils.trimToNull(csvRecord.get(16))))
            .prescriptionSource(StringUtils.trimToNull(csvRecord.get(17)))
            .sig(
                PrescriptionSig.builder()
                    .language(LanguageCode.valueOf(StringUtils.trimToNull(csvRecord.get(18))))
                    .descriptions(
                        List.of(
                            SigDescription.builder()
                                .name(
                                    Map.of(
                                        LanguageCode.valueOf(csvRecord.get(18)), csvRecord.get(19)))
                                .build(),
                            SigDescription.builder()
                                .name(
                                    Map.of(
                                        LanguageCode.valueOf(csvRecord.get(18)), csvRecord.get(20)))
                                .build(),
                            SigDescription.builder()
                                .name(
                                    Map.of(
                                        LanguageCode.valueOf(csvRecord.get(18)), csvRecord.get(21)))
                                .build()))
                    .build())
            .build());
  }
}
