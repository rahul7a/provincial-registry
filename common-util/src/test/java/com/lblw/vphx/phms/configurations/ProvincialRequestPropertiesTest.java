package com.lblw.vphx.phms.configurations;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ProvincialRequestProperties.class)
@EnableConfigurationProperties(value = ProvincialRequestProperties.class)
@ActiveProfiles("test")
class ProvincialRequestPropertiesTest {

  @Autowired private ProvincialRequestProperties provincialRequestProperties;

  @Test
  void testRequestConfiguration() {

    Assertions.assertNotNull(provincialRequestProperties.getRequest());

    Assertions.assertNotNull(
        provincialRequestProperties.getRequest().getSecurity().getCertificate().getAlias());
    Assertions.assertNotNull(
        provincialRequestProperties.getRequest().getSecurity().getCertificate().getPassword());
    Assertions.assertNotNull(
        provincialRequestProperties
            .getRequest()
            .getSecurity()
            .getCertificate()
            .getDefaultLocation());
  }

  @Test
  void transactionPropertiesTest() {
    // DSQ Requests
    Assertions.assertEquals(
        "findCandidateQueryUri",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_SEARCH.getName())
            .getUri());
    Assertions.assertEquals(
        "findCandidateQuerySOAPAction",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_SEARCH.getName())
            .getHeader()
            .getSoapAction());
    Assertions.assertEquals(
        "findCandidateQueryHeaderValue",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_SEARCH.getName())
            .getHeader()
            .getValue());
    Assertions.assertEquals(
        "findProviderQueryUri",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PROVIDER_SEARCH.getName())
            .getUri());
    Assertions.assertEquals(
        "findProviderQuerySOAPAction",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PROVIDER_SEARCH.getName())
            .getHeader()
            .getSoapAction());
    Assertions.assertEquals(
        "findProviderQueryHeaderValue",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PROVIDER_SEARCH.getName())
            .getHeader()
            .getValue());

    Assertions.assertEquals(
        "getPatientConsentUri",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_CONSENT.getName())
            .getUri());
    Assertions.assertEquals(
        "getPatientConsentSOAPAction",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_CONSENT.getName())
            .getHeader()
            .getSoapAction());
    Assertions.assertEquals(
        "getPatientConsentHeaderValue",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_CONSENT.getName())
            .getHeader()
            .getValue());
    Assertions.assertEquals(
        "findLocationSummaryUri",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_SEARCH.getName())
            .getUri());
    Assertions.assertEquals(
        "findLocationSummarySOAPAction",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_SEARCH.getName())
            .getHeader()
            .getSoapAction());
    Assertions.assertEquals(
        "findLocationSummaryHeaderValue",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_SEARCH.getName())
            .getHeader()
            .getValue());
    Assertions.assertEquals(
        "findLocationDetailsUri",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_DETAILS.getName())
            .getUri());
    Assertions.assertEquals(
        "findLocationDetailsSOAPAction",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_DETAILS.getName())
            .getHeader()
            .getSoapAction());
    Assertions.assertEquals(
        "findLocationDetailsHeaderValue",
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_DETAILS.getName())
            .getHeader()
            .getValue());
  }

  @Test
  void senderPropertiesTest() {
    Assertions.assertNotNull(
        provincialRequestProperties.getRequest().getSender().getPharmacyGroup());
    Assertions.assertNotNull(
        provincialRequestProperties.getRequest().getSender().getPharmacyLocationId());
    Assertions.assertNotNull(
        provincialRequestProperties.getRequest().getSender().getApplicationVersionNumber());
    Assertions.assertNotNull(provincialRequestProperties.getRequest().getSender().getExtension());
    Assertions.assertNotNull(provincialRequestProperties.getRequest().getSender().getName());
    Assertions.assertNotNull(
        provincialRequestProperties.getRequest().getSender().getProcessingCode());
  }

  @Test
  void webClientPropertiesTest() {
    Assertions.assertEquals(
        2000, provincialRequestProperties.getRequest().getWebclient().getResponseTimeOut());
    Assertions.assertEquals(
        2000, provincialRequestProperties.getRequest().getWebclient().getConnectionTimeOut());
    Assertions.assertEquals(
        1000, provincialRequestProperties.getRequest().getWebclient().getReadTimeOut());
    Assertions.assertEquals(
        1000, provincialRequestProperties.getRequest().getWebclient().getWriteTimeOut());
    Assertions.assertEquals(
        1000000,
        provincialRequestProperties.getRequest().getWebclient().getResponseMemoryLimitInBytes());
  }
}
