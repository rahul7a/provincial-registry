package com.lblw.vphx.phms.common.internal.services.config;

import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = InternalApiConfig.class)
@EnableConfigurationProperties(value = InternalApiConfig.class)
@ActiveProfiles("test")
class InternalApiConfigTest {

  @Autowired private InternalApiConfig internalApiConfig;

  @Test
  @DisplayName("Test IAM configuration are populated from configured environment profile")
  void testIAMPropertiesAutoConfiguration() {

    Assertions.assertNotNull(internalApiConfig.getIam());
  }

  @Test
  @DisplayName("Test Populated IAM configurations are expected")
  void testIAMPropertiesAutoConfigurationValidation() {
    Assertions.assertEquals("test-clientid", internalApiConfig.getIam().getClientId());

    Assertions.assertEquals("test-clientsecret", internalApiConfig.getIam().getClientSecret());

    Assertions.assertEquals("test-environment", internalApiConfig.getIam().getEnvironment());
  }

  @Test
  @DisplayName("Test RDS configuration are populated from configured environment profile")
  void testRDSPropertiesAutoConfiguration() {
    Assertions.assertNotNull(internalApiConfig.getRds());
  }

  @Test
  @DisplayName("Test Populated RDS configurations are expected")
  void testRDSPropertiesAutoConfigurationValidation() {
    var vocabBuilder = internalApiConfig.getRds();
    Assertions.assertEquals(
        "http://hwl-dis-referencedata.hwl-dis-sit1.svc.cluster.local:8080/",
        vocabBuilder.getBaseUrl());
    Assertions.assertEquals("v1/lookup/VOCAB/{vocabType}", vocabBuilder.getVocabPath());
    Assertions.assertEquals("/actuator/health", vocabBuilder.getActuator());
    Assertions.assertEquals("0 0 3 1/1 * ?", vocabBuilder.getSchedule().getCron());
  }

  @Test
  @DisplayName("Test Populated DPS configurations are expected")
  void testDPSPropertiesAutoConfigurationValidation() {
    var dps = internalApiConfig.getDps();
    Assertions.assertNotNull(dps);
    Assertions.assertEquals("test-dps-url", dps.getBaseUrl());
    Assertions.assertEquals("deidentify/blob", dps.getPaths().getDeidentifyBlobPath());
    Assertions.assertEquals("reidentify/blob", dps.getPaths().getReidentifyBlobPath());
    Assertions.assertEquals(
        Set.of(MessageProcess.PATIENT_SEARCH, MessageProcess.PRESCRIPTION_TRANSACTION_SEARCH),
        dps.getEnableCompression());
    Assertions.assertEquals(
        Set.of(MessageProcess.PATIENT_SEARCH, MessageProcess.PATIENT_CONSENT),
        dps.getEnableEncryption());
  }
}
