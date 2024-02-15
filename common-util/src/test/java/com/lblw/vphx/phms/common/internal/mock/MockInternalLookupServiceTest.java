package com.lblw.vphx.phms.common.internal.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SigDescription;
import com.lblw.vphx.phms.domain.medication.*;
import com.lblw.vphx.phms.domain.patient.Patient;
import com.lblw.vphx.phms.domain.patient.PatientType;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.prescriber.Prescriber;
import com.lblw.vphx.phms.domain.prescription.Prescription;
import com.lblw.vphx.phms.domain.prescription.PrescriptionSig;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest(
    classes = {
      ResourceLoader.class,
      ProvincialRequestProperties.class,
      MockInternalLookupService.class
    })
@ActiveProfiles("test")
@EnableConfigurationProperties(ProvincialRequestProperties.class)
class MockInternalLookupServiceTest {

  @Autowired MockInternalLookupService internalLookupService;
  @Autowired ProvincialRequestProperties provincialRequestProperties;
  @MockBean ResourceLoader resourceLoader;

  @Test
  void searchPrescriber() {
    var prescriber = internalLookupService.fetchPrescriber("7");
    assertEquals(
        Prescriber.builder()
            .systemIdentifier("7")
            .provincialIdentifier("PI")
            .firstName("FN")
            .lastName("LN")
            .licenseNumber(null)
            .prescriberTypeCode("1940000")
            .licensingProvince("QC")
            .activeFlag(Boolean.TRUE)
            .build(),
        prescriber);
  }

  @Test
  void searchPatient() {
    var patient = internalLookupService.fetchPatient("0");
    assertEquals(
        Patient.builder()
            .systemId("0")
            .provincialIdentifier("0000000000")
            .firstName("FN")
            .lastName("LN")
            .dateOfBirth(LocalDate.of(2000, 1, 1))
            .gender(Gender.M)
            .type(PatientType.HUMAN)
            .build(),
        patient);
  }

  @Test
  void searchPharmacy() {
    internalLookupService
        .fetchPharmacy("-1")
        .as(StepVerifier::create)
        .assertNext(
            pharmacy -> {
              assertEquals(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .name("N")
                      .storeNumber("0")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(Pharmacy.Identifier.builder().value("1123465789").build())
                              .locationType(
                                  Pharmacy.LocationType.builder().province(Province.QC).build())
                              .telecom(Pharmacy.Telecom.builder().number("1234567890").build())
                              .address(
                                  Pharmacy.Address.builder()
                                      .streetAddressLine1("streetLineAddress")
                                      .streetAddressLine2("streetAddressLine2")
                                      .postalCode("postalCode")
                                      .city("city")
                                      .country("country")
                                      .build())
                              .build())
                      .build(),
                  pharmacy);
            });
  }

  @Test
  void searchMedication() {
    var medication = internalLookupService.fetchMedication("3");
    Assertions.assertThat(medication)
        .usingRecursiveComparison()
        .ignoringFields("ingredients.medication.tradeName", "packSize")
        .isEqualTo(
            Medication.builder()
                .type(MedicationType.COMPOUND)
                .din(null)
                .tradeName("mockTradeName")
                .category("C")
                .routeOfAdministration("IJ")
                .form("CAP")
                .systemIdentifier("3")
                .upc("mockUpc")
                .ingredients(
                    List.of(
                        MedicationIngredient.builder()
                            .medication(
                                Medication.builder()
                                    .type(MedicationType.DRUG)
                                    .din("mockIngredientDin1")
                                    .build())
                            .build(),
                        MedicationIngredient.builder()
                            .medication(
                                Medication.builder()
                                    .type(MedicationType.DRUG)
                                    .din("mockIngredientDin2")
                                    .build())
                            .build()))
                .tradeNameEnglish("mockTradeName")
                .tradeNameFrench("mockTradeName")
                .compoundType(CompoundType.builder().code("4").build())
                .uom(UOM.builder().code("AMP").build())
                .build())
        .ignoringFields("ingredients.medication.tradeName")
        .isEqualTo(medication);
  }

  @Test
  void searchPrescription() {
    var prescription = internalLookupService.fetchPrescription("1");
    Assertions.assertThat(prescription)
        .usingRecursiveComparison()
        .isEqualTo(
            Prescription.builder()
                .systemIdentifier("1")
                .rxNumber("mockRxNum")
                .prescriberProvincialLocationIdentifier("PLI")
                .prescriberProvincialLocationName("PLN")
                .writtenDate(LocalDate.of(2021, 11, 1))
                .expiryDate(LocalDate.of(2022, 1, 1))
                .prescribedQuantity(10.0)
                .totalAuthorizedQuantity(180.0)
                .unit("g")
                .prescriptionStatus(List.of("SUBMIT"))
                .trialSupplyFlag(true)
                .refills("0")
                .remainingRefills("1")
                .previousFillStatusCode("")
                .fillSubStatus("")
                .authoritativeFlag(true)
                .prescriptionSource("Written")
                .sig(
                    PrescriptionSig.builder()
                        .descriptions(
                            List.of(
                                SigDescription.builder()
                                    .name(Map.of(LanguageCode.ENG, "TAKE"))
                                    .build(),
                                SigDescription.builder()
                                    .name(Map.of(LanguageCode.ENG, "1"))
                                    .build(),
                                SigDescription.builder()
                                    .name(Map.of(LanguageCode.ENG, "CAPSULE"))
                                    .build()))
                        .language(LanguageCode.ENG)
                        .build())
                .build());
  }

  @Test
  void searchPrescriptionTransaction() {
    internalLookupService
        .fetchPrescriptionTransaction("-1")
        .as(StepVerifier::create)
        .assertNext(
            prescriptionTransaction ->
                Assertions.assertThat(prescriptionTransaction)
                    .usingRecursiveComparison()
                    .isEqualTo(
                        PrescriptionTransaction.builder()
                            .systemIdentifier("-1")
                            .supplyDays(50)
                            .intervalDays(5)
                            .dispensedQuantity(80.0)
                            .build()))
        .verifyComplete();
  }

  @Test
  void searchPrescriptionTransactionIntervalDaysNotNull() {
    internalLookupService
        .fetchPrescriptionTransaction("-1")
        .as(StepVerifier::create)
        .assertNext(
            prescriptionTransaction ->
                Assertions.assertThat(prescriptionTransaction.getIntervalDays()).isEqualTo(5))
        .verifyComplete();
  }

  @Test
  void searchPrescriptionTransactionIntervalDaysNull() {
    internalLookupService
        .fetchPrescriptionTransaction("0")
        .as(StepVerifier::create)
        .assertNext(
            prescriptionTransaction ->
                Assertions.assertThat(prescriptionTransaction.getIntervalDays()).isNull())
        .verifyComplete();
  }

  @Test
  void searchMedicationDinAndUpcNull() {
    var medication = internalLookupService.fetchMedication("16");
    Assertions.assertThat(medication)
        .usingRecursiveComparison()
        .ignoringFields("ingredients.medication.tradeName")
        .isEqualTo(
            Medication.builder()
                .type(MedicationType.DRUG)
                .din(null)
                .tradeName("ATASOL 500MG CAPLET")
                .category("A")
                .routeOfAdministration("IJ")
                .ingredients(List.of())
                .systemIdentifier("16")
                .upc(null)
                .form("COM")
                .tradeNameFrench("ATASOL 500MG CAPLET")
                .tradeNameEnglish("ATASOL 500MG CAPLET")
                .compoundType(CompoundType.builder().code("4").build())
                .packSize(PackSize.builder().value(100.0).build())
                .uom(UOM.builder().code("CAP").build())
                .build());
  }
}
