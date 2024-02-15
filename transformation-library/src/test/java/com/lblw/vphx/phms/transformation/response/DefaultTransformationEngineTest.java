package com.lblw.vphx.phms.transformation.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.transformation.response.transformers.DefaultResponseTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(DefaultResponseTransformer.class)
class DefaultTransformationEngineTest {
  @Autowired DefaultResponseTransformer defaultResponseTransformer;

  @Test
  void defaultTransformationTest() {
    String hl7Response = "hl7Response";
    assertNull(defaultResponseTransformer.transform(hl7Response));
  }

  @Test
  void checkProvince() {
    assertEquals(Province.QC, defaultResponseTransformer.getProvince());
  }
}
