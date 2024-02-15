package com.lblw.vphx.phms.transformation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.thymeleaf.testing.templateengine.engine.TestExecutor;

/**
 * Unit test for Find Candidate Query Request Thymeleaf template as generated in {@link
 * com.lblw.vphx.phms.transformation.services.TransformationService} using thymeleaf-testing library
 */
class ThymeleafTemplateTransformationTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "classpath:tests/PATIENT_SEARCH-request.thtest",
        "classpath:tests/PROVIDER_SEARCH-request.thtest",
        "classpath:tests/PATIENT_CONSENT-request.thtest",
        "classpath:tests/LOCATION_SEARCH-request.thtest"
      })
  void thymeLeafTemplateXmlTest(String filePath) {
    final TestExecutor executor = new TestExecutor();
    executor.execute(filePath);
    assertTrue(executor.isAllOK());
  }
}
