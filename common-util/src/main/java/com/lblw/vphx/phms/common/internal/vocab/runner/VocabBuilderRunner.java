package com.lblw.vphx.phms.common.internal.vocab.runner;

import com.lblw.vphx.phms.common.internal.vocab.VocabService;
import com.lblw.vphx.phms.common.internal.vocab.client.VocabBuilderWebClient;
import com.lblw.vphx.phms.common.internal.vocab.constants.VocabConstants;
import com.lblw.vphx.phms.common.internal.vocab.dto.VocabResponse;
import com.lblw.vphx.phms.domain.coding.CodeableConceptType;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.Province;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * {@link VocabBuilderRunner} registers a runner to immediately start afer application context has
 * been built on a conditional basis.
 *
 * <p>To enable, consider defining service.internal.rds.enabled = true
 *
 * <p>Orders at 100, as this should be the last in the queue.
 *
 * <p>When enabled, failure to load the values from rds-data service will not restrict the server
 * startup. However, vocabulary values would not be available till the scheduler reruns
 */
@Slf4j
@Component
public class VocabBuilderRunner implements InitializingBean {

  private final VocabBuilderWebClient vocabBuilderWebClient;
  private final VocabService vocabService;

  public VocabBuilderRunner(
      VocabBuilderWebClient vocabBuilderWebClient, VocabService vocabService) {
    this.vocabBuilderWebClient = vocabBuilderWebClient;
    this.vocabService = vocabService;
  }

  @Override
  public void afterPropertiesSet() {
    run().subscribeOn(Schedulers.boundedElastic()).subscribe();
  }

  /** It initiates the process of building vocabMaps */
  public Mono<Void> run() {
    log.info("Vocab Builder Runner initiated.");
    return initiateBuildingVocabularies();
  }

  /** Initiates building vocabularies from ref-data-service */
  private Mono<Void> initiateBuildingVocabularies() {
    var lookupKeys =
        Arrays.stream(CodeableConceptType.values())
            .flatMap(
                codeableConceptType ->
                    Arrays.stream(Province.getSupportedProvince())
                        .map(province -> Pair.of(codeableConceptType, province)))
            .collect(Collectors.toList());

    return Mono.zip(
            lookupKeys.stream().map(this::getQuebecVocabResponse).collect(Collectors.toList()),
            responses ->
                Arrays.stream(responses)
                    .map(VocabResponse.class::cast)
                    .collect(Collectors.toList()))
        .elapsed()
        .map(
            withElapsed -> {
              log.info("Time taken to load complete vocabulary {} ms.", withElapsed.getT1());
              return withElapsed.getT2();
            })
        .flatMapMany(
            responses -> Flux.zip(Flux.fromIterable(lookupKeys), Flux.fromIterable(responses)))
        .doOnNext(
            lookupKeyResponses -> {
              var lookupKey = lookupKeyResponses.getT1();
              var vocabResponse = lookupKeyResponses.getT2();

              var vocabSchemas =
                  vocabResponse.getQuebecVocabularies().stream()
                      .filter(vocabSchema -> StringUtils.isNotBlank(vocabSchema.getCerxCode()))
                      .collect(Collectors.toList());

              if (vocabSchemas.isEmpty()) {
                return;
              }

              var provincialCodesToSystemCodingsMap =
                  vocabService.getProvincialCodesToSystemCodingsMap();
              var systemCodesToProvincialCodingsMap =
                  vocabService.getSystemCodesToProvincialCodingsMap();
              var provincialCodesToProvincialCodingsMap =
                  vocabService.getProvincialCodesToProvincialCodingsMap();

              provincialCodesToSystemCodingsMap.put(lookupKey, new ConcurrentHashMap<>());
              systemCodesToProvincialCodingsMap.put(lookupKey, new ConcurrentHashMap<>());
              provincialCodesToProvincialCodingsMap.put(lookupKey, new ConcurrentHashMap<>());

              vocabSchemas.stream()
                  .filter(vocabSchema -> StringUtils.isNotBlank(vocabSchema.getCerxCode()))
                  .forEach(
                      vocabSchema -> {
                        if (StringUtils.isNotBlank(vocabSchema.getHwCode())) {
                          provincialCodesToSystemCodingsMap
                              .get(lookupKey)
                              .putIfAbsent(
                                  vocabSchema.getCerxCode(),
                                  Coding.builder()
                                      .province(Province.ALL)
                                      .code(vocabSchema.getHwCode())
                                      .display(
                                          new LocalizedText[] {
                                            LocalizedText.builder()
                                                .text(vocabSchema.getHwDescription())
                                                .language(LanguageCode.ENG)
                                                .build(),
                                            LocalizedText.builder()
                                                .text(vocabSchema.getHwDescriptionFrench())
                                                .language(LanguageCode.FRA)
                                                .build()
                                          })
                                      .system(VocabConstants.SYSTEM)
                                      .build());

                          systemCodesToProvincialCodingsMap
                              .get(lookupKey)
                              .putIfAbsent(
                                  vocabSchema.getHwCode(),
                                  Coding.builder()
                                      .province(lookupKey.getSecond())
                                      .code(vocabSchema.getCerxCode())
                                      .display(
                                          new LocalizedText[] {
                                            LocalizedText.builder()
                                                .text(vocabSchema.getCerxDisplayName())
                                                .language(LanguageCode.ENG)
                                                .build(),
                                            LocalizedText.builder()
                                                .text(vocabSchema.getCerxDisplayNameFrench())
                                                .language(LanguageCode.FRA)
                                                .build()
                                          })
                                      .system(null)
                                      .build());
                        }

                        provincialCodesToProvincialCodingsMap
                            .get(lookupKey)
                            .putIfAbsent(
                                vocabSchema.getCerxCode(),
                                Coding.builder()
                                    .province(lookupKey.getSecond())
                                    .code(vocabSchema.getCerxCode())
                                    .display(
                                        new LocalizedText[] {
                                          LocalizedText.builder()
                                              .text(vocabSchema.getCerxDisplayName())
                                              .language(LanguageCode.ENG)
                                              .build(),
                                          LocalizedText.builder()
                                              .text(vocabSchema.getCerxDisplayNameFrench())
                                              .language(LanguageCode.FRA)
                                              .build()
                                        })
                                    .system(VocabConstants.SYSTEM)
                                    .build());
                      });
            })
        .then();
  }

  private Mono<VocabResponse> getQuebecVocabResponse(
      Pair<CodeableConceptType, Province> lookupKey) {
    return vocabBuilderWebClient
        .callReferenceDataService(
            lookupKey.getFirst().getRdsVocabKey(), lookupKey.getSecond().name())
        .onErrorReturn(VocabResponse.builder().quebecVocabularies(Collections.emptyList()).build())
        .doOnSubscribe(subscription -> log.info("Loading Vocabulary for Province: " + lookupKey));
  }
}
