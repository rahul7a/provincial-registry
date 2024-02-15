package com.lblw.vphx.phms.common.internal.objectstorage.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import com.lblw.vphx.iams.securityauditengine.oauth.ApplicationProperties;
import com.lblw.vphx.iams.securityauditengine.oauth.OAuthClientCredentials;
import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.internal.objectstorage.client.ObjectStorageClient;
import com.lblw.vphx.phms.common.internal.objectstorage.client.ObjectStorageClientConfig;
import com.lblw.vphx.phms.common.security.services.OAuthClientService;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(
    classes = {
      ObjectStorageClientConfig.class,
      ObjectStorageService.class,
      ObjectStorageClient.class,
      OAuthClientService.class,
      OAuthClientCredentials.class,
      ApplicationProperties.class,
      WebClient.class
    })
@EnableConfigurationProperties(value = {InternalApiConfig.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectStorageServiceTest {
  @MockBean
  @Qualifier(ObjectStorageClientConfig.OBJECT_STORAGE_WEB_CLIENT_QUALIFIER)
  WebClient objectStorageWebClient;

  @MockBean ObjectStorageClient objectStorageClient;

  @MockBean OAuthClientService oAuthClientService;
  @MockBean
  @Qualifier("oauth-web-client")
  WebClient webClient;
  @Autowired private ObjectStorageService objectStorageService;
  private String certificateFolder;
  private String fileLocation;
  private byte[] testCertificateData;

  @BeforeEach
  void setupAndClearCache() {
    testCertificateData = new byte[] {0};
    certificateFolder = "temp".concat("/").concat("pharmacy_id");
    fileLocation =
        certificateFolder
            .concat("/")
            .concat("certificate_ref_id")
            .concat(CommonConstants.CERTIFICATE_FILE_EXT);
    Path certificateLocation = Path.of(fileLocation);
    try {
      Files.deleteIfExists(certificateLocation);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void givenCertificateNotCached_whenFetchCertificate_thenFetchCertificateAndCache() {
    var provincialPharmacyCertificateDetails =
        ProvincialPharmacyCertificateDetails.builder()
            .certificateReferenceId("certificate_ref_id")
            .password("XXXX")
            .alias("tq-certificate_alias")
            .expirationDateTime(Instant.now().plusSeconds(3600))
            .build();

    when(oAuthClientService.getJWT()).thenReturn(Mono.just("testJwtString"));
    when(objectStorageClient.getCertificate(
            provincialPharmacyCertificateDetails.getCertificateReferenceId(), "testJwtString"))
        .thenReturn(Mono.just(testCertificateData));

    StepVerifier.create(
            objectStorageService.fetchCertificateKeyStore(
                certificateFolder, provincialPharmacyCertificateDetails))
        .assertNext(
            response -> {
              Path certificateLocation = Path.of(fileLocation);
              assertTrue(Files.exists(certificateLocation));
              verify(objectStorageClient, times(1)).getCertificate(any(), any());
            })
        .verifyComplete();
  }

  @Test
  void givenCertificateCachedAndNotExpired_whenFetchCertificate_thenReturnFromCache() {
    var provincialPharmacyCertificateDetails =
        ProvincialPharmacyCertificateDetails.builder()
            .certificateReferenceId("certificate_ref_id")
            .password("XXXX")
            .alias("tq-certificate_alias")
            .expirationDateTime(Instant.now().plusSeconds(3600))
            .build();

    when(oAuthClientService.getJWT()).thenReturn(Mono.just("testJwtString"));
    when(objectStorageClient.getCertificate(
            provincialPharmacyCertificateDetails.getCertificateReferenceId(), "testJwtString"))
        .thenReturn(Mono.just(testCertificateData));
    StepVerifier.create(
            objectStorageService
                .fetchCertificateKeyStore(certificateFolder, provincialPharmacyCertificateDetails)
                .then(
                    objectStorageService.fetchCertificateKeyStore(
                        certificateFolder, provincialPharmacyCertificateDetails)))
        .assertNext(
            response -> {
              Path certificateLocation = Path.of(fileLocation);
              assertTrue(Files.exists(certificateLocation));
              verify(objectStorageClient, times(1)).getCertificate(any(), any());
            })
        .verifyComplete();
  }

  @Test
  void givenCertificateCachedAndExpired_whenFetchCertificate_thenFetchAndCache() {

    var provincialPharmacyCertificateDetails =
        ProvincialPharmacyCertificateDetails.builder()
            .certificateReferenceId("certificate_ref_id")
            .password("XXXX")
            .alias("tq-certificate_alias")
            .expirationDateTime(Instant.now().minusSeconds(3600))
            .build();
    when(oAuthClientService.getJWT()).thenReturn(Mono.just("testJwtString"));
    when(objectStorageClient.getCertificate(
            provincialPharmacyCertificateDetails.getCertificateReferenceId(), "testJwtString"))
        .thenReturn(Mono.just(testCertificateData));
    StepVerifier.create(
            objectStorageService
                .fetchCertificateKeyStore(certificateFolder, provincialPharmacyCertificateDetails)
                .flatMap(
                    (response) -> {
                      provincialPharmacyCertificateDetails.setExpirationDateTime(
                          Instant.now().minusSeconds(3600));
                      return objectStorageService.fetchCertificateKeyStore(
                          certificateFolder, provincialPharmacyCertificateDetails);
                    }))
        .assertNext(
            response -> {
              Path certificateLocation = Path.of(fileLocation);
              assertTrue(Files.exists(certificateLocation));
              verify(objectStorageClient, times(2)).getCertificate(any(), any());
            })
        .verifyComplete();
  }
}
