package com.lblw.vphx.phms.common.internal.services;

import com.lblw.vphx.phms.common.internal.vocab.constants.VocabConstants;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.User;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This class will enrich provincialRequestControl for all transaction preProcessor(s)
 * hl7v3.Request(s) and domainRequest(s). Will have common enriching across transactions
 */
@Component
public class ProvincialRequestControlEnrichService {

  private InternalLookupService internalLookupService;

  public ProvincialRequestControlEnrichService(InternalLookupService internalLookupService) {
    this.internalLookupService = internalLookupService;
  }

  /**
   * This method will enrich ProvincialLocation based on data received from fetchPharmacy for all
   * transaction preProcessor(s) hl7v3.Request(s) and domainRequest(s).
   *
   * @return a Mono of Void.
   */
  private Mono<Void> enrichWithProvincialLocation() {
    return Mono.deferContextual(
        context -> {
          final ProvincialRequestControl provincialRequestControl =
              context.get(ProvincialRequestControl.class);
          var pharmacySystemId = provincialRequestControl.getPharmacy().getId();
          // TODO: build User number from gql
          provincialRequestControl.setUser(
              User.builder()
                  .firstName("ROGERE")
                  .lastName("MOQUIN")
                  .permitNumber("19650214")
                  .build());

          return internalLookupService
              .fetchPharmacy(pharmacySystemId)
              .doOnNext(
                  pharmacy -> {
                    // TODO: pharmacyId must be come from fetchPharmacy by  gql calls
                    pharmacy.setId(pharmacySystemId);
                    // TODO: storeNumber must be come from fetchPharmacy by  gql calls
                    pharmacy.setStoreNumber(
                        provincialRequestControl.getPharmacy().getStoreNumber());
                    pharmacy.setSupervisingPharmacist(provincialRequestControl.getPharmacy().getSupervisingPharmacist());
                    provincialRequestControl.setPharmacy(pharmacy);
                  })
              .then();
        });
  }

  // TODO: fetchPrescriber() logic to me moved from tran-lib to here later on
  /**
   * This method will enrich ProvincialProvider based on data received from fetchPrescriber for all
   * transaction preProcessor(s) hl7v3.Request(s) and domainRequest(s).
   *
   * @return a Mono of Void.
   */
  private Mono<Void> enrichWithProvincialProvider() {
    return Mono.deferContextual(
        context -> {
          final ProvincialRequestControl provincialRequestControl =
              context.get(ProvincialRequestControl.class);
          // TODO: build User number from gql
          provincialRequestControl.setUser(
              User.builder()
                  .firstName("ROGERE")
                  .lastName("MOQUIN")
                  .permitNumber("19650214")
                  .provincialId("CPN.00000128.QC.PRS")
                  .providerRole(
                      Coding.builder()
                          .code("1940000")
                          .province(Province.ALL)
                          .system(VocabConstants.SYSTEM)
                          .display(
                              new LocalizedText[] {
                                LocalizedText.builder()
                                    .text("PHYSICIAN")
                                    .language(LanguageCode.ENG)
                                    .build(),
                                LocalizedText.builder()
                                    .text("MÉDECIN")
                                    .language(LanguageCode.FRA)
                                    .build()
                              })
                          .build())
                  .build());
          return Mono.empty();
        });
  }

  /**
   * This method will enrich Supervising Pharmacist based on data received from
   * fetchSupervisingPharmacist for all transaction preProcessor(s) hl7v3.Request(s) and
   * domainRequest(s).
   *
   * @return a Mono of Void.
   */
  private Mono<Void> enrichWithSupervisingPharmacist() {
    return Mono.deferContextual(
        context -> {
          final ProvincialRequestControl provincialRequestControl =
              context.get(ProvincialRequestControl.class);
          var pharmacy = provincialRequestControl.getPharmacy();
          if (Strings.isBlank(pharmacy.getWorkstationHostname())) {
            return Mono.empty().then();
          }
          return internalLookupService
              .fetchSupervisingPharmacist(pharmacy.getId(), pharmacy.getWorkstationHostname())
              .doOnNext(
                  supervisingPharmacist -> {
                    pharmacy.setSupervisingPharmacist(supervisingPharmacist);
                    provincialRequestControl.setPharmacy(pharmacy);
                  })
              .then();
        });
  }

  /**
   * This method will enrich default Pharmacist based on data received from
   * storeGetDefaultPharmacist for all transaction preProcessor(s) hl7v3.Request(s) and
   * domainRequest(s).
   *
   * @return a Mono of Void.
   */
  private Mono<Void> enrichWithDefaultPharmacist() {
    return Mono.deferContextual(
        context -> {
          final ProvincialRequestControl provincialRequestControl =
              context.get(ProvincialRequestControl.class);
          var pharmacy = provincialRequestControl.getPharmacy();
          return internalLookupService
              .fetchDefaultPharmacist(pharmacy.getId())
              .doOnNext(
                  defaultPharmacist -> {
                    pharmacy.setDefaultPharmacist(defaultPharmacist);
                    provincialRequestControl.setPharmacy(pharmacy);
                  })
              .then();
        });
  }

  /**
   * This method will call all enrich methods for all transaction using both preProcessor(s)
   * hl7v3.Request(s) and domainRequest(s).
   *
   * @return a Mono of Void.
   */
  public Mono<Void> enrich() {
    return enrichWithSupervisingPharmacist()
        .then(enrichWithProvincialLocation())
        .then(enrichWithProvincialProvider())
        .then(enrichWithDefaultPharmacist());
  }
}
