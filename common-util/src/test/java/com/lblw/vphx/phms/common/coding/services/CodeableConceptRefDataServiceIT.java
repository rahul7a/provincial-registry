package com.lblw.vphx.phms.common.coding.services;

import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.internal.vocab.RdsUtil;
import com.lblw.vphx.phms.common.internal.vocab.VocabService;
import com.lblw.vphx.phms.common.internal.vocab.client.VocabBuilderWebClient;
import com.lblw.vphx.phms.common.internal.vocab.configuration.VocabWebClientConfiguration;
import com.lblw.vphx.phms.common.internal.vocab.runner.VocabBuilderRunner;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.Province;
import com.sdm.ehealth.referencedata.client.cache.*;
import com.sdm.ehealth.referencedata.client.configuration.ReferenceDataProperties;
import com.sdm.ehealth.referencedata.client.service.LookupService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    classes = {
      CodeableConceptRefDataService.class,
      VocabService.class,
      VocabBuilderWebClient.class,
      ExceptionClient.class,
      VocabClient.class,
      OidClient.class,
      VocabWithoutProvinceClient.class,
      UomClient.class,
      StoreClient.class,
      RdsUtil.class,
      LookupService.class,
      RestTemplate.class,
      VocabBuilderRunner.class
    })
@Import(VocabWebClientConfiguration.class)
@EnableConfigurationProperties(value = {InternalApiConfig.class, ReferenceDataProperties.class})
@ActiveProfiles("test")
class CodeableConceptRefDataServiceIT {
  @Autowired CodeableConceptRefDataService codeableConceptRefDataService;
  @Autowired VocabBuilderRunner vocabBuilderRunner;
  @Autowired VocabService vocabService;

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class FindProvincialRoleCodingBySystemRoleCode {
    @Test
    void givenNullSystemCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialRoleCodingBySystemRoleCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenValidSystemCodeWithMapping_whenProvincialCodeIsFetched_thenShouldReturnMappingWithOID() {
      codeableConceptRefDataService
          .findProvincialRoleCodingBySystemRoleCode(Province.QC, "2060000")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("DEN", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("DENTIST").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("DENTISTE").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
                Assertions.assertEquals("2.16.840.1.113883.4.302", coding.getCodingIdentifier());
              },
              Assertions::fail);
    }

    @Test
    void givenInvalidSystemCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialRoleCodingBySystemRoleCode(Province.QC, "349")
          .ifPresent(coding -> Assertions.fail("Unexpected coding: " + coding));
    }

    @Test
    void givenBlankSystemCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialRoleCodingBySystemRoleCode(Province.QC, "")
          .ifPresent(coding -> Assertions.fail("Unexpected coding: " + coding));
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class FindSystemRoleCodingByProvincialRoleCode {
    @BeforeAll()
    void beforeAll() {
      vocabBuilderRunner.run().block();
    }

    @Test
    void givenNullSystemCode_whenSystemCodingIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findSystemRoleCodingByProvincialRoleCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenSystemCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findSystemRoleCodingByProvincialRoleCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenSystemCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findSystemRoleCodingByProvincialRoleCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenValidProvincialCodeWithMapping_whenSystemCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findSystemRoleCodingByProvincialRoleCode(Province.QC, "DEN")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.ALL, coding.getProvince());
                Assertions.assertEquals("2060000", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("DENTIST").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("DENTISTE").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenSystemCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findSystemRoleCodingByProvincialRoleCode(Province.QC, "BIOCH")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("Unknown Code: BIOCH", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: BIOCH")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: BIOCH")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenInvalidProvincialCode_whenSystemCodingIsFetched_thenShouldReturnUnknownMappings() {
      codeableConceptRefDataService
          .findSystemRoleCodingByProvincialRoleCode(Province.QC, "INVALID")
          .ifPresentOrElse(
              coding ->
                  Assertions.assertEquals(
                      Coding.builder()
                          .province(Province.QC)
                          .code("Unknown Code: INVALID")
                          .display(
                              new LocalizedText[] {
                                LocalizedText.builder()
                                    .text("Unknown Code: INVALID")
                                    .language(LanguageCode.ENG)
                                    .build(),
                                LocalizedText.builder()
                                    .text("Code Inconnu: INVALID")
                                    .language(LanguageCode.FRA)
                                    .build()
                              })
                          .build(),
                      coding),
              Assertions::fail);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class FindProvincialSpecialityRoleCodingBySystemSpecialityRoleCode {
    @BeforeAll()
    void beforeAll() {
      vocabBuilderRunner.run().block();
    }

    @Test
    void givenNullSystemCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void givenValidSystemCodeWithMapping_whenProvincialCodeIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(Province.QC, "1890000")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("227", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Medical microbiology and infectiology")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Microbiologie médicale et infectiologie")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class FindSystemSpecialityRoleCodingByProvincialSpecialityRoleCode {
    @BeforeAll()
    void beforeAll() {
      vocabBuilderRunner.run().block();
    }

    @Test
    void givenNullSystemCode_whenSystemCodingIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenSystemCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenSystemCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenValidProvincialCodeWithMapping_whenSystemCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "227")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.ALL, coding.getProvince());
                Assertions.assertEquals("1880000", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("MEDICAL MICROBIOLOGY")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("MICROBIOLOGIE MÉDICALE")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenSystemCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "342")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("Unknown Code: 342", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: 342")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: 342")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenInvalidProvincialCode_whenSystemCodingIsFetched_thenShouldReturnUnknownMappings() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "INVALID")
          .ifPresentOrElse(
              coding ->
                  Assertions.assertEquals(
                      Coding.builder()
                          .province(Province.QC)
                          .code("Unknown Code: INVALID")
                          .display(
                              new LocalizedText[] {
                                LocalizedText.builder()
                                    .text("Unknown Code: INVALID")
                                    .language(LanguageCode.ENG)
                                    .build(),
                                LocalizedText.builder()
                                    .text("Code Inconnu: INVALID")
                                    .language(LanguageCode.FRA)
                                    .build()
                              })
                          .build(),
                      coding),
              Assertions::fail);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class GivenVocabFailedToInitialize {
    @BeforeAll()
    void beforeAll() {
      vocabService.getSystemCodesToProvincialCodingsMap().clear();
      vocabService.getProvincialCodesToSystemCodingsMap().clear();
      vocabService.getProvincialCodesToProvincialCodingsMap().clear();
    }

    @Test
    void givenValidProvincialCodeWithMapping_whenSystemCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "227")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.ALL, coding.getProvince());
                Assertions.assertEquals("1880000", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("MEDICAL MICROBIOLOGY")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("MICROBIOLOGIE MÉDICALE")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenSystemCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "342")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("Unknown Code: 342", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: 342")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: 342")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenInvalidProvincialCode_whenSystemCodingIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(Province.QC, "INVALID")
          .ifPresentOrElse(
              coding ->
                  Assertions.assertEquals(
                      Coding.builder()
                          .province(Province.QC)
                          .code("Unknown Code: INVALID")
                          .display(
                              new LocalizedText[] {
                                LocalizedText.builder()
                                    .text("Unknown Code: INVALID")
                                    .language(LanguageCode.ENG)
                                    .build(),
                                LocalizedText.builder()
                                    .text("Code Inconnu: INVALID")
                                    .language(LanguageCode.FRA)
                                    .build()
                              })
                          .build(),
                      coding),
              Assertions::fail);
    }

    @Nested
    class ProvincialPrescriptionActDetectedIssueCodingByProvincialCodeTests {
      @Test
      void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
        codeableConceptRefDataService
            .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, null)
            .ifPresent(coding -> Assertions.fail());
      }

      @Test
      void
          givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
        codeableConceptRefDataService
            .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "NA")
            .ifPresentOrElse(
                coding -> {
                  Assertions.assertEquals("", coding.getCode());
                  Assertions.assertArrayEquals(
                      new LocalizedText[] {
                        LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                        LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                      },
                      coding.getDisplay());
                },
                Assertions::fail);
      }

      @Test
      void
          givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
        codeableConceptRefDataService
            .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "UNK")
            .ifPresentOrElse(
                coding -> {
                  Assertions.assertEquals("UNK", coding.getCode());
                  Assertions.assertArrayEquals(
                      new LocalizedText[] {
                        LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                        LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                      },
                      coding.getDisplay());
                },
                Assertions::fail);
      }

      @Test
      void
          givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
        codeableConceptRefDataService
            .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "ALGY")
            .ifPresentOrElse(
                coding -> {
                  Assertions.assertEquals("ALGY", coding.getCode());
                  Assertions.assertArrayEquals(
                      new LocalizedText[] {
                        LocalizedText.builder()
                            .text("allergy alert")
                            .language(LanguageCode.ENG)
                            .build(),
                        LocalizedText.builder()
                            .text("alerte d'allergie")
                            .language(LanguageCode.FRA)
                            .build()
                      },
                      coding.getDisplay());
                },
                Assertions::fail);
      }

      @Test
      void
          givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
        codeableConceptRefDataService
            .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "HH")
            .ifPresentOrElse(
                coding -> {
                  Assertions.assertEquals("HH", coding.getCode());
                  Assertions.assertArrayEquals(
                      new LocalizedText[] {
                        LocalizedText.builder()
                            .text("Unknown Code: HH")
                            .language(LanguageCode.ENG)
                            .build(),
                        LocalizedText.builder()
                            .text("Code Inconnu: HH")
                            .language(LanguageCode.FRA)
                            .build()
                      },
                      coding.getDisplay());
                },
                Assertions::fail);
      }
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionIssuePriorityCodingByProvincialCodeTests {
    @BeforeAll()
    void beforeAll() {
      vocabBuilderRunner.run().block();
    }

    @Test
    void givenNullProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssuePriorityCodingByProvincialCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssuePriorityCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssuePriorityCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssuePriorityCodingByProvincialCode(Province.QC, "E")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("E", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("error").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Erreur").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenValidProvincialCodeWithoutMapping_whenIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssuePriorityCodingByProvincialCode(Province.QC, "E1")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("E1", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: E1")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: E1")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenInvalidProvincialCode_whenSystemCodingIsFetched_thenShouldReturnUnknownMappings() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssuePriorityCodingByProvincialCode(Province.QC, "INVALID")
          .ifPresentOrElse(
              coding ->
                  Assertions.assertEquals(
                      Coding.builder()
                          .province(Province.QC)
                          .code("INVALID")
                          .display(
                              new LocalizedText[] {
                                LocalizedText.builder()
                                    .text("Unknown Code: INVALID")
                                    .language(LanguageCode.ENG)
                                    .build(),
                                LocalizedText.builder()
                                    .text("Code Inconnu: INVALID")
                                    .language(LanguageCode.FRA)
                                    .build()
                              })
                          .build(),
                      coding),
              Assertions::fail);
    }
  }

  @Nested
  class ProvincialPrescriptionIssueSeverityCodingByProvincialCodeTests {
    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssueSeverityCodingByProvincialCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssueSeverityCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssueSeverityCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssueSeverityCodingByProvincialCode(Province.QC, "H")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("H", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("High").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Haute").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionIssueSeverityCodingByProvincialCode(Province.QC, "HH")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("HH", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: HH")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: HH")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionAdministrationSiteCodingByProvincialCodeTests {
    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionAdministrationSiteCodingByProvincialCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionAdministrationSiteCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionAdministrationSiteCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionAdministrationSiteCodingByProvincialCode(Province.QC, "LA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("LA", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Left arm").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("bras gauche").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionAdministrationSiteCodingByProvincialCode(Province.QC, "ALA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("ALA", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: ALA")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: ALA")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionRouteOfAdminCodingByProvincialCodeTests {
    @BeforeAll()
    void beforeAll() {
      vocabBuilderRunner.run().block();
    }

    @Test
    void givenNullSystemCode_whenSystemCodingIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionRouteOfAdminCodingByProvincialCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenSystemCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionRouteOfAdminCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenSystemCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionRouteOfAdminCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenValidProvincialCodeWithMapping_whenSystemCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionRouteOfAdminCodingByProvincialCode(Province.QC, "BUC")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("BUC", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("topical application, buccal")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("application topique buccale")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenInvalidProvincialCode_whenSystemCodingIsFetched_thenShouldReturnUnkownMappings() {
      codeableConceptRefDataService
          .findProvincialPrescriptionRouteOfAdminCodingByProvincialCode(Province.QC, "INVALID")
          .ifPresentOrElse(
              coding ->
                  Assertions.assertEquals(
                      Coding.builder()
                          .province(Province.QC)
                          .code("INVALID")
                          .display(
                              new LocalizedText[] {
                                LocalizedText.builder()
                                    .text("Unknown Code: INVALID")
                                    .language(LanguageCode.ENG)
                                    .build(),
                                LocalizedText.builder()
                                    .text("Code Inconnu: INVALID")
                                    .language(LanguageCode.FRA)
                                    .build()
                              })
                          .build(),
                      coding),
              Assertions::fail);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionPackSizeUoMCodingByProvincialCodeTests {
    @BeforeAll()
    void beforeAll() {
      vocabBuilderRunner.run().block();
    }

    @Test
    void givenNullProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionPackSizeUoMCodingByProvincialCode(Province.QC, null)
          .ifPresent(coding -> Assertions.fail());
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionPackSizeUoMCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionPackSizeUoMCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionPackSizeUoMCodingByProvincialCode(Province.QC, "U")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals(Province.QC, coding.getProvince());
                Assertions.assertEquals("U", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unit").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Unité(s)").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              Assertions::fail);
    }

    @Test
    void givenInvalidProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnUnkownMappings() {
      codeableConceptRefDataService
          .findProvincialPrescriptionPackSizeUoMCodingByProvincialCode(Province.QC, "INVALID")
          .ifPresentOrElse(
              coding ->
                  Assertions.assertEquals(
                      Coding.builder()
                          .province(Province.QC)
                          .code("INVALID")
                          .display(
                              new LocalizedText[] {
                                LocalizedText.builder()
                                    .text("Unknown Code: INVALID")
                                    .language(LanguageCode.ENG)
                                    .build(),
                                LocalizedText.builder()
                                    .text("Code Inconnu: INVALID")
                                    .language(LanguageCode.FRA)
                                    .build()
                              })
                          .build(),
                      coding),
              Assertions::fail);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionDurationLengthUnitCodingByProvincialCodeTests {
    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDurationLengthUnitCodingByProvincialCode(Province.QC, null)
          .ifPresent(
              coding ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Administration Instruction Effective Time Unit Code is having a null value"));
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDurationLengthUnitCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Administration Instruction Effective Time Unit Code is having a Null flavor Code 'NA'"));
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDurationLengthUnitCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Administration Instruction Effective Time Unit Code is having a Null flavor Code 'UNK'"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDurationLengthUnitCodingByProvincialCode(Province.QC, "a")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("a", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("year(s)").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("année(s)").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Administration Instruction Effective Time Unit Code is having a Provincial Code 'a' "));
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDurationLengthUnitCodingByProvincialCode(
              Province.QC, "Invalid")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("Invalid", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: Invalid")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: Invalid")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Administration Instruction Effective Time Unit Code is having a Invalid Provincial Code"));
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionDrugFormCodingByProvincialCodeTests {
    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDrugFormCodingByProvincialCode(Province.QC, null)
          .ifPresent(
              coding ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Drug Form Code is having a null value"));
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDrugFormCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Drug Form Code is having a Null flavor Code 'NA'"));
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDrugFormCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Drug Form Code is having a Null flavor Code 'UNK'"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDrugFormCodingByProvincialCode(Province.QC, "ORALSOL")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("ORALSOL", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Oral solution")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("solution orale")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Drug Form Code is having a Provincial Code 'a' "));
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionDrugFormCodingByProvincialCode(Province.QC, "Invalid")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("Unknown Code: Invalid", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: Invalid")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: Invalid")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Drug Form Code is having a Invalid Provincial Code"));
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionCareCompositionCodeByProvincialCodeTests {
    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(Province.QC, null)
          .ifPresent(
              coding ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Care Composition Code is having a null value"));
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Care Composition Code is having a Null flavor Code 'NA'"));
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Care Composition Code is having a Null flavor Code 'UNK'"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(Province.QC, "AMB")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("AMB", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("ambulatory").language(LanguageCode.ENG).build(),
                      LocalizedText.builder()
                          .text("clinique externe")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Care Composition Code is having a Provincial Code 'a' "));
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(Province.QC, "Invalid")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("Invalid", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: Invalid")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: Invalid")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Care Composition Code is having a Invalid Provincial Code"));
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionServiceLocationRoleTypeByProvincialCodeTests {
    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionServiceDeliveryLocationRoleTypeCodingByProvincialCode(
              Province.QC, null)
          .ifPresent(
              coding ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Service Location Type Code is having a null value"));
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionServiceDeliveryLocationRoleTypeCodingByProvincialCode(
              Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Service Location Type Code is having a Null flavor Code 'NA'"));
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Service Location Type Code is having a Null flavor Code 'UNK'"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionServiceDeliveryLocationRoleTypeCodingByProvincialCode(
              Province.QC, "ONCL")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("ONCL", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Medical oncology clinic")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Clinique médicale oncologie")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Service Location Type Code is having a Provincial Code 'a' "));
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionServiceDeliveryLocationRoleTypeCodingByProvincialCode(
              Province.QC, "Invalid")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("Invalid", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: Invalid")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: Invalid")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Service Location Type Code is having a Invalid Provincial Code"));
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionActDetectedIssueCodingByProvincialCodeTests {
    @BeforeAll()
    void beforeAll() {
      vocabBuilderRunner.run().block();
    }

    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, null)
          .ifPresent(
              coding ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Provincial RefusalToFill Code is having a null value"));
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "NA")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Provincial RefusalToFill Code is having a Null flavor Code 'NA'"));
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "UNK")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Provincial RefusalToFill Code is having a Null flavor Code 'UNK'"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "COMPLY")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("COMPLY", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("compliance alert")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("alerte d'inobservance")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Provincial RefusalToFill Code is having a Provincial Code 'COMPLY' "));
    }

    @Test
    void
        givenValidProvincialCodeWithoutMapping_whenProvincialCodingIsFetched_thenShouldReturnUnknownMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(Province.QC, "Invalid")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("Invalid", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unknown Code: Invalid")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Code Inconnu: Invalid")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () ->
                  Assertions.fail(
                      "Test case failed when Vocab mapping Provincial RefusalToFill Code is having a Invalid Provincial Code"));
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ProvincialPrescriptionPrescriptionLocationTypeCodingByProvincialCodeTests {
    @Test
    void givenNullProvincialCode_whenProvincialCodeIsFetched_thenShouldReturnEmpty() {
      codeableConceptRefDataService
          .findProvincialPrescriptionLocationTypeCodingByProvincialCode(Province.QC, null, "mockID")
          .ifPresent(coding -> Assertions.fail("Expecting no coding for null locationType"));
    }

    @Test
    void
        givenNullFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionLocationTypeCodingByProvincialCode(Province.QC, "NA", "mockID")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () -> Assertions.fail("Expecting coding for null flavor Code 'NA' to be present"));
    }

    @Test
    void
        givenUNKFlavorProvincialCode_whenProvincialCodingIsFetched_thenShouldReturnNullFlavorMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionLocationTypeCodingByProvincialCode(
              Province.QC, "UNK", "mockID")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("UNK", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder().text("Unknown").language(LanguageCode.ENG).build(),
                      LocalizedText.builder().text("Inconnu").language(LanguageCode.FRA).build()
                    },
                    coding.getDisplay());
              },
              () -> Assertions.fail("Expecting coding for null flavor Code 'UNK' to be present"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_whenProvincialCodingIsFetched_thenShouldReturnMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionLocationTypeCodingByProvincialCode(
              Province.QC, "HOSP", "mockID")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("HOSP", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Hospital, institutional pharmacy, laboratory")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text("Centre hospitalier, pharmacie d’établissement, laboratoire (CH)")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () -> Assertions.fail("Expecting coding for code 'HOSP' to be present"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_and_provinceIdIndicatesOutOfProvince_whenProvincialCodingIsFetched_thenShouldReturnPrefixedMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionLocationTypeCodingByProvincialCode(
              Province.QC, "HOSP", "9999999999")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("HOSP", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text(
                              "Out of Quebec Location: Hospital, institutional pharmacy, laboratory")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text(
                              "Lieu hors Québec: Centre hospitalier, pharmacie d’établissement, laboratoire (CH)")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () -> Assertions.fail("Expecting coding for code 'HOSP' to be present"));
    }

    @Test
    void
        givenValidProvincialCodeWithMapping_and_provinceIdIndicatesUnresolved_whenProvincialCodingIsFetched_thenShouldReturnPrefixedMapping() {
      codeableConceptRefDataService
          .findProvincialPrescriptionLocationTypeCodingByProvincialCode(
              Province.QC, "HOSP", "8888888888")
          .ifPresentOrElse(
              coding -> {
                Assertions.assertEquals("HOSP", coding.getCode());
                Assertions.assertArrayEquals(
                    new LocalizedText[] {
                      LocalizedText.builder()
                          .text("Unresolved Location: Hospital, institutional pharmacy, laboratory")
                          .language(LanguageCode.ENG)
                          .build(),
                      LocalizedText.builder()
                          .text(
                              "Lieu non resolu: Centre hospitalier, pharmacie d’établissement, laboratoire (CH)")
                          .language(LanguageCode.FRA)
                          .build()
                    },
                    coding.getDisplay());
              },
              () -> Assertions.fail("Expecting coding for code 'HOSP' to be present"));
    }
  }
}
