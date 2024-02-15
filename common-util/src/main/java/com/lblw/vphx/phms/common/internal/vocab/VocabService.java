package com.lblw.vphx.phms.common.internal.vocab;

import com.lblw.vphx.phms.common.internal.vocab.client.VocabBuilderWebClient;
import com.lblw.vphx.phms.domain.coding.CodeableConceptType;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.Province;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

/** Vocab Service to retrieve the vocabulary value as {@link Coding} for given Vocabulary */
@Service
@AllArgsConstructor
@Slf4j
public class VocabService {

  private final VocabBuilderWebClient vocabBuilderWebClient;

  @Getter
  /**
   * Key = Composition of (VocabName, Province) and Value = Composition of ( List of ( System Code ,
   * Coding ) )
   */
  private Map<Pair<CodeableConceptType, Province>, ConcurrentHashMap<String, Coding>>
      systemCodesToProvincialCodingsMap = new ConcurrentHashMap<>();

  @Getter
  /**
   * Key = Composition of (VocabName, Province) and Value = Composition of ( List of (
   * ProvincialCode ,Coding ) )
   */
  private Map<Pair<CodeableConceptType, Province>, ConcurrentHashMap<String, Coding>>
      provincialCodesToSystemCodingsMap = new ConcurrentHashMap<>();

  @Getter
  /**
   * Key = Composition of (VocabName, Province) and Value = Composition of ( List of (
   * ProvincialCode ,Coding ) )
   */
  private Map<Pair<CodeableConceptType, Province>, Map<String, Coding>>
      provincialCodesToProvincialCodingsMap = new ConcurrentHashMap<>();

  /**
   * Returns Provincial Coding for given system code.
   *
   * @param codeableConceptType - {@link CodeableConceptType}
   * @param province - {@link Province}
   * @param systemCode - {@link String}
   * @return Optional of Coding
   */
  public Optional<Coding> getProvincialCodingBySystemCode(
      CodeableConceptType codeableConceptType, Province province, @NonNull String systemCode) {

    var mapKey = Pair.of(codeableConceptType, province);
    return Optional.ofNullable(systemCodesToProvincialCodingsMap.get(mapKey))
        .flatMap(codings -> Optional.ofNullable(codings.get(systemCode)))
        .or(
            () ->
                vocabBuilderWebClient
                    .getCerxCodingByHwCode(
                        codeableConceptType.getRdsVocabKey(), province, systemCode)
                    .map(
                        coding -> {
                          if (!systemCodesToProvincialCodingsMap.containsKey(mapKey)) {
                            systemCodesToProvincialCodingsMap.put(
                                mapKey, new ConcurrentHashMap<>());
                          }
                          systemCodesToProvincialCodingsMap.get(mapKey).put(systemCode, coding);

                          return coding;
                        }));
  }

  /**
   * Returns Provincial Coding for given provincialCode.
   *
   * @param codeableConceptType - {@link CodeableConceptType}
   * @param province - {@link Province}
   * @param provincialCode - {@link String}
   * @return Optional of Coding
   */
  public Optional<Coding> getProvincialCodingByProvincialCode(
      CodeableConceptType codeableConceptType, Province province, String provincialCode) {

    var mapKey = Pair.of(codeableConceptType, province);
    return Optional.ofNullable(
            provincialCodesToProvincialCodingsMap.get(Pair.of(codeableConceptType, province)))
        .flatMap(codings -> Optional.ofNullable(codings.get(provincialCode)))
        .or(
            () ->
                vocabBuilderWebClient
                    .getCerxCodingByCerxCode(
                        codeableConceptType.getRdsVocabKey(), province, provincialCode)
                    .map(
                        coding -> {
                          if (!provincialCodesToProvincialCodingsMap.containsKey(mapKey)) {
                            provincialCodesToProvincialCodingsMap.put(mapKey, new HashMap<>());
                          }
                          provincialCodesToProvincialCodingsMap
                              .get(Pair.of(codeableConceptType, province))
                              .put(provincialCode, coding);

                          return coding;
                        }));
  }

  /**
   * Returns System Coding for given provincial code.
   *
   * @param codeableConceptType - {@link CodeableConceptType}
   * @param province - {@link Province}
   * @param provincialCode - {@link String}
   * @return Optional of Coding
   */
  public Optional<Coding> getSystemCodingByProvincialCode(
      CodeableConceptType codeableConceptType, Province province, @NonNull String provincialCode) {

    var mapKey = Pair.of(codeableConceptType, province);
    return Optional.ofNullable(provincialCodesToSystemCodingsMap.get(mapKey))
        .flatMap(codings -> Optional.ofNullable(codings.get(provincialCode)))
        .or(
            () ->
                vocabBuilderWebClient
                    .getHwCodingByCerxCode(
                        codeableConceptType.getRdsVocabKey(), province.name(), provincialCode)
                    .map(
                        coding -> {
                          if (!provincialCodesToSystemCodingsMap.containsKey(mapKey)) {
                            provincialCodesToSystemCodingsMap.put(
                                mapKey, new ConcurrentHashMap<>());
                          }
                          provincialCodesToSystemCodingsMap.get(mapKey).put(provincialCode, coding);

                          return coding;
                        }));
  }
}
