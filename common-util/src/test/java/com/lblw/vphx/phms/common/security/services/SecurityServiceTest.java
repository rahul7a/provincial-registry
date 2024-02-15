package com.lblw.vphx.phms.common.security.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {ProvincialRequestProperties.class, SecurityService.class, InternalApiConfig.class})
@ActiveProfiles("test")
@EnableConfigurationProperties(ProvincialRequestProperties.class)
class SecurityServiceTest {

  @Autowired private ProvincialRequestProperties provincialRequestProperties;

  @Autowired private SecurityService securityService;

  @Test
  void verifySignature() throws Exception {
    String request =
        Files.readString(
            Paths.get("src/test/resources", "find_candidate_signed_request.xml").toAbsolutePath());

    assertTrue(securityService.validateSignature(request));
  }
}
