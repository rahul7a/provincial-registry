package com.lblw.vphx.phms.registry.configurations;

import static org.junit.jupiter.api.Assertions.*;

import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.NestedTestConfiguration;

@SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
public class ApplicationPropertiesTest {

  private static final String DEMO = "demo";
  private static final String DEV = "dev";
  private static final String DEV2 = "dev2";
  private static final String DEV3 = "dev3";
  private static final String LOCAL = "local";
  private static final String SIT = "sit";
  private static final String SIT2 = "sit2";
  private static final String UAT = "uat";
  private static final String TRAINING = "training";

  void testNullCheckApplicationProperties(InternalApiConfig internalApiConfig) {
    assertAll(
        "Check Application Properties for Internal API Configuration ",
        () -> {
          assertNotEquals(0, internalApiConfig.getResponseTimeout());
          assertNotEquals(0, internalApiConfig.getConnectTimeout());
          assertNotEquals(0, internalApiConfig.getReadTimeout());
          assertNotEquals(0, internalApiConfig.getWriteTimeout());
          assertNotNull(internalApiConfig.getGraphql().getUrl());
          assertNotNull(internalApiConfig.getObjectStorage().getBaseUrl());
          assertNotNull(internalApiConfig.getObjectStorage().getObjectStorePath());
          assertNotNull(internalApiConfig.getRds().getBaseUrl());
          assertNotNull(internalApiConfig.getRds().getSchedule().getCron());
          assertNotNull(internalApiConfig.getRds().getActuator());
          assertNotNull(internalApiConfig.getRds().getVocabPath());
          assertNotNull(internalApiConfig.getDps().getBaseUrl());
          assertNotNull(internalApiConfig.getDps().getPaths().getDeidentifyBlobPath());
          assertNotNull(internalApiConfig.getDps().getPaths().getReidentifyBlobPath());
          assertNotNull(internalApiConfig.getRds().getBaseUrl());
          assertNotNull(internalApiConfig.getIam().getClientId());
          assertNotNull(internalApiConfig.getIam().getClientSecret());
          assertNotNull(internalApiConfig.getIam().getEnvironment());
        });
  }

  void testNullCheckProvincialRequestProperties(
      ProvincialRequestProperties provincialRequestProperties) {

    assertAll(
        "Provincial Request Properties ",
        () -> {
          assertNotNull(provincialRequestProperties.getRequest());

          assertNotEquals(
              0,
              provincialRequestProperties
                  .getRequest()
                  .getWebclient()
                  .getResponseMemoryLimitInBytes());
          assertNotEquals(
              0, provincialRequestProperties.getRequest().getWebclient().getResponseTimeOut());
          assertNotEquals(
              0, provincialRequestProperties.getRequest().getWebclient().getConnectionTimeOut());
          assertNotEquals(
              0, provincialRequestProperties.getRequest().getWebclient().getReadTimeOut());
          assertNotEquals(
              0, provincialRequestProperties.getRequest().getWebclient().getWriteTimeOut());

          assertNotNull(provincialRequestProperties.getRequest().getSender().getPharmacyGroup());
          assertNotNull(
              provincialRequestProperties.getRequest().getSender().getPharmacyLocationId());
          assertNotNull(
              provincialRequestProperties.getRequest().getSender().getApplicationVersionNumber());
          assertNotNull(provincialRequestProperties.getRequest().getSender().getExtension());
          assertNotNull(provincialRequestProperties.getRequest().getSender().getName());

          assertNotNull(
              provincialRequestProperties.getRequest().getSecurity().getCertificate().getAlias());
          assertNotNull(
              provincialRequestProperties
                  .getRequest()
                  .getSecurity()
                  .getCertificate()
                  .getPassword());
          assertNotNull(
              provincialRequestProperties
                  .getRequest()
                  .getSecurity()
                  .getCertificate()
                  .getDefaultLocation());
          assertNotNull(
              provincialRequestProperties
                  .getRequest()
                  .getSecurity()
                  .getCertificate()
                  .getLocation());
        });
  }

  void testNullCheckProvincialRequestTransactionProperties(
      ProvincialRequestProperties provincialRequestProperties) {

    List<String> provincialRegistryProcesses =
        List.of(
            MessageProcess.PATIENT_SEARCH.getName(),
            MessageProcess.PROVIDER_SEARCH.getName(),
            MessageProcess.PATIENT_CONSENT.getName(),
            MessageProcess.LOCATION_SEARCH.getName(),
            MessageProcess.LOCATION_DETAILS.getName());

    assertAll(
        "Provincial Request Transaction Properties",
        () -> {
          provincialRegistryProcesses.forEach(
              messageProcess -> {
                assertNotNull(
                    provincialRequestProperties
                        .getRequest()
                        .getTransaction()
                        .get(messageProcess)
                        .getUri());
                assertNotNull(
                    provincialRequestProperties
                        .getRequest()
                        .getTransaction()
                        .get(messageProcess)
                        .getHeader()
                        .getSoapAction());
                assertNotNull(
                    provincialRequestProperties
                        .getRequest()
                        .getTransaction()
                        .get(messageProcess)
                        .getHeader()
                        .getValue());
                assertNotNull(
                    provincialRequestProperties
                        .getRequest()
                        .getTransaction()
                        .get(messageProcess)
                        .getMessagePayloadTemplate()
                        .getUri());
              });
        });
  }

  @Nested
  @DisplayName("Validates Default Property File")
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class DefaultProfileTest {
    @Autowired private ProvincialRequestProperties provincialRequestProperties;

    @Test
    void whenProfileIsDefault_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckProvincialRequestProperties(provincialRequestProperties);
      testNullCheckProvincialRequestTransactionProperties(provincialRequestProperties);
    }
  }

  @Nested
  @DisplayName("Validates Demo Property File")
  @ActiveProfiles(DEMO)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class DemoProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsDemo_thenValidateRequiredEntriesAreNotNull() {

      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates Local Property File")
  @ActiveProfiles(LOCAL)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class LocalProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsLocal_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates Dev Property File")
  @ActiveProfiles(DEV)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class TestDevProfile {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsDev_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates Dev2 Property File")
  @ActiveProfiles(DEV2)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class Dev2ProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsDev2_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates Dev3 Property File")
  @ActiveProfiles(DEV3)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class Dev3ProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsDev3_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates SIT Property File")
  @ActiveProfiles(SIT)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class SitProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsSIT_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates SIT2 Property File")
  @ActiveProfiles(SIT2)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class Sit2ProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsSIT2_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates UAT Property File")
  @ActiveProfiles(UAT)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class UatProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsUAT_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }

  @Nested
  @DisplayName("Validates Training Property File")
  @ActiveProfiles(TRAINING)
  @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @SpringBootTest(classes = {InternalApiConfig.class, ProvincialRequestProperties.class})
  @EnableConfigurationProperties(
      value = {InternalApiConfig.class, ProvincialRequestProperties.class})
  class TrainingProfileTest {
    @Autowired private InternalApiConfig internalApiConfig;

    @Test
    void whenProfileIsTraining_thenValidateRequiredEntriesAreNotNull() {
      testNullCheckApplicationProperties(internalApiConfig);
    }
  }
}
