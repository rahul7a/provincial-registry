package com.lblw.vphx.phms.common.internal.vocab;

import com.lblw.vphx.phms.domain.common.Province;
import com.sdm.ehealth.referencedata.client.service.LookupService;
import com.sdm.ehealth.referencedata.dtos.VocabResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@link RdsUtil} is a util class for using methods for fetching vocabulary mappings from reference
 * data service using rds client library
 *
 * <p>It utilizes {@link LookupService} to retrieve relevant coding from Reference Data Service.
 *
 * @since <a href="https://jira.lblw.cloud/browse/LTPHVPHXQC-632">LTPHVPHXQC-632</a>
 */
@Component
public class RdsUtil {
  private final LookupService lookupService;
  /**
   * Public Constructor. Initializes lookupService
   *
   * @param lookupService {@link LookupService}
   */
  public RdsUtil(LookupService lookupService) {
    this.lookupService = lookupService;
  }

  public Optional<VocabResponse> getVocabByHwCode(
      String vocabType, Province province, String systemRoleCode) {
    return lookupService
        .getVocabByHWCode(
            vocabType,
            com.sdm.ehealth.referencedata.enums.Province.getProvince(province.toString()),
            systemRoleCode)
        .stream()
        .findFirst();
  }
}
